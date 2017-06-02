package paw.greycat;

import greycat.*;
import greycat.struct.*;
import greycat.utility.HashHelper;
import paw.greycat.struct.radix.RadixTree;
import paw.tokenizer.token.ContentT;
import paw.tokenizer.token.NumberT;
import paw.tokenizer.token.Token;

import java.util.Arrays;
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
    private Node father = null;
    private Node tokenizeContent = null;
    private Node localStatistic = null;


    private TokenizedContent(Graph graph, long currentWorld, long currentTime, Node index, Node cache) {
        this.graph = graph;
        this.currentWorld = currentWorld;
        this.currentTime = currentTime;
        this.index = index;
        this.cache = cache;
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
        tc.getOrCreate(TOKENIZED_CONTENT_SUBS_DEL, Type.STRING_ARRAY);
        tc.getOrCreate(TOKENIZED_CONTENT_IDS_NUMB, Type.LONG_ARRAY);
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
        tokenizeContent.getStringArray(TOKENIZED_CONTENT_SUBS_DEL).init(size);
        tokenizeContent.getLongArray(TOKENIZED_CONTENT_IDS_NUMB).init(size);
        tokenizeContent.remove(TOKENIZED_CONTENT_MASKS);
        for (int i = 0; i < tokens.size(); i++) {
            tokenizeContent.getStringArray(TOKENIZED_CONTENT_SUBS_DEL).set(i, "-");
            Token token = tokens.get(i);
            TokenHandler th = new TokenHandler(i, token);
            th.addTokenToTokenizeContent();
            th.free();
        }
    }

    class TokenHandler {
        private final int inc;
        private final Token tokenObject;
        private final String token;
        private final byte type;
        private int hashToLookFor;
        private String subToken = null;
        private Node subCache = null;


        private TokenHandler(int inc, Token token) {
            this.inc = inc;
            this.token = token.getToken();
            this.type = token.getType();
            this.tokenObject = token;
        }

        private void addTokenToTokenizeContent() {
            tokenizeContent.getIntArray(TOKENIZED_CONTENT_TYPES).set(inc, type);
            if (type == DELIMITER_TOKEN) {
                tokenizeContent.getStringArray(TOKENIZED_CONTENT_SUBS_DEL).set(inc, token);

                localStatistic.set(LOCALSTATIC_DELIMITER, Type.INT, (int) localStatistic.get(LOCALSTATIC_DELIMITER) + 1);
            } else {
                if (type == NUMBER_TOKEN) {
                    tokenizeContent.getLongArray(TOKENIZED_CONTENT_IDS_NUMB).set(inc, ((NumberT) tokenObject).getLong());
                    localStatistic.set(LOCALSTATIC_NUMBER, Type.INT, (int) localStatistic.get(LOCALSTATIC_NUMBER) + 1);
                } else {
                    LongLongArrayMap masks = (LongLongArrayMap) tokenizeContent.getOrCreate(TOKENIZED_CONTENT_MASKS, Type.LONG_TO_LONG_ARRAY_MAP);
                    int[] mask = ((ContentT) tokenObject).getLowerString().getMask();
                    for (int i = 0; i < mask.length; i++) {
                        masks.put(inc, mask[i]);
                    }
                    hashToLookFor = HashHelper.hash(token);
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
                updateTokenIndex(enodeId);
            }
        }

        private void updateTokenIndex(int indexEnode) {
            //EGraph radix = index.getEGraph(INDEXING_NODE_RADIX_TREE);
            //ENode indexingENode = radix.node(indexEnode);
            //long ii = (long) indexingENode.get(EGRAPH_TOKEN_INVERTED_INDEX);
            tokenizeContent.getLongArray(TOKENIZED_CONTENT_IDS_NUMB).set(inc, indexEnode);
            IntIntMap mapOfAppearance = localStatistic.getIntIntMap(LOCALSTATIC_MAP);
            int count = 1;
            int old = mapOfAppearance.get(indexEnode);
            if (old != Constants.NULL_INT) {
                count += old;
            }
            mapOfAppearance.put(indexEnode, count);
            //addEntryToInvertedIndex(graph, currentWorld, currentTime, ii, tokenizeContent.id());
        }

        private void addEntryToInvertedIndex(long invertedIndexId, long tokenizeContentId) {
            graph.lookup(currentWorld, currentTime, invertedIndexId, result -> {
                LongArray array = result.getLongArray(INVERTED_INDEX_NODE_LIST);
                long[] myarray = array.extract();
                int position = Arrays.binarySearch(myarray, tokenizeContentId);
                if (position == myarray.length) {
                    array.addElement(tokenizeContentId);
                } else {
                    if (position < 0) {
                        position = -position - 1;
                        array.insertElementAt(position, tokenizeContentId);
                    }
                }
                result.free();
            });
        }

        private void updateOrCreateGraphTokenFromCache() {
            subToken = (token.length() > 2) ? token.substring(0, 3) : "less";

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

            if (data.length == 0 || data.length / 3 < CACHE_THRESHOLD - 1) {
                createUpdateCacheEntry();
            } else {
                int enode = turnCacheIntoIndex(data);
                updateTokenIndex(enode);
            }
        }

        private void createSubCacheNode() {
            Node node = graph.newNode(0, BEGINNING_OF_TIME);
            node.set(NODE_NAME, Type.STRING, subToken);
            node.set(NODE_TYPE, Type.INT, SUB_CACHING_NODE);
            node.setTimeSensitivity(-1, 0);
            node.getOrCreate(CACHE_MAP_HASH_ID, Type.LONG_TO_LONG_ARRAY_MAP);
            subCache = node;
        }

        private void createUpdateCacheEntry() {
            LongLongArrayMap map = subCache.getLongLongArrayMap(CACHE_MAP_HASH_ID);
            map.putNoCheck(hashToLookFor, currentWorld);
            map.putNoCheck(hashToLookFor, currentTime);
            map.putNoCheck(hashToLookFor, tokenizeContent.id());
            tokenizeContent.getStringArray(TOKENIZED_CONTENT_SUBS_DEL).set(inc, token);
            localStatistic.set(LOCALSTATIC_CACHE, Type.INT, (int) localStatistic.get(LOCALSTATIC_CACHE) + 1);
        }

        private int turnCacheIntoIndex(long[] data) {

            IntIntMap mapIndex = index.getIntIntMap(INDEXING_NODE_MAP_HASH_ID);
            EGraph radix = index.getEGraph(INDEXING_NODE_RADIX_TREE);
            RadixTree radixTree = new RadixTree(radix);

            int radixId = radixTree.getOrCreate(token);

            mapIndex.put(hashToLookFor, radixId);

            createInvertedIndexEntry(data, radixId);

            LongLongArrayMap map = subCache.getLongLongArrayMap(CACHE_MAP_HASH_ID);
            for (int i = 0; i < data.length; i++) {
                map.delete(hashToLookFor, data[i]);
            }
            return radixId;
        }

        private void createInvertedIndexEntry(long[] data, int radixId) {


            //Node[] node = {graph.newNode(0, BEGINNING_OF_TIME)};
            //radixEntry.set(EGRAPH_TOKEN_INVERTED_INDEX, Type.LONG, node[0].id());

            //node[0].setTimeSensitivity(-1, 0);
            //node[0].set(NODE_TYPE, Type.INT, INVERTED_INDEX);
            //LongArray nodesList = (LongArray) node[0].getOrCreate(INVERTED_INDEX_NODE_LIST, Type.LONG_ARRAY);
            long[] worlds = new long[data.length / 3];
            long[] times = new long[data.length / 3];
            long[] idNodes = new long[data.length / 3];
            for (int i = 0; i < data.length; i += 3) {
                idNodes[i / 3] = data[i];
                times[i / 3] = data[i + 1];
                worlds[i / 3] = data[i + 2];
                //iDs[i] = time;

            }
            updateEToken(idNodes, times, worlds, radixId);
            //Arrays.sort(iDs);
            //nodesList.addAll(iDs);
        }

        private void updateEToken(long[] idNodes, long[] times, long[] worlds, int newId) {
            graph.lookupBatch(worlds, times, idNodes, result -> {
                for (int i = 0; i < result.length; i++) {
                    int[] types = result[i].getIntArray(TOKENIZED_CONTENT_TYPES).extract();
                    for (int j = 0; j < types.length; j++) {
                        if (types[j] == CONTENT_TOKEN) {
                            String sub = result[i].getStringArray(TOKENIZED_CONTENT_SUBS_DEL).get(j);
                            if (sub.equals(token)) {
                                result[i].getStringArray(TOKENIZED_CONTENT_SUBS_DEL).set(j, "-");
                                result[i].getLongArray(TOKENIZED_CONTENT_IDS_NUMB).set(j, newId);
                                break;
                            }
                        }
                    }
                    result[i].relation(RELATION_TOKENIZECONTENT_TO_LOCAL_STAT, ls -> {
                        ls[0].set(LOCALSTATIC_CACHE, Type.INT, (int) ls[0].get(LOCALSTATIC_CACHE) - 1);
                        IntIntMap mapOfAppearance = ls[0].getIntIntMap(LOCALSTATIC_MAP);
                        int count = 1;
                        int old = mapOfAppearance.get(newId);
                        if (old != Constants.NULL_INT) {
                            count += old;
                        }
                        mapOfAppearance.put(newId, count);
                        ls[0].free();
                    });
                    result[i].free();
                }

            });
        }

    }

    public static Task updateOrCreateTokenizeContent(List<Token> tokens, String nameOfContent, String fatherNodeVar) {
        return newTask()
                .ifThen(ctx -> ctx.variable("vocabNodes") == null,
                        accessVocabulary()
                                //Retrieving for both their cache and index node
                                .traverse(RELATION_INDEXED_MAIN_NODES_CACHE_INDEX)
                                .defineAsGlobalVar("vocabNodes")
                )
                .thenDo(ctx -> {
                    long currentWorld = ctx.world();
                    long currentTime = ctx.time();
                    Graph graph = ctx.graph();
                    Node cache = (Node) ctx.variable("vocabNodes").get(0);
                    Node index = (Node) ctx.variable("vocabNodes").get(1);

                    TokenizedContent tc = new TokenizedContent(graph, currentWorld, currentTime, index, cache);

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
