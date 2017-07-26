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
import greycat.struct.IntStringMap;
import greycat.struct.Relation;
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

    public final static String TC_LIST = "List";
    private final static int TC_LIST_H = HashHelper.hash(TC_LIST);

    public final static String DELIMITER_MAP = "delimiterMap";
    private final static int DELIMITER_MAP_H = HashHelper.hash(DELIMITER_MAP);

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

    public final long[] getTokenizeContentList() {
        return ((Relation) getAt(TC_LIST_H)).all();
    }

    public final void addTCToTCList(long id) {
        ((Relation) getAt(TC_LIST_H)).add(id);
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
     * Method to retrieve the delimiter corresponding to a given hash
     *
     * @param hash to look for
     * @return the delimiter
     */
    public final String retrieveDelimiterCorrespondingTo(int hash) {
        IntStringMap map = (IntStringMap) getAt(DELIMITER_MAP_H);
        return map.get(hash);
    }

    /**
     * Method to add a delimiter to the map
     *
     * @param hash      to add
     * @param delimiter corresponding
     * @return true if inserted false if already present
     */
    public boolean addDelimiter(int hash, String delimiter) {
        IntStringMap map = (IntStringMap) getAt(DELIMITER_MAP_H);
        if (!delimiter.equals(map.get(hash))) {
            map.put(hash, delimiter);
            return true;
        } else {
            return false;
        }
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

        getOrCreateAt(TC_LIST_H, Type.RELATION);
        getOrCreateAt(DELIMITER_MAP_H, Type.INT_TO_STRING_MAP);

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
        graph.index(0, BEGINNING_OF_TIME, INDEX_CATEGORY, index ->
                index.findFrom(
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
                        }
                        , category));
    }
}