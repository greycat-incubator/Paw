package paw.graph.nodes;

import greycat.*;
import greycat.base.BaseNode;
import greycat.utility.HashHelper;

import static greycat.Constants.BEGINNING_OF_TIME;
import static paw.PawConstants.INDEX_CATEGORY;
import static paw.graph.nodes.TokenizeContentNode.CATEGORY;
import static paw.graph.nodes.TokenizeContentNode.CATEGORY_H;

/**
 * Category Node, extending the base node, this node is the main entrance point to a given category of tokenize content
 */
public class CategoryNode extends BaseNode {
    public final static String NAME = "CATEGORY";

    public final static String VOCABULARY_RELATION = "vocabulary";
    private final static int VOCABULARY_RELATION_H = HashHelper.hash(VOCABULARY_RELATION);

    public final static String DELIMITER_VOCABULARY_RELATION = "delimiterVoc";
    private final static int DELIMITER_VOCABULARY_RELATION_H = HashHelper.hash(DELIMITER_VOCABULARY_RELATION);

    public final static String TC_LIST_RELATION = "tcList";
    private final static int TC_LIST_RELATION_H = HashHelper.hash(TC_LIST_RELATION);

    /**
     * Constructor
     *
     * @param p_world world
     * @param p_time  time
     * @param p_id    id of the node
     * @param p_graph graph
     */
    public CategoryNode(long p_world, long p_time, long p_id, Graph p_graph) {
        super(p_world, p_time, p_id, p_graph);
    }

    /**
     * @return the name of the category
     */
    public final String getCategory() {
        return (String) getAt(CATEGORY_H);
    }

    /**
     * return in a CallBack the node containing the list of all tokenize content belonging to the category
     *
     * @param callback in which the node will be returned
     */
    public final void getTokenizeContentListNode(Callback<TCListNode> callback) {
        traverseAt(TC_LIST_RELATION_H, (Callback<Node[]>) result -> {
            if (result.length > 0) {
                callback.on((TCListNode) result[0]);
            } else throw new RuntimeException("Category Node was not initialized or is not a category Node");
        });
    }

    /**
     * return in a CallBack the node containing all token starting by the given firstchar
     *
     * @param firstChar of the token
     * @param callback  in which the node will be returned
     */
    public final void getVocabularyNodeFor(char firstChar, Callback<VocabularyNode> callback) {
        Index index = (Index) getAt(VOCABULARY_RELATION_H);
        long[] vocId = index.select(String.valueOf(firstChar));
        if (vocId.length == 0) {
            VocabularyNode vocabularyNode = (VocabularyNode) _graph.newTypedNode(0, BEGINNING_OF_TIME, VocabularyNode.NAME);
            vocabularyNode.initVocNode(firstChar);
            index.update(vocabularyNode);
            callback.on(vocabularyNode);
        } else {
            _graph.lookup(0, BEGINNING_OF_TIME, vocId[0], result -> callback.on((VocabularyNode) result));
        }
    }

    /**
     * return in a CallBack the node containing the Delimiter Vocabulary Node
     *
     * @param callback in which the node will be returned
     */
    public final void getDelimiterVocabularyNode(Callback<DelimiterVocabularyNode> callback) {
        traverseAt(DELIMITER_VOCABULARY_RELATION_H, (Callback<Node[]>) result -> {
            if (result.length > 0) {
                callback.on((DelimiterVocabularyNode) result[0]);
            } else throw new RuntimeException("Category Node was not initialized or is not a category Node");
        });

    }

    /**
     * Function to initialize the category node
     *
     * @param category name of the category
     */
    private void initCategory(String category, NodeIndex indexfather, Callback<CategoryNode> callback) {
        set(CATEGORY, Type.STRING, category);
        this.setTimeSensitivity(-1, 0);
        indexfather.update(this);
        indexfather.free();

        TCListNode tcListNode = (TCListNode) _graph.newTypedNode(0, BEGINNING_OF_TIME, TCListNode.NAME);
        tcListNode.initNode();
        addToRelationAt(TC_LIST_RELATION_H, tcListNode);
        tcListNode.free();

        DelimiterVocabularyNode delimiterVocabularyNode = (DelimiterVocabularyNode) _graph.newTypedNode(0, BEGINNING_OF_TIME, DelimiterVocabularyNode.NAME);
        delimiterVocabularyNode.initNode();
        addToRelationAt(DELIMITER_VOCABULARY_RELATION_H, delimiterVocabularyNode);
        delimiterVocabularyNode.free();

        CategoryNode categoryNode = this;

        Index index = (Index) getOrCreateAt(VOCABULARY_RELATION_H, Type.INDEX);
        index.declareAttributes(result -> {
            callback.on(categoryNode);
        }, VocabularyNode.FIRST_CHAR);
    }

    /**
     * Function to retrieve or create a category node
     *
     * @param graph    graph
     * @param category name of the category
     * @param callback in which the node will be returned
     */
    public final static void getOrCreateCategoryNode(Graph graph, String category, Callback<CategoryNode> callback) {
        graph.declareIndex(0, INDEX_CATEGORY,
                index -> index.findFrom(
                        result -> {
                            CategoryNode categoryNode;
                            if (result.length != 0) {
                                categoryNode = (CategoryNode) result[0];
                                index.free();
                                callback.on(categoryNode);
                            } else {
                                categoryNode = (CategoryNode) graph.newTypedNode(0, BEGINNING_OF_TIME, CategoryNode.NAME);
                                categoryNode.initCategory(category, index, callback);
                            }
                        },
                        category), CATEGORY);
    }
}