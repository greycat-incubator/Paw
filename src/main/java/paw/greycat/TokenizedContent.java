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
        tc.getOrCreate(TOKENIZED_CONTENT_TOKENS, Type.EGRAPH);
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
        EGraph tokensEgraph = tokenizeContent.getEGraph(TOKENIZED_CONTENT_TOKENS);
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            ENode enode = tokensEgraph.newNode();
            TokenHandler th = new TokenHandler(enode, token);
            th.addTokenToTokenizeContent();
            th.free();
        }
    }

    class TokenHandler {
        private final ENode tokenEnode;
        private final Token tokenObject;
        private final String token;
        private final byte type;
        private int hashToLookFor;
        private String subToken = null;
        private Node subCache = null;


        private TokenHandler(ENode tokenEnode, Token token) {
            this.tokenEnode = tokenEnode;
            this.token = token.getToken();
            this.type = token.getType();
            this.tokenObject = token;
        }

        private void addTokenToTokenizeContent() {
            tokenEnode.set(EGRAPH_TOKEN_TYPE, Type.INT, (int) type);
            if (type == DELIMITER_TOKEN) {
                tokenEnode.set(EGRAPH_TOKEN_CONTENT, Type.STRING, token);
                localStatistic.set(LOCALSTATIC_DELIMITER, Type.INT, (int) localStatistic.get(LOCALSTATIC_DELIMITER) + 1);
            } else {
                if (type == NUMBER_TOKEN) {
                    tokenEnode.set(EGRAPH_TOKEN_CONTENT, Type.LONG, ((NumberT) tokenObject).getLong());
                    localStatistic.set(LOCALSTATIC_NUMBER, Type.INT, (int) localStatistic.get(LOCALSTATIC_NUMBER) + 1);
                } else {
                    IntArray mask = (IntArray) tokenEnode.getOrCreate(EGRAPH_TOKEN_MASK, Type.INT_ARRAY);
                    mask.initWith(((ContentT) tokenObject).getLowerString().getMask());
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
            tokenEnode.set(EGRAPH_TOKEN_ID, Type.INT, indexEnode);
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

            IntIntMap map = subCache.getIntIntMap(CACHE_MAP_HASH_ID);
            int entry = map.get(hashToLookFor);

            if (entry == Constants.NULL_INT) {
                createCacheEntry();
            } else {
                updateCacheEntry(entry);
            }
        }

        private void createSubCacheNode() {
            Node node = graph.newNode(0, BEGINNING_OF_TIME);
            node.set(NODE_NAME, Type.STRING, subToken);
            node.set(NODE_TYPE, Type.INT, SUB_CACHING_NODE);
            node.setTimeSensitivity(-1, 0);
            node.getOrCreate(CACHE_MAP_HASH_ID, Type.INT_TO_INT_MAP);
            node.getOrCreate(CACHE_EGRAPH, Type.EGRAPH);
            subCache = node;
        }

        private void createCacheEntry() {
            EGraph egraph = subCache.getEGraph(CACHE_EGRAPH);
            IntIntMap map = subCache.getIntIntMap(CACHE_MAP_HASH_ID);

            ENode entry = egraph.newNode();
            entry.set(CACHING_ENODE_COUNT, Type.INT, 1);

            LongArray longArray = (LongArray) entry.getOrCreate(CACHING_ENODE_WHEN_WHERE, Type.LONG_ARRAY);
            longArray.initWith(new long[]{currentWorld, currentTime, tokenizeContent.id()});

            entry.set(CACHING_ENODE_CONTENT, Type.STRING, token);

            map.put(hashToLookFor, entry.id());

            tokenEnode.set(EGRAPH_TOKEN_CACHE_SUB, Type.STRING, subToken);
            tokenEnode.set(EGRAPH_TOKEN_ID, Type.INT, hashToLookFor);
            localStatistic.set(LOCALSTATIC_CACHE, Type.INT, (int) localStatistic.get(LOCALSTATIC_CACHE) + 1);
        }

        private void updateCacheEntry(int cacheEntryId) {
            EGraph egraph = subCache.getEGraph(CACHE_EGRAPH);
            ENode cacheEntry = egraph.node(cacheEntryId);

            int count = (int) cacheEntry.get(CACHING_ENODE_COUNT);

            if (count != CACHE_THRESHOLD - 1) {
                cacheEntry.set(CACHING_ENODE_COUNT, Type.INT, count + 1);
                LongArray longArray = cacheEntry.getLongArray(CACHING_ENODE_WHEN_WHERE);
                longArray.addAll(new long[]{currentWorld, currentTime, tokenizeContent.id()});

                tokenEnode.set(EGRAPH_TOKEN_CACHE_SUB, Type.STRING, subToken);
                tokenEnode.set(EGRAPH_TOKEN_ID, Type.INT, hashToLookFor);
                localStatistic.set(LOCALSTATIC_CACHE, Type.INT, (int) localStatistic.get(LOCALSTATIC_CACHE) + 1);
            } else {
                int enode = turnCacheIntoIndex(cacheEntryId);
                updateTokenIndex(enode);
            }
        }

        private int turnCacheIntoIndex(int cacheEntryId) {
            IntIntMap mapOfCache = subCache.getIntIntMap(CACHE_MAP_HASH_ID);
            EGraph egraphCache = subCache.getEGraph(CACHE_EGRAPH);
            ENode cacheEntry = egraphCache.node(cacheEntryId);


            IntIntMap mapIndex = index.getIntIntMap(INDEXING_NODE_MAP_HASH_ID);
            EGraph radix = index.getEGraph(INDEXING_NODE_RADIX_TREE);
            RadixTree radixTree = new RadixTree(radix);

            int radixId = radixTree.getOrCreate(token);

            mapIndex.put(hashToLookFor, radixId);

            createInvertedIndexEntry(cacheEntryId, radixId);
            ENode replacementENode = egraphCache.node(mapOfCache.size() - 1);
            if (replacementENode.id() != cacheEntry.id()) {
                mapOfCache.each((key, value) -> {
                    if (value == replacementENode.id()) {
                        mapOfCache.put(key, cacheEntry.id());
                    }
                });
            }
            mapOfCache.remove(hashToLookFor);
            egraphCache.drop(cacheEntry);
            return radixId;
        }

        private void createInvertedIndexEntry(int cacheEntryId, int radixId) {
            EGraph egraphCache = subCache.getEGraph(CACHE_EGRAPH);
            ENode cacheEntry = egraphCache.node(cacheEntryId);

            //Node[] node = {graph.newNode(0, BEGINNING_OF_TIME)};
            //radixEntry.set(EGRAPH_TOKEN_INVERTED_INDEX, Type.LONG, node[0].id());

            //node[0].setTimeSensitivity(-1, 0);
            //node[0].set(NODE_TYPE, Type.INT, INVERTED_INDEX);
            //LongArray nodesList = (LongArray) node[0].getOrCreate(INVERTED_INDEX_NODE_LIST, Type.LONG_ARRAY);
            LongArray whenwhere = cacheEntry.getLongArray(CACHING_ENODE_WHEN_WHERE);
            for (int i = 0; i < whenwhere.size(); i += 3) {
                long idNode = whenwhere.get(i + 2);
                long time = whenwhere.get(i + 1);
                long world = whenwhere.get(i);
                //iDs[i] = time;
                updateEToken(idNode, time, world, radixId);
            }
            //Arrays.sort(iDs);
            //nodesList.addAll(iDs);
        }

        private void updateEToken(long idNode, long time, long world, int newId) {
            graph.lookup(world, time, idNode, result -> {
                EGraph tokens = result.getEGraph(TOKENIZED_CONTENT_TOKENS);
                for (int i = 0; i < tokens.size(); i++) {
                    ENode etoken = tokens.node(i);
                    String sub = (String) etoken.get(EGRAPH_TOKEN_CACHE_SUB);
                    if(sub!=null && sub.equals(subToken)){
                        int hash = (int) etoken.get(EGRAPH_TOKEN_ID);
                        if(hash == hashToLookFor){
                            etoken.remove(EGRAPH_TOKEN_CACHE_SUB);
                            etoken.set(EGRAPH_TOKEN_ID, Type.INT, newId);
                            break;
                        }
                    }
                }
                result.relation(RELATION_TOKENIZECONTENT_TO_LOCAL_STAT, ls -> {
                    ls[0].set(LOCALSTATIC_CACHE, Type.INT, (int) ls[0].get(LOCALSTATIC_CACHE) - 1);
                    IntIntMap mapOfAppearance = ls[0].getIntIntMap(LOCALSTATIC_MAP);
                    int count = 1;
                    int old = mapOfAppearance.get(newId);
                    if (old != Constants.NULL_INT) {
                        count += old;
                    }
                    mapOfAppearance.put(newId, count);
                    ls[0].free();
                    result.free();
                });
            });
        }

    }

    public static Task updateOrCreateTokenizeContent(List<Token> tokens, String nameOfContent, String fatherNodeVar) {
        return accessVocabulary()
                //Retrieving for both their cache and index node
                .traverse(RELATION_INDEXED_MAIN_NODES_CACHE_INDEX)
                .thenDo(ctx -> {
                    long currentWorld = ctx.world();
                    long currentTime = ctx.time();
                    Graph graph = ctx.graph();
                    Node cache = ctx.resultAsNodes().get(0);
                    Node index = ctx.resultAsNodes().get(1);


                    TokenizedContent tc = new TokenizedContent(graph, currentWorld, currentTime, index, cache);

                    //Retrieving the father node
                    tc.synchronizeFatherInTime(ctx, currentTime, fatherNodeVar);

                    //Get the indexed relation of the father containing all of its tokenize content
                    tc.getOrCreateTokenizeContentNode(nameOfContent);

                    tc.addTokens(tokens);
                    tc.free();
                    //Add each token to the tokenized content node
                    ctx.continueWith(ctx.newResult());
                });
    }


}
