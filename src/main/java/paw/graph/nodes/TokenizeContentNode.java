package paw.graph.nodes;

import greycat.*;
import greycat.base.BaseNode;
import greycat.struct.LongLongArrayMap;
import greycat.struct.Relation;
import greycat.utility.HashHelper;
import paw.graph.customTypes.tokenizedContent.CTTCRoaring;
import paw.graph.customTypes.tokenizedContent.Word;
import paw.tokenizer.token.ContentT;
import paw.tokenizer.token.DelimiterT;
import paw.tokenizer.token.NumberT;
import paw.tokenizer.token.Token;
import paw.tokenizer.utils.LowerString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static paw.PawConstants.*;


/**
 * Tokenize content Node
 */
public class TokenizeContentNode extends BaseNode {

    public final static String NAME = "TokenizeContent";
    public final static String TOKENIZE_CONTENT_NAME = "name";
    public final static String CATEGORY = "category";
    public final static String FATHER = "father";
    private final static String INTERNAL_ENCODED_TEXT = "encodedText";
    private final static String INTERNAL_MAP_OF_WORDS = "mapOfWords";
    private final static String INTERNAL_MASKS = "masks";
    public final static String TOKENIZED_CONTENT_INDEX = "indexOfTC";

    protected final static int TOKENIZE_CONTENT_NAME_H = HashHelper.hash(TOKENIZE_CONTENT_NAME);
    protected final static int CATEGORY_H = HashHelper.hash(CATEGORY);
    protected final static int FATHER_H = HashHelper.hash(FATHER);
    private final static int INTERNAL_ENCODED_TEXT_H = HashHelper.hash(INTERNAL_ENCODED_TEXT);
    private final static int INTERNAL_MAP_OF_WORDS_H = HashHelper.hash(INTERNAL_MAP_OF_WORDS);
    private final static int INTERNAL_MASKS_H = HashHelper.hash(INTERNAL_MASKS);
    protected final static int TOKENIZED_CONTENT_INDEX_H = HashHelper.hash(TOKENIZED_CONTENT_INDEX);

    /**
     * Constructor
     *
     * @param p_world
     * @param p_time
     * @param p_id
     * @param p_graph
     */
    public TokenizeContentNode(long p_world, long p_time, long p_id, Graph p_graph) {
        super(p_world, p_time, p_id, p_graph);
    }

    /**
     * Method to initialize the node and return it in the callback
     *
     * @param relatedNode node that require a tokenize content
     * @param category    of tokenize content
     * @param name        of the content
     * @param callback    in which the node will be return once initialize
     */
    private final void initNode(Node relatedNode, String category, String name, Callback<TokenizeContentNode> callback) {
        addToRelationAt(FATHER_H, relatedNode);
        setAt(TOKENIZE_CONTENT_NAME_H, Type.STRING, name);

        getOrCreateCustomAt(INTERNAL_ENCODED_TEXT_H, CTTCRoaring.NAME);


        TokenizeContentNode currentNode = this;
        Relation relation = (Relation) getOrCreateAt(CATEGORY_H, Type.RELATION);
        CategoryNode.getOrCreateCategoryNode(_graph, category, result -> {
            relation.add(result.id());
            result.addTCToTCList(currentNode._id);
            result.free();
            callback.on(currentNode);
        });
    }

    /**
     * Method to retrieve the category node corresponding to the tokenize content
     *
     * @param callback in which the categoryNode will be returned
     */
    public final void getCategoryNode(Callback<CategoryNode> callback) {
        traverseAt(CATEGORY_H, (Callback<Node[]>) result -> callback.on((CategoryNode) result[0]));
    }

    /**
     * Method to retrieve the related Node
     *
     * @param callback in which the related Node will be returned
     */
    public final void getFather(Callback<Node> callback) {
        traverseAt(FATHER_H, (Callback<Node[]>) result -> callback.on(result[0]));
    }

    /**
     * @return
     */
    public final List<Token> rebuildContent() {
        CTTCRoaring roaring = (CTTCRoaring) getOrCreateCustomAt(INTERNAL_ENCODED_TEXT_H, CTTCRoaring.NAME);
        List<Word> words = roaring.decodeWords();


        final CategoryNode[] categoryNodes = new CategoryNode[1];
        getCategoryNode(result -> categoryNodes[0] = result);

        LongLongArrayMap mapMasks = (LongLongArrayMap) getAt(INTERNAL_MASKS_H);
        List<Token> tokens = new ArrayList<>(words.size());

        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            Token[] token = new Token[1];
            switch (word.getType()) {
                case CONTENT_TOKEN:
                    long[] lmask = mapMasks.get(i);
                    int[] mask = new int[lmask.length];
                    if (lmask.length > 0) {
                        int k = 0;
                        for (int j = lmask.length - 1; j >= 0; j++) {
                            mask[k] = (int) lmask[j];
                            k++;
                        }
                    }
                    categoryNodes[0].getVocabularyNodeFor((char) word.getFirstChar(), new Callback<VocabularyNode>() {
                        @Override
                        public void on(VocabularyNode result) {
                            token[0] = new ContentT(new LowerString(result.getWordForPosition(word.getWordID()), mask));
                            result.free();
                        }
                    });
                    break;
                case DELIMITER_TOKEN:
                    token[0] = new DelimiterT(categoryNodes[0].retrieveDelimiterCorrespondingTo(word.getWordID()));
                    break;
                case NUMBER_TOKEN:
                    token[0] = new NumberT(word.getWordID());
                    break;
            }
            tokens.add(token[0]);
        }
        categoryNodes[0].free();

        return tokens;
    }

    public final TokenizeContentNode setContent(List<Token> tokens) {
        this.rephase();
        CTTCRoaring roaring = (CTTCRoaring) getOrCreateCustomAt(INTERNAL_ENCODED_TEXT_H, CTTCRoaring.NAME);
        roaring.clear();
        final CategoryNode[] categoryNodes = new CategoryNode[1];
        getCategoryNode(result -> categoryNodes[0] = result);

        removeAt(INTERNAL_MAP_OF_WORDS_H);
        removeAt(INTERNAL_MASKS_H);
        LongLongArrayMap mapPosition = (LongLongArrayMap) getOrCreateAt(INTERNAL_MAP_OF_WORDS_H, Type.LONG_TO_LONG_ARRAY_MAP);
        LongLongArrayMap mapMasks = (LongLongArrayMap) getOrCreateAt(INTERNAL_MASKS_H, Type.LONG_TO_LONG_ARRAY_MAP);

        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String content = token.getToken();
            int hash = HashHelper.hash(content);
            switch (token.getType()) {
                case CONTENT_TOKEN:
                    mapPosition.put(hash, i);
                    int[] mask = ((ContentT) token).getLowerString().getMask();
                    if (!(mask.length == 1 && mask[0] == 0)) {
                        for (int j = mask.length - 1; j >= 0; j--) {
                            mapMasks.putNoCheck(i, mask[j]);
                        }
                    }
                    char firstChar = content.charAt(0);
                    if (map.containsKey(hash)) {
                        roaring.addWord(new Word(CONTENT_TOKEN, map.get(hash), firstChar));
                    } else {
                        VocabularyNode[] vocabularyNodes = new VocabularyNode[1];
                        categoryNodes[0].getVocabularyNodeFor(firstChar, result -> vocabularyNodes[0] = result);
                        int position = vocabularyNodes[0].getOrCreateWord(content);
                        roaring.addWord(new Word(CONTENT_TOKEN, position, firstChar));
                        map.put(hash, position);
                        vocabularyNodes[0].free();
                    }
                    break;
                case DELIMITER_TOKEN:
                    categoryNodes[0].addDelimiter(hash, content);
                    roaring.addWord(new Word(DELIMITER_TOKEN, hash));
                    break;
                case NUMBER_TOKEN:
                    roaring.addWord(new Word(NUMBER_TOKEN, ((NumberT) token).getInt()));
                    break;
            }
        }
        categoryNodes[0].free();
        roaring.save();
        return this;
    }

    public final void containsWord(String word, boolean caseSensitive, Callback<Boolean> callback) {
        //TODO
    }

    public final void frequency(String word, boolean caseSensitive, Callback<Integer> callback) {
//TODO
    }


    public static void getTokenizeContentOfNode(Node relatedNode, String name, Callback<TokenizeContentNode> callback) {
        Index index = (Index) relatedNode.getAt(TOKENIZED_CONTENT_INDEX_H);
        if (index == null) {
            callback.on(null);
        } else {
            index.find(result -> {
                if (result.length == 1) {
                    callback.on((TokenizeContentNode) result[0]);
                } else {
                    callback.on(null);
                }
            }, relatedNode.world(), relatedNode.time(), name);
        }
    }

    public static void getOrCreateTokenizeContentOfNode(Node relatedNode, String name, String category, Callback<TokenizeContentNode> callback) {
        getTokenizeContentOfNode(relatedNode, name, new Callback<TokenizeContentNode>() {
            @Override
            public void on(TokenizeContentNode result) {
                if (result != null) {
                    callback.on(result);
                } else {
                    TokenizeContentNode node = (TokenizeContentNode) relatedNode.graph().newTypedNode(relatedNode.world(), relatedNode.time(), NAME);
                    node.initNode(relatedNode, category, name, tcn -> {
                        Index index = (Index) relatedNode.getAt(TOKENIZED_CONTENT_INDEX_H);
                        if (index == null) {
                            Index finalIndex = (Index) relatedNode.getOrCreateAt(TOKENIZED_CONTENT_INDEX_H, Type.INDEX);
                            finalIndex.declareAttributes(result1 -> {
                                finalIndex.update(node);
                                callback.on(node);
                            }, TOKENIZE_CONTENT_NAME);
                        } else {
                            index.update(node);
                            callback.on(node);
                        }
                    });
                }
            }
        });
    }

}
