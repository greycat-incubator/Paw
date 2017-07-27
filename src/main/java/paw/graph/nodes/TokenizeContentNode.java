/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package paw.graph.nodes;

import greycat.*;
import greycat.base.BaseNode;
import greycat.plugin.Job;
import greycat.struct.EStruct;
import greycat.struct.EStructArray;
import greycat.struct.IntArray;
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

import static greycat.Constants.BEGINNING_OF_TIME;
import static paw.PawConstants.*;
import static paw.graph.nodes.DelimiterVocabularyNode.getOrCreateDelimiterNode;
import static paw.graph.nodes.DictionnaryNode.getOrCreateDictionnaryNode;


/**
 * Tokenize content Node
 */
public class TokenizeContentNode extends BaseNode {

    public final static String NAME = "TokenizeContent";
    public final static String TOKENIZE_CONTENT_NAME = "name";
    public final static String CATEGORY = "category";
    public final static String FATHER = "father";
    private final static String INTERNAL_ENCODED_TEXT = "encodedText";
    private final static String INTERNAL_LOCAL_STAT = "localStat";
    // private final static String INTERNAL_MAP_OF_WORDS = "mapOfWords";
    // private final static String INTERNAL_MASKS = "masks";
    public final static String TOKENIZED_CONTENT_INDEX = "indexOfTC";

    protected final static int TOKENIZE_CONTENT_NAME_H = HashHelper.hash(TOKENIZE_CONTENT_NAME);
    protected final static int CATEGORY_H = HashHelper.hash(CATEGORY);
    protected final static int FATHER_H = HashHelper.hash(FATHER);
    private final static int INTERNAL_ENCODED_TEXT_H = HashHelper.hash(INTERNAL_ENCODED_TEXT);
    private final static int INTERNAL_LOCAL_STAT_H = HashHelper.hash(INTERNAL_LOCAL_STAT);
    //private final static int INTERNAL_MAP_OF_WORDS_H = HashHelper.hash(INTERNAL_MAP_OF_WORDS);
    //private final static int INTERNAL_MASKS_H = HashHelper.hash(INTERNAL_MASKS);
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
     */
    private final void initNode(Node relatedNode, String category, String name) {
        addToRelationAt(FATHER_H, relatedNode);
        setAt(TOKENIZE_CONTENT_NAME_H, Type.STRING, name);
        setAt(CATEGORY_H, Type.STRING, category);

        getOrCreateCustomAt(INTERNAL_ENCODED_TEXT_H, CTTCRoaring.NAME);
    }

    /**
     * Method to retrieve the category node corresponding to the tokenize content
     */
    public final String getCategory() {
        return (String) getAt(CATEGORY_H);
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

        String category = getCategory();

        final DictionnaryNode[] dictionnaryNodes = new DictionnaryNode[1];
        graph().index(0, BEGINNING_OF_TIME, INDEX_DICTIONNARY,
                index -> index.findFrom(
                        result -> {
                            dictionnaryNodes[0] = (DictionnaryNode) result[0];
                            index.free();
                        }, category));

        final DelimiterVocabularyNode[] delimiterVocabularyNodes = new DelimiterVocabularyNode[1];
        graph().index(0, BEGINNING_OF_TIME, INDEX_DELIMITER,
                index -> index.findFrom(
                        result -> {
                            delimiterVocabularyNodes[0] = (DelimiterVocabularyNode) result[0];
                            index.free();
                        }, category));

        EStruct masks = ((EStructArray) getAt(INTERNAL_LOCAL_STAT_H)).estruct(1);

        List<Token> tokens = new ArrayList<>(words.size());

        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            Token[] token = new Token[1];
            switch (word.getType()) {
                case CONTENT_TOKEN:
                    Object omask = masks.getAt(i);
                    int[] mask;
                    if (omask != null) {
                        mask = ((IntArray) omask).extract();
                    } else {
                        mask = new int[0];
                    }
                    dictionnaryNodes[0].getVocabularyNodeFor((char) word.getFirstChar(),
                            result -> {
                                token[0] = new ContentT(
                                        new LowerString(
                                                result.getWordForPosition(word.getWordID()),
                                                mask)
                                );
                                result.free();
                            });
                    break;
                case DELIMITER_TOKEN:
                    token[0] = new DelimiterT(delimiterVocabularyNodes[0].retrieveDelimiterCorrespondingTo(word.getWordID()));
                    break;
                case NUMBER_TOKEN:
                    token[0] = new NumberT(word.getWordID());
                    break;
            }
            tokens.add(token[0]);
        }
        delimiterVocabularyNodes[0].free();
        dictionnaryNodes[0].free();

        return tokens;
    }

    public final void setContent(List<Token> tokens) {
        this.rephase();
        String category = getCategory();
        CTTCRoaring roaring = (CTTCRoaring) getOrCreateCustomAt(INTERNAL_ENCODED_TEXT_H, CTTCRoaring.NAME);
        roaring.clear();

        removeAt(INTERNAL_LOCAL_STAT_H);
        EStructArray array = (EStructArray) getOrCreateAt(INTERNAL_LOCAL_STAT_H, Type.ESTRUCT_ARRAY);
        EStruct ls = array.newEStruct();
        array.setRoot(ls);
        EStruct masks = array.newEStruct();


        final DictionnaryNode[] dictionnaryNodes = new DictionnaryNode[1];
        graph().index(0, BEGINNING_OF_TIME, INDEX_DICTIONNARY,
                index -> index.findFrom(
                        result -> {
                            dictionnaryNodes[0] = (DictionnaryNode) result[0];
                            index.free();
                        }, category));

        final DelimiterVocabularyNode[] delimiterVocabularyNodes = new DelimiterVocabularyNode[1];
        graph().index(0, BEGINNING_OF_TIME, INDEX_DELIMITER,
                index -> index.findFrom(
                        result -> {
                            delimiterVocabularyNodes[0] = (DelimiterVocabularyNode) result[0];
                            index.free();
                        }, category));

        Map<Integer, Integer> map = new HashMap<>();

        List<Word> words = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String content = token.getToken();
            int hash = HashHelper.hash(content);
            switch (token.getType()) {
                case CONTENT_TOKEN:
                    IntArray positions = (IntArray) ls.getOrCreateAt(hash, Type.INT_ARRAY);
                    positions.addElement(i);

                    int[] mask = ((ContentT) token).getLowerString().getMask();
                    if (!(mask.length == 1 && mask[0] == 0)) {
                        IntArray maskI = (IntArray) masks.getOrCreateAt(i, Type.INT_ARRAY);
                        maskI.initWith(mask);
                    }
                    char firstChar = content.charAt(0);
                    if (map.containsKey(hash)) {
                        words.add(new Word(CONTENT_TOKEN, map.get(hash), firstChar));
                    } else {
                        VocabularyNode[] vocabularyNodes = new VocabularyNode[1];
                        dictionnaryNodes[0].getVocabularyNodeFor(firstChar, result -> vocabularyNodes[0] = result);
                        int position = vocabularyNodes[0].getOrCreateWord(content);
                        words.add(new Word(CONTENT_TOKEN, position, firstChar));
                        map.put(hash, position);
                        vocabularyNodes[0].free();
                    }
                    break;
                case DELIMITER_TOKEN:
                    delimiterVocabularyNodes[0].addDelimiter(hash, content);
                    words.add(new Word(DELIMITER_TOKEN, hash));
                    break;
                case NUMBER_TOKEN:
                    words.add(new Word(NUMBER_TOKEN, ((NumberT) token).getInt()));
                    break;
            }
        }
        roaring.addWords(words);
        dictionnaryNodes[0].free();
        delimiterVocabularyNodes[0].free();
        roaring.save();
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

    private static void generateDelimiterAndDictionnaryForCategory(Graph graph, String category, long id, Callback<Boolean> callback) {
        DeferCounter counter = graph.newCounter(2);
        graph.index(0, BEGINNING_OF_TIME, INDEX_DELIMITER, new Callback<NodeIndex>() {
            @Override
            public void on(NodeIndex result) {
                long[] catId = result.select(category);
                result.free();
                if (catId.length > 0) {
                    counter.count();
                } else {
                    getOrCreateDelimiterNode(graph, category, new Callback<DelimiterVocabularyNode>() {
                        @Override
                        public void on(DelimiterVocabularyNode result) {
                            result.free();
                            counter.count();
                        }
                    });
                }
            }
        });

        graph.index(0, BEGINNING_OF_TIME, INDEX_DICTIONNARY, new Callback<NodeIndex>() {
            @Override
            public void on(NodeIndex result) {
                long[] catId = result.select(category);
                result.free();
                DictionnaryNode[] dictionnaryNodes = new DictionnaryNode[1];
                if (catId.length > 0) {
                    graph.lookup(0, BEGINNING_OF_TIME, catId[0], new Callback<Node>() {
                        @Override
                        public void on(Node result) {
                            dictionnaryNodes[0] = (DictionnaryNode) result;
                        }
                    });
                } else {
                    getOrCreateDictionnaryNode(graph, category, new Callback<DictionnaryNode>() {
                        @Override
                        public void on(DictionnaryNode result) {
                            dictionnaryNodes[0] = (DictionnaryNode) result;
                        }
                    });
                }
                dictionnaryNodes[0].addTCToTCList(id);
                dictionnaryNodes[0].free();
                counter.count();
            }
        });

        counter.then(new Job() {
            @Override
            public void run() {
                callback.on(true);
            }
        });
    }

    public static void getOrCreateTokenizeContentOfNode(Node relatedNode, String name, String category, Callback<TokenizeContentNode> callback) {
        getTokenizeContentOfNode(relatedNode, name, new Callback<TokenizeContentNode>() {
            @Override
            public void on(TokenizeContentNode result) {
                if (result != null) {
                    callback.on(result);
                } else {
                    Graph graph = relatedNode.graph();
                    TokenizeContentNode node = (TokenizeContentNode) graph.newTypedNode(relatedNode.world(), relatedNode.time(), NAME);
                    node.initNode(relatedNode, category, name);
                    DeferCounter counter = graph.newCounter(2);
                    Index index = (Index) relatedNode.getAt(TOKENIZED_CONTENT_INDEX_H);
                    if (index == null) {
                        Index finalIndex = (Index) relatedNode.getOrCreateAt(TOKENIZED_CONTENT_INDEX_H, Type.INDEX);
                        finalIndex.declareAttributes(result1 -> {
                            finalIndex.update(node);
                            counter.count();
                        }, TOKENIZE_CONTENT_NAME);
                    } else {
                        index.update(node);
                        counter.count();
                    }
                    generateDelimiterAndDictionnaryForCategory(graph, category, node.id(), new Callback<Boolean>() {
                        @Override
                        public void on(Boolean result) {
                            counter.count();
                        }
                    });

                    counter.then(new Job() {
                        @Override
                        public void run() {
                            callback.on(node);
                        }
                    });
                }
            }
        });
    }

}
