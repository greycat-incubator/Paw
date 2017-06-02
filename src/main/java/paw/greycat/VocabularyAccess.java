package paw.greycat;

import greycat.Graph;
import greycat.Node;
import greycat.Task;
import greycat.Type;

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
                .readGlobalIndex(RELATION_INDEX_ENTRY_POINT, NODE_TYPE, String.valueOf(VOCABULARY_NODE))
                .ifThen(ctx -> ctx.result().size() == 0,
                        newTask()
                                .thenDo(ctx -> ctx.continueWith(ctx.wrap(initializeVocabulary(ctx.graph()))))
                                .addToGlobalIndex(RELATION_INDEX_ENTRY_POINT, NODE_TYPE)
                );
    }

    /**
     * Function creating the Vocabulary Node
     *
     * @param graph to create the node in
     * @return the vocabulary Node
     */
    private static Node createVocabulary(Graph graph) {
        Node vocabulary = graph.newNode(0, BEGINNING_OF_TIME);
        vocabulary.set(NODE_TYPE, Type.INT, VOCABULARY_NODE);
        vocabulary.setTimeSensitivity(-1, 0);
        return vocabulary;
    }


    /**
     * Function creating the cache node of a main node
     *
     * @param graph      to create the node in
     * @param vocabulary the cache will relate to
     */
    private static void createCache(Graph graph, Node vocabulary) {
        Node cachingNode = graph.newNode(0, BEGINNING_OF_TIME);
        cachingNode.setTimeSensitivity(-1, 0);
        cachingNode.set(NODE_TYPE, Type.INT, CACHING_NODE);
        cachingNode.getOrCreate(RELATION_INDEXED_CACHE_TO_SUBCACHE, Type.RELATION_INDEXED);
        vocabulary.addToRelation(RELATION_VOCAB_CACHE_INDEX_DEL, cachingNode);
        cachingNode.free();
    }

    /**
     * Function creating the indexing node of a main node
     *
     * @param graph      to create the node in
     * @param vocabulary the index will relate to
     */
    private static void createIndex(Graph graph, Node vocabulary) {
        Node indexingNode = graph.newNode(0, BEGINNING_OF_TIME);
        indexingNode.setTimeSensitivity(-1, 0);
        indexingNode.set(NODE_TYPE, Type.INT, INDEXING_NODE);
        vocabulary.addToRelation(RELATION_VOCAB_CACHE_INDEX_DEL, indexingNode);
        indexingNode.getOrCreate(INDEXING_NODE_RADIX_TREE, Type.EGRAPH);
        indexingNode.getOrCreate(INDEXING_NODE_MAP_HASH_ID, Type.INT_TO_INT_MAP);
        indexingNode.free();
    }

    private static void createDelimiterVocabulary(Graph graph, Node vocabulary) {
        Node delimiterVocab = graph.newNode(0, BEGINNING_OF_TIME);
        delimiterVocab.setTimeSensitivity(-1, 0);
        delimiterVocab.set(NODE_TYPE, Type.INT, DELIMITER_NODE);
        vocabulary.addToRelation(RELATION_VOCAB_CACHE_INDEX_DEL, delimiterVocab);
        delimiterVocab.getOrCreate(DELIMITER_VOCABULARY, Type.INT_TO_STRING_MAP);
        delimiterVocab.free();
    }

    /**
     * Function initializing the vocabulary
     *
     * @param graph to create the vocabulary in
     * @return the vocabulary node
     */
    private static Node initializeVocabulary(Graph graph) {
        Node vocabulary = createVocabulary(graph);
        createCache(graph, vocabulary);
        createIndex(graph, vocabulary);
        createDelimiterVocabulary(graph, vocabulary);

        return vocabulary;
    }


}
