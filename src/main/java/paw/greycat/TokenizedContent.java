package paw.greycat;

import greycat.*;
import greycat.struct.*;
import greycat.utility.HashHelper;
import paw.greycat.struct.radix.array.RadixTreeArray;
import paw.tokenizer.token.ContentT;
import paw.tokenizer.token.NumberT;
import paw.tokenizer.token.Token;

import java.util.ArrayList;
import java.util.List;

import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static paw.PawConstants.*;
import static paw.greycat.VocabularyAccess.accessVocabulary;


public class TokenizedContent {


    private final Graph graph;
    private final long currentWorld;
    private final long currentTime;
    private final Node index;
    private final Node cache;
    private final Node delimiterVocab;
    private Node father = null;
    private Node tokenizeContent = null;
    private Node localStatistic = null;


    private TokenizedContent(Graph graph, long currentWorld, long currentTime, Node index, Node cache, Node delimiterVocab) {
        this.graph = graph;
        this.currentWorld = currentWorld;
        this.currentTime = currentTime;
        this.index = index;
        this.cache = cache;
        this.delimiterVocab = delimiterVocab;
    }

    private void free() {
        tokenizeContent.free();
        localStatistic.free();
        father = null;
        tokenizeContent = null;
        localStatistic = null;
    }

    private void setFather(Node father) {
        this.father = father;
    }

    private void synchronizeFatherInTime(TaskContext ctx, long currentTime, String fatherNodeVar) {
        Node fatherNode = (Node) ctx.variable(fatherNodeVar).get(0);
        //Update the father time if necessary
        if (fatherNode.time() != currentTime) {
            if (fatherNode.time() > currentTime) {
                ctx.endTask(ctx.result(), new RuntimeException("node is in the future"));
            } else {
                fatherNode.travelInTime(currentTime, result -> {
                    fatherNode.free();
                    setFather(result);
                    ctx.setVariable(fatherNodeVar, result);
                });
            }
        } else {
            father = fatherNode;
        }
    }

    private void getOrCreateTokenizeContentNode(String nameOfContent) {
        //check wether the content exist
        long[] result = ((RelationIndexed) father.getOrCreate(RELATION_INDEXED_NODE_TO_TOKENIZECONTENT, Type.RELATION_INDEXED)).select(NODE_NAME, nameOfContent);
        //If not creating it
        if (result.length == 0) {
            createTokenizeContentNode(nameOfContent);
            createLocalStatiscticNode();
        } else {
            //otherwise retrieve it and prepare it for the new version
            graph.lookup(currentWorld, currentTime, result[0], tc -> {
                tokenizeContent = tc;
                purgeLocalStatistics();
            });
        }
    }

    private void createTokenizeContentNode(String nameOfContent) {
        Node tc = graph.newNode(currentWorld, currentTime);
        tc.set(NODE_NAME, Type.STRING, nameOfContent);
        tc.set(NODE_TYPE, Type.INT, TOKENIZED_CONTENT);
        tc.getOrCreate(TOKENIZED_CONTENT_TYPES, Type.INT_ARRAY);
        tc.getOrCreate(TOKENIZED_CONTENT_HASH, Type.INT_ARRAY);
        tc.addToRelation(RELATION_TOKENIZECONTENT_TO_NODE, father);
        father.addToRelation(RELATION_INDEXED_NODE_TO_TOKENIZECONTENT, tc, NODE_NAME);
        tokenizeContent = tc;
    }

    private void createLocalStatiscticNode() {
        Node localStatistic = graph.newNode(currentWorld, currentTime);
        localStatistic.set(NODE_TYPE, Type.INT, LOCAL_STATISTIC);
        localStatistic.getOrCreate(LOCALSTATIC_MAP, Type.INT_TO_INT_MAP);
        localStatistic.set(LOCALSTATIC_DELIMITER, Type.INT, 0);
        localStatistic.set(LOCALSTATIC_CACHE, Type.INT, 0);
        localStatistic.set(LOCALSTATIC_NUMBER, Type.INT, 0);
        tokenizeContent.addToRelation(RELATION_TOKENIZECONTENT_TO_LOCAL_STAT, localStatistic);
        this.localStatistic = localStatistic;
    }

    private void purgeLocalStatistics() {
        tokenizeContent.relation(RELATION_TOKENIZECONTENT_TO_LOCAL_STAT, result -> {
            result[0].remove(LOCALSTATIC_MAP);
            result[0].getOrCreate(LOCALSTATIC_MAP, Type.INT_TO_INT_MAP);
            result[0].set(LOCALSTATIC_DELIMITER, Type.INT, 0);
            result[0].set(LOCALSTATIC_CACHE, Type.INT, 0);
            this.localStatistic = result[0];
        });
    }

    private void addTokens(List<Token> tokens) {
        int size = tokens.size();
        tokenizeContent.getIntArray(TOKENIZED_CONTENT_TYPES).init(size);
        tokenizeContent.getIntArray(TOKENIZED_CONTENT_HASH).init(size);
        tokenizeContent.remove(TOKENIZED_CONTENT_MASKS);
        tokenizeContent.remove(TOKENIZED_CONTENT_SUB_ID);
        tokenizeContent.getOrCreate(TOKENIZED_CONTENT_SUB_ID, Type.LONG_TO_LONG_MAP);
        int delimiters = 0;
        int numbers = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getType() == NUMBER_TOKEN) {
                numbers++;
            } else {
                if (token.getType() == DELIMITER_TOKEN) {
                    delimiters++;
                }
            }
            TokenHandler th = new TokenHandler(i, token);
            th.addTokenToTokenizeContent();
            th.free();
        }
        int cacheSize = tokenizeContent.getLongLongMap(TOKENIZED_CONTENT_SUB_ID).size();
        localStatistic.set(LOCALSTATIC_CACHE, Type.INT, cacheSize);
        localStatistic.set(LOCALSTATIC_DELIMITER, Type.INT, delimiters);
        localStatistic.set(LOCALSTATIC_NUMBER, Type.INT, numbers);
    }

    class TokenHandler {
        private final int inc;
        private final Token tokenObject;
        private final String token;
        private final byte type;
        private int hashToLookFor;
        private String subToken = null;
        private Node subCache = null;


        private TokenHandler(int inc, Token tokenO) {
            this.inc = inc;
            this.token = tokenO.getToken();
            this.type = tokenO.getType();
            this.hashToLookFor = HashHelper.hash(token);
            this.tokenObject = tokenO;
        }

        private void addTokenToTokenizeContent() {
            tokenizeContent.getIntArray(TOKENIZED_CONTENT_TYPES).set(inc, type);
            if (type == NUMBER_TOKEN) {
                tokenizeContent.getIntArray(TOKENIZED_CONTENT_HASH).set(inc, ((NumberT) tokenObject).getInt());
            } else {
                if (type == DELIMITER_TOKEN) {
                    tokenizeContent.getIntArray(TOKENIZED_CONTENT_HASH).set(inc, hashToLookFor);
                    delimiterVocab.getIntStringMap(DELIMITER_VOCABULARY).put(hashToLookFor, token);
                } else {
                    LongLongArrayMap masks = (LongLongArrayMap) tokenizeContent.getOrCreate(TOKENIZED_CONTENT_MASKS, Type.LONG_TO_LONG_ARRAY_MAP);
                    int[] mask = ((ContentT) tokenObject).getLowerString().getMask();
                    for (int i = mask.length - 1; i >= 0; i--) {
                        masks.putNoCheck(inc, mask[i]);
                    }
                    addIndexableTokenToTokenizeContent();
                }
            }
        }

        private void free() {
            if (subCache != null) {
                subCache.free();
                subCache = null;
            }
        }

        private void addIndexableTokenToTokenizeContent() {
            IntIntMap map = index.getIntIntMap(INDEXING_NODE_MAP_HASH_ID);
            int enodeId = map.get(hashToLookFor);
            if (enodeId == Constants.NULL_INT) {
                updateOrCreateGraphTokenFromCache();
            } else {
                updateStat(enodeId);
            }
        }

        private void updateStat(int indexEnode) {
            tokenizeContent.getIntArray(TOKENIZED_CONTENT_HASH).set(inc, indexEnode);
            IntIntMap mapOfAppearance = localStatistic.getIntIntMap(LOCALSTATIC_MAP);
            int count = 1;
            int old = mapOfAppearance.get(indexEnode);
            if (old != Constants.NULL_INT) {
                count += old;
            }
            mapOfAppearance.put(indexEnode, count);
        }


        private void updateOrCreateGraphTokenFromCache() {
            subToken = (token.length() > 1) ? token.substring(0, 2) : "1";

            RelationIndexed relationIndexed = (RelationIndexed) cache.getOrCreate(RELATION_INDEXED_CACHE_TO_SUBCACHE, Type.RELATION_INDEXED);
            long[] subcacheId = relationIndexed.select(NODE_NAME, subToken);

            if (subcacheId.length == 0) {
                createSubCacheNode();
                relationIndexed.add(subCache, NODE_NAME);
            } else {
                graph.lookup(0, BEGINNING_OF_TIME, subcacheId[0], result ->
                        subCache = result);
            }

            LongLongArrayMap map = subCache.getLongLongArrayMap(CACHE_MAP_HASH_ID);
            long[] data = map.get(hashToLookFor);

            if (data.length == 0) {
                createUpdateCacheEntry(true);
            } else {
                if (data.length / 3 < CACHE_THRESHOLD - 1) {
                    createUpdateCacheEntry(false);
                } else {
                    int enode = turnCacheIntoIndex(data);
                    updateStat(enode);
                }
            }
        }

        private void createSubCacheNode() {
            Node node = graph.newNode(0, BEGINNING_OF_TIME);
            node.set(NODE_NAME, Type.STRING, subToken);
            node.set(NODE_TYPE, Type.INT, SUB_CACHING_NODE);
            node.setTimeSensitivity(-1, 0);
            node.getOrCreate(CACHE_MAP_HASH_ID, Type.LONG_TO_LONG_ARRAY_MAP);
            node.getOrCreate(CACHE_VOCAB, Type.INT_TO_STRING_MAP);
            subCache = node;
        }

        private void createUpdateCacheEntry(boolean first) {
            LongLongArrayMap map = subCache.getLongLongArrayMap(CACHE_MAP_HASH_ID);
            map.putNoCheck(hashToLookFor, currentWorld);
            map.putNoCheck(hashToLookFor, currentTime);
            map.putNoCheck(hashToLookFor, tokenizeContent.id());

            if (first) {
                IntStringMap vocab = subCache.getIntStringMap(CACHE_VOCAB);
                vocab.put(hashToLookFor, token);
            }
            tokenizeContent.getIntArray(TOKENIZED_CONTENT_HASH).set(inc, hashToLookFor);
            LongLongMap subs = tokenizeContent.getLongLongMap(TOKENIZED_CONTENT_SUB_ID);
            subs.put(inc, subCache.id());
        }

        private int turnCacheIntoIndex(long[] data) {

            IntIntMap mapIndex = index.getIntIntMap(INDEXING_NODE_MAP_HASH_ID);
            RadixTreeArray radixTree = new RadixTreeArray(index);

            int radixId = radixTree.getOrCreate(token);

            mapIndex.put(hashToLookFor, radixId);

            createInvertedIndexEntry(data, radixId);

            LongLongArrayMap map = subCache.getLongLongArrayMap(CACHE_MAP_HASH_ID);
            for (int i = 0; i < data.length; i++) {
                map.delete(hashToLookFor, data[i]);
            }

            IntStringMap vocab = subCache.getIntStringMap(CACHE_VOCAB);
            vocab.remove(hashToLookFor);

            return radixId;
        }

        private void createInvertedIndexEntry(long[] data, int radixId) {
            List<Long> worlds = new ArrayList<>(data.length / 3);
            List<Long> times = new ArrayList<>(data.length / 3);
            List<Long> idNodes = new ArrayList<>(data.length / 3);
            long previousWorld = data[2];
            long previousTime = data[1];
            long previousID = data[0];
            worlds.add(previousWorld);
            times.add(previousTime);
            idNodes.add(previousID);
            boolean currentTC = false;
            for (int i = 3; i < data.length; i += 3) {
                long world = data[i + 2];
                long time = data[i + 1];
                long id = data[i];
                if (world == currentWorld && time == currentTime && id == tokenizeContent.id()) {
                    currentTC = true;
                } else {
                    if (previousID != id || previousTime != time || previousWorld != world) {
                        worlds.add(world);
                        times.add(time);
                        idNodes.add(id);
                        previousWorld = world;
                        previousTime = time;
                        previousID = id;
                    }
                }
            }
            long[] idA = idNodes.stream().mapToLong(i -> i).toArray();
            long[] timeA = times.stream().mapToLong(i -> i).toArray();
            long[] worldA = worlds.stream().mapToLong(i -> i).toArray();
            updateETokens(idA, timeA, worldA, radixId);
            if (currentTC) {
                updateEToken(tokenizeContent, radixId, true);
            }
        }

        private void updateETokens(long[] idNodes, long[] times, long[] worlds, int newId) {
            graph.lookupBatch(worlds, times, idNodes, result -> {
                for (int i = 0; i < result.length; i++) {
                    updateEToken(result[i], newId, false);
                    result[i].free();
                }
            });
        }

        private void updateEToken(Node nodeToUpdate, int newId, boolean current) {
            LongLongMap subs = nodeToUpdate.getLongLongMap(TOKENIZED_CONTENT_SUB_ID);
            int[] hashs = nodeToUpdate.getIntArray(TOKENIZED_CONTENT_HASH).extract();
            long subId = subCache.id();
            List<Integer> occurences = new ArrayList<>();
            subs.each((key, value) -> {
                if (subId == value && hashs[(int) key] == hashToLookFor) {
                    occurences.add((int) key);
                }
            });
            int count = occurences.size();
            for (int j = 0; j < count; j++) {
                subs.remove(occurences.get(j));
                nodeToUpdate.getIntArray(TOKENIZED_CONTENT_HASH).set(occurences.get(j), newId);
            }

            if (current) {
                IntIntMap mapOfAppearance = localStatistic.getIntIntMap(LOCALSTATIC_MAP);
                mapOfAppearance.put(newId, count);
            } else {
                nodeToUpdate.relation(RELATION_TOKENIZECONTENT_TO_LOCAL_STAT, ls -> {
                    ls[0].set(LOCALSTATIC_CACHE, Type.INT, (int) ls[0].get(LOCALSTATIC_CACHE) - count);
                    IntIntMap mapOfAppearance = ls[0].getIntIntMap(LOCALSTATIC_MAP);
                    mapOfAppearance.put(newId, count);
                    ls[0].free();
                });
            }
        }

    }

    public static Task updateOrCreateTokenizeContent(List<Token> tokens, String nameOfContent, String fatherNodeVar) {
        return newTask()
                .ifThen(ctx -> ctx.variable("vocabNodes") == null,
                        accessVocabulary()
                                //Retrieving for both their cache and index node
                                .traverse(RELATION_VOCAB_CACHE_INDEX_DEL)
                                .defineAsGlobalVar("vocabNodes")
                )
                .thenDo(ctx -> {
                    long currentWorld = ctx.world();
                    long currentTime = ctx.time();
                    Graph graph = ctx.graph();
                    Node cache = (Node) ctx.variable("vocabNodes").get(0);
                    Node index = (Node) ctx.variable("vocabNodes").get(1);
                    Node vocabdel = (Node) ctx.variable("vocabNodes").get(2);

                    TokenizedContent tc = new TokenizedContent(graph, currentWorld, currentTime, index, cache, vocabdel);

                    //Retrieving the father node
                    tc.synchronizeFatherInTime(ctx, currentTime, fatherNodeVar);

                    //Get the indexed relation of the father containing all of its tokenize content
                    tc.getOrCreateTokenizeContentNode(nameOfContent);

                    tc.addTokens(tokens);
                    tc.free();
                    //Add each token to the tokenized content node
                    ctx.continueTask();
                });
    }


}
