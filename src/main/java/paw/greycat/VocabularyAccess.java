package paw.greycat;

import greycat.*;

import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static paw.PawConstants.*;

/**
 * Class providing a Task to access the vocabulary Node and creating it in case it doesn't exist yet.
 */
public class VocabularyAccess {

    /**
     * Task to retrieve the Vocabulary Node and put it in the current context result
     *
     * @return Task
     */
    public static Task accessVocabulary() {
        return newTask()
                .thenDo(
                        ctx -> {
                            Graph graph = ctx.graph();
                            graph.index(0, BEGINNING_OF_TIME, RELATION_INDEX_ENTRY_POINT, nodeIndex ->
                                    nodeIndex.find(result -> {

                                        Node vocabulary = null;
                                        //Vocabulary doesn't exist
                                        if (result.length == 0) {
                                            vocabulary = initializeVocabulary(graph, nodeIndex);
                                        } else {
                                            //Vocabulary exist
                                            if (result.length == 1) {
                                                vocabulary = result[0];
                                            } else {
                                                ctx.endTask(ctx.result(), new RuntimeException("more than on vocabulary Node"));
                                            }
                                        }
                                        //Freeing entry point node
                                        nodeIndex.free();
                                        ctx.continueWith(ctx.wrap(vocabulary));
                                    }, NODE_TYPE, String.valueOf(VOCABULARY_NODE)));
                        });
    }

    /**
     * Function creating the Vocabulary Node
     * @param graph to create the node in
     * @param gindexNode entry point in the graph
     * @return the vocabulary Node
     */
    private static Node createVocabulary(Graph graph, NodeIndex gindexNode) {
        Node vocabulary = graph.newNode(0, BEGINNING_OF_TIME);
        vocabulary.set(NODE_TYPE, Type.INT, VOCABULARY_NODE);
        vocabulary.setTimeSensitivity(-1, 0);
        gindexNode.addToIndex(vocabulary, NODE_TYPE);
        return vocabulary;
    }

    /**
     * Function creating A main Node, i.e., the text or number main Node
     * @param graph to create the node in
     * @param vocabulary node
     * @param number is the main node to create the number one  (true) or the text one (false)
     * @return the created main Node
     */
    private static Node createMainNode(Graph graph, Node vocabulary, boolean number) {
        Node mainNode = graph.newNode(0, BEGINNING_OF_TIME);
        if (number) {
            mainNode.set(NODE_TYPE, Type.INT, NUMBER_MAIN_NODE);
        } else {
            mainNode.set(NODE_TYPE, Type.INT, TEXT_MAIN_NODE);
        }
        mainNode.setTimeSensitivity(-1, 0);
        vocabulary.addToRelation(RELATION_INDEXED_VOCABULARY_CHILDREN, mainNode, NODE_TYPE);
        return mainNode;
    }

    /**
     * Function creating the cache node of a main node
     * @param graph to create the node in
     * @param mainNode the cache will relate to
     */
    private static void createCache(Graph graph, Node mainNode) {
        Node cachingNode = graph.newNode(0, BEGINNING_OF_TIME);
        cachingNode.setTimeSensitivity(-1, 0);
        cachingNode.set(NODE_TYPE, Type.INT, CACHING_NODE);
        cachingNode.getOrCreate(RELATION_INDEXED_CACHE_TO_SUBCACHE, Type.RELATION_INDEXED);
        mainNode.addToRelation(RELATION_INDEXED_MAIN_NODES_CACHE_INDEX, cachingNode, NODE_TYPE);
        cachingNode.free();
    }

    /**
     *Function creating the indexing node of a main node
     * @param graph to create the node in
     * @param mainNode the index will relate to
     */
    private static void createIndex(Graph graph, Node mainNode) {
        Node indexingNode = graph.newNode(0, BEGINNING_OF_TIME);
        indexingNode.setTimeSensitivity(-1, 0);
        indexingNode.set(NODE_TYPE, Type.INT, INDEXING_NODE);
        mainNode.addToRelation(RELATION_INDEXED_MAIN_NODES_CACHE_INDEX, indexingNode, NODE_TYPE);
        indexingNode.getOrCreate(INDEXING_NODE_RADIX_TREE, Type.EGRAPH);
        indexingNode.getOrCreate(INDEXING_NODE_MAP_HASH_ID, Type.INT_TO_INT_MAP);
        indexingNode.free();
    }

    /**
     * Function initializing the vocabulary
     * @param graph to create the vocabulary in
     * @param gindexNode entry point in the graph
     * @return the vocabulary node
     */
    private static Node initializeVocabulary(Graph graph, NodeIndex gindexNode) {
        Node vocabulary = createVocabulary(graph, gindexNode);

        Node numberMain = createMainNode(graph, vocabulary, true);
        createCache(graph, numberMain);
        createIndex(graph, numberMain);
        numberMain.free();

        Node textMain = createMainNode(graph, vocabulary, false);
        createCache(graph, textMain);
        createIndex(graph, textMain);
        textMain.free();

        return vocabulary;
    }
}
