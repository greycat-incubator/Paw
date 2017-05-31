package paw.greycat;

import greycat.*;
import greycat.struct.*;
import greycat.utility.HashHelper;
import paw.PawConstants;
import paw.greycat.struct.radix.RadixTree;
import paw.tokenizer.token.ContentT;
import paw.tokenizer.token.Token;

import java.util.List;

import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Constants.END_OF_TIME;
import static greycat.Tasks.newTask;
import static paw.PawConstants.*;
import static paw.greycat.VocabularyAccess.accessVocabulary;


public class TokenizedContent {

    public static Task updateOrCreateTokenizeContent(List<Token> tokens, String nameOfContent, String fatherNodeVar) {
        return accessVocabulary()
                //Retrieving the number and text main node
                .pipe(newTask().traverse(RELATION_INDEXED_VOCABULARY_CHILDREN, NODE_TYPE, String.valueOf(NUMBER_MAIN_NODE)),
                        newTask().traverse(RELATION_INDEXED_VOCABULARY_CHILDREN, NODE_TYPE, String.valueOf(TEXT_MAIN_NODE)))
                .flat()
                //Retrieving for both their cache and index node
                .pipe(newTask().traverse(RELATION_INDEXED_MAIN_NODES_CACHE_INDEX, NODE_TYPE, String.valueOf(CACHING_NODE)),
                        newTask().traverse(RELATION_INDEXED_MAIN_NODES_CACHE_INDEX, NODE_TYPE, String.valueOf(INDEXING_NODE)))
                .flat()
                .thenDo(ctx -> {
                    long currentWorld = ctx.world();
                    long currentTime = ctx.time();
                    Graph graph = ctx.graph();

                    Node cacheNumber = ctx.resultAsNodes().get(0);
                    Node indexNumber = ctx.resultAsNodes().get(2);
                    Node cacheText = ctx.resultAsNodes().get(1);
                    Node indexText = ctx.resultAsNodes().get(3);

                    //Retrieving the father node
                    Node[] fatherNode = {synchronizeFatherInTime(ctx, fatherNodeVar, currentTime)};

                    //Get the indexed relation of the father containing all of its tokenize content
                    RelationIndexed re = (RelationIndexed) fatherNode[0].getOrCreate(RELATION_INDEXED_NODE_TO_TOKENIZECONTENT, Type.RELATION_INDEXED);
                    //check wether the content exist
                    long[] result = re.select(NODE_NAME, nameOfContent);
                    Node[] tokenizedContent = new Node[1];
                    //If not creating it
                    if (result.length == 0) {
                        tokenizedContent[0] = createTokenizeContentNode(graph, fatherNode[0], currentWorld, currentTime, nameOfContent);
                    } else {
                        //otherwise retrieve it and prepare it for the new version
                        graph.lookup(currentWorld, currentTime, result[0], tc -> {

                            purgeFormerVersionOfTokenizeContent(graph, currentWorld, currentTime, tc);
                            tokenizedContent[0] = tc;
                        });
                    }
                    //Add each token to the tokenized content node
                    EGraph tokensEgraph = tokenizedContent[0].getEGraph(TOKENIZED_CONTENT_TOKENS);
                    for (int i = 0; i < tokens.size(); i++) {
                        Token token = tokens.get(i);
                        ENode enode = tokensEgraph.newNode();
                        boolean number = token.getType() == PawConstants.NUMBER_TOKEN;
                        if (number) {
                            addTokenToTokenizeContent(indexNumber, cacheNumber, graph, currentWorld, currentTime, token, enode, tokenizedContent[0]);
                        } else {
                            if (token.getType() == CONTENT_TOKEN) {
                                IntArray mask = (IntArray) enode.getOrCreate(EGRAPH_TOKEN_MASK, Type.INT_ARRAY);
                                mask.initWith(((ContentT) token).getLowerString().getMask());
                            }
                            addTokenToTokenizeContent(indexText, cacheText, graph, currentWorld, currentTime, token, enode, tokenizedContent[0]);
                        }

                    }
                    tokenizedContent[0].free();
                    ctx.continueTask();
                });
    }

    private static Node synchronizeFatherInTime(TaskContext ctx, String fatherNodeVar, long currentTime) {
        Node[] father = {(Node) ctx.variable(fatherNodeVar).get(0)};

        //Update the father time if necessary
        if (father[0].time() != currentTime) {
            if (father[0].time() > currentTime) {
                ctx.endTask(ctx.result(), new RuntimeException("node is in the future"));
            } else {
                father[0].travelInTime(currentTime, result -> {
                    father[0].free();
                    father[0] = result;
                    ctx.setVariable(fatherNodeVar, result);

                });
            }
        }
        return father[0];
    }

    private static Node createTokenizeContentNode(Graph graph, Node fatherNode, long currentWorld, long currentTime, String nameOfContent) {
        Node tc = graph.newNode(currentWorld, currentTime);
        tc.set(NODE_NAME, Type.STRING, nameOfContent);
        tc.set(NODE_TYPE, Type.INT, TOKENIZED_CONTENT);
        tc.getOrCreate(TOKENIZED_CONTENT_TOKENS, Type.EGRAPH);
        tc.addToRelation(RELATION_TOKENIZECONTENT_TO_NODE, fatherNode);
        fatherNode.addToRelation(RELATION_INDEXED_NODE_TO_TOKENIZECONTENT, tc, NODE_NAME);
        return tc;
    }

    private static void purgeFormerVersionOfTokenizeContent(Graph graph, long currentWorld, long currentTime, Node tokenizeContent) {
        EGraph tokens = tokenizeContent.getEGraph(TOKENIZED_CONTENT_TOKENS);
        for (int i = 0; i < tokens.size(); i++) {
            ENode enode = tokens.node(i);
            if (enode.get(EGRAPH_TOKEN_INVERTED_INDEX) != null) {
                long iiId = (long) enode.get(EGRAPH_TOKEN_INVERTED_INDEX);
                graph.lookup(currentWorld, currentTime, iiId, ii -> {
                    LongLongMap longLongMap = ii.getLongLongMap(INVERTED_INDEX_NODE_MAP);
                    longLongMap.remove(tokenizeContent.id());
                    ii.free();
                });
                graph.lookupTimes(currentWorld, currentTime + 1, END_OF_TIME, iiId, -1, result -> {
                    for (int i1 = 0; i1 < result.length; i1++) {
                        LongLongMap longLongMap = result[i1].getLongLongMap(INVERTED_INDEX_NODE_MAP);
                        longLongMap.remove(tokenizeContent.id());
                        result[i1].free();
                    }
                });
            }
        }
        tokens.free();
    }

    private static void addTokenToTokenizeContent(Node index, Node cache, Graph graph, long currentWorld, long currentTime, Token token, ENode etoken, Node tokenizeContent) {
        etoken.set(EGRAPH_TOKEN_TYPE, Type.INT, (int) token.getType());
        if (token.getType() == DELIMITER_TOKEN) {
            etoken.set(EGRAPH_TOKEN_CONTENT, Type.STRING, token.getToken());
        } else {
            addIndexableTokenToTokenizeContent(index, cache, graph, currentWorld, currentTime, token, etoken, tokenizeContent);
        }
    }

    private static void addIndexableTokenToTokenizeContent(Node index, Node cache, Graph graph, long currentWorld, long currentTime, Token token, ENode etoken, Node tokenizeContent) {
        int hashToLookFor = HashHelper.hash(token.getToken());
        IntIntMap map = index.getIntIntMap(INDEXING_NODE_MAP_HASH_ID);
        int enode = map.get(hashToLookFor);
        if (enode == Constants.NULL_INT) {
            updateOrCreateGraphTokenFromCache(index, cache, graph, token, hashToLookFor, currentTime, tokenizeContent, etoken);
        } else {
            updateTokenIndex(graph, currentTime, index, enode, etoken, tokenizeContent);
        }

    }

    private static void updateTokenIndex(Graph graph, long currentTime, Node indexing, int enode, ENode etoken, Node tokenizeContent) {
        EGraph radix = indexing.getEGraph(INDEXING_NODE_RADIX_TREE);
        ENode indexingENode = radix.node(enode);
        long ii = (long) indexingENode.get(EGRAPH_TOKEN_INVERTED_INDEX);
        etoken.set(EGRAPH_TOKEN_ID, Type.INT, enode);
        etoken.set(EGRAPH_TOKEN_INVERTED_INDEX, Type.LONG, ii);
        addEntryToInvertedIndex(graph, currentTime, ii, tokenizeContent.id());
    }

    private static void addEntryToInvertedIndex(Graph graph, long currentTime, long invertedIndexId, long tokenizeContent) {
        long[] count = {1};
        graph.lookup(0, currentTime, invertedIndexId, result -> {
            LongLongMap longLongMap = result.getLongLongMap(INVERTED_INDEX_NODE_MAP);
            if (longLongMap.get(tokenizeContent) != Constants.NULL_LONG) {
                count[0] += longLongMap.get(tokenizeContent);
            }
            longLongMap.put(tokenizeContent, count[0]);
            result.free();
        });
        graph.lookupTimes(0, currentTime + 1, END_OF_TIME, invertedIndexId, -1, iis -> {
            for (int j = 0; j < iis.length; j++) {
                LongLongMap longLongMap = iis[j].getLongLongMap(INVERTED_INDEX_NODE_MAP);
                longLongMap.put(tokenizeContent, count[0]);
                iis[j].free();
            }
        });
    }

    private static void updateOrCreateGraphTokenFromCache(Node index, Node cache, Graph graph, Token token, int hashToLookFor, long currentTime, Node tokenizeContent, ENode eToken) {
        String tokenToFind = token.getToken();
        String subToken = (tokenToFind.length() > 2) ? tokenToFind.substring(0, 3) : "less";
        RelationIndexed relationIndexed = (RelationIndexed) cache.getOrCreate(RELATION_INDEXED_CACHE_TO_SUBCACHE, Type.RELATION_INDEXED);
        long[] subcacheId = relationIndexed.select(NODE_NAME, subToken);
        final Node[] subCache = new Node[1];
        if (subcacheId.length == 0) {
            subCache[0] = createSubCacheNode(graph, subToken);
            relationIndexed.add(subCache[0], NODE_NAME);
        } else {
            graph.lookup(0, BEGINNING_OF_TIME, subcacheId[0], result -> subCache[0] = result);
        }

        IntIntMap map = subCache[0].getIntIntMap(CACHE_MAP_HASH_ID);
        int entry = map.get(hashToLookFor);
        if (entry == Constants.NULL_INT) {
            createCacheEntry(subCache[0], currentTime, tokenizeContent.id(), tokenToFind, hashToLookFor, eToken, subToken);
        } else {
            updateCacheEntry(index, cache, graph, subCache[0], entry, currentTime, tokenizeContent, eToken, subToken, tokenToFind, hashToLookFor);
        }
        subCache[0].free();
    }

    private static Node createSubCacheNode(Graph graph, String sub) {
        Node node = graph.newNode(0, BEGINNING_OF_TIME);
        node.set(NODE_NAME, Type.STRING, sub);
        node.set(NODE_TYPE, Type.INT, SUB_CACHING_NODE);
        node.setTimeSensitivity(-1, 0);
        node.getOrCreate(CACHE_MAP_HASH_ID, Type.INT_TO_INT_MAP);
        node.getOrCreate(CACHE_EGRAPH, Type.EGRAPH);
        return node;
    }

    private static void createCacheEntry(Node subCache, long currentTime, long tokenizeContentId, String tokenToFind, int hashToLookFor, ENode eToken, String subToken) {
        EGraph egraph = subCache.getEGraph(CACHE_EGRAPH);
        IntIntMap map = subCache.getIntIntMap(CACHE_MAP_HASH_ID);
        ENode entry = egraph.newNode();
        entry.set(CACHING_ENODE_COUNT, Type.INT, 1);
        LongArray longArray = (LongArray) entry.getOrCreate(CACHING_ENODE_WHEN_WHERE, Type.LONG_ARRAY);
        longArray.initWith(new long[]{currentTime, tokenizeContentId});
        entry.set(CACHING_ENODE_CONTENT, Type.STRING, tokenToFind);
        map.put(hashToLookFor, entry.id());
        eToken.set(EGRAPH_TOKEN_CACHE_SUB, Type.STRING, subToken);
        eToken.set(EGRAPH_TOKEN_ID, Type.INT, entry.id());
    }

    private static void updateCacheEntry(Node index, Node cache, Graph graph, Node subCache, int entryId, long currentTime, Node tokenizeContent, ENode eToken, String subToken, String token, int hashToLookFor) {
        EGraph egraph = subCache.getEGraph(CACHE_EGRAPH);
        ENode eNode = egraph.node(entryId);
        int count = (int) eNode.get(CACHING_ENODE_COUNT);
        if (count != CACHE_THRESHOLD - 1) {
            eNode.set(CACHING_ENODE_COUNT, Type.INT, count + 1);
            LongArray longArray = eNode.getLongArray(CACHING_ENODE_WHEN_WHERE);
            longArray.addAll(new long[]{currentTime, tokenizeContent.id()});
            eToken.set(EGRAPH_TOKEN_CACHE_SUB, Type.STRING, subToken);
            eToken.set(EGRAPH_TOKEN_ID, Type.INT, entryId);
        } else {
            int enode = turnCacheIntoIndex(index, cache, graph, subCache, egraph, eNode, token, hashToLookFor, subToken);
            updateTokenIndex(graph, currentTime, index, enode, eToken, tokenizeContent);
        }
    }

    private static int turnCacheIntoIndex(Node index, Node cache, Graph graph, Node subCache, EGraph egraph, ENode eNodeCache, String token, int hashToLookFor, String subToken) {
        IntIntMap mapOfCache = subCache.getIntIntMap(CACHE_MAP_HASH_ID);
        IntIntMap mapIndex = index.getIntIntMap(INDEXING_NODE_MAP_HASH_ID);
        EGraph radix = index.getEGraph(INDEXING_NODE_RADIX_TREE);
        RadixTree radixTree = new RadixTree(radix);
        int radixId = radixTree.getOrCreate(token);
        ENode eNodeIndex = radix.node(radixId);
        mapIndex.put(hashToLookFor, radixId);
        createInvertedIndexEntry(graph, eNodeCache, eNodeIndex, subToken);
        ENode replacementENode = egraph.node(mapOfCache.size() - 1);
        if (replacementENode.id() != eNodeCache.id()) {
            mapOfCache.each((key, value) -> {
                if (value == replacementENode.id()) {
                    mapOfCache.put(key, eNodeCache.id());
                }
            });
        }
        mapOfCache.remove(hashToLookFor);
        egraph.drop(eNodeCache);
        return radixId;
    }

    private static void createInvertedIndexEntry(Graph graph, ENode eNodeCache, ENode eNodeIndex, String subToken) {
        Node[] node = {graph.newNode(0, BEGINNING_OF_TIME)};
        eNodeIndex.set(EGRAPH_TOKEN_INVERTED_INDEX, Type.LONG, node[0].id());
        node[0].getOrCreate(INVERTED_INDEX_NODE_MAP, Type.LONG_TO_LONG_MAP);
        LongArray longArray = eNodeCache.getLongArray(CACHING_ENODE_WHEN_WHERE);
        for (int i = 0; i < longArray.size(); i += 2) {
            long idNode = longArray.get(i + 1);
            long time = longArray.get(i);
            long[] count = {1};
            node[0].travelInTime(time, result -> {
                LongLongMap longLongMap = result.getLongLongMap(INVERTED_INDEX_NODE_MAP);
                if (longLongMap.get(idNode) != Constants.NULL_LONG) {
                    count[0] += longLongMap.get(idNode);
                }
                longLongMap.put(idNode, count[0]);
                result.free();
            });
            graph.lookupTimes(0, time + 1, END_OF_TIME, node[0].id(), -1, result -> {
                for (int j = 0; j < result.length; j++) {
                    LongLongMap longLongMap = result[j].getLongLongMap(INVERTED_INDEX_NODE_MAP);
                    longLongMap.put(idNode, count[0]);
                    result[j].free();
                }
            });
            updateEToken(graph, idNode, time, eNodeCache.id(), eNodeIndex.id(), node[0].id(), subToken);
        }
    }

    private static void updateEToken(Graph graph, long node, long time, int formerId, int newId, long iiId, String subToken) {
        graph.lookup(0, time, node, result -> {
            EGraph tokens = result.getEGraph(TOKENIZED_CONTENT_TOKENS);
            for (int i = 0; i < tokens.size(); i++) {
                ENode token = tokens.node(i);
                if ((token.get(EGRAPH_TOKEN_CACHE_SUB) == subToken) && ((int) token.get(EGRAPH_TOKEN_ID) == formerId)) {
                    token.remove(EGRAPH_TOKEN_CACHE_SUB);
                    token.set(EGRAPH_TOKEN_ID, Type.INT, newId);
                    token.set(EGRAPH_TOKEN_INVERTED_INDEX, Type.LONG, iiId);
                    break;
                }
            }
            result.free();
        });
    }
}
