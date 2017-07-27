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
import greycat.struct.Relation;
import greycat.utility.HashHelper;

import static greycat.Constants.BEGINNING_OF_TIME;
import static paw.PawConstants.INDEX_DICTIONNARY;
import static paw.graph.nodes.TokenizeContentNode.CATEGORY;
import static paw.graph.nodes.TokenizeContentNode.CATEGORY_H;

/**
 * Category Node, extending the base node, this node is the main entrance point to a given category of tokenize content
 */
public class DictionnaryNode extends BaseNode {
    public final static String NAME = "DICTIONNARY";

    public final static String VOCABULARY_RELATION = "vocabulary";
    private final static int VOCABULARY_RELATION_H = HashHelper.hash(VOCABULARY_RELATION);

    public final static String TC_LIST = "List";
    private final static int TC_LIST_H = HashHelper.hash(TC_LIST);

    public final static String NUMBER_OF_TC = "numberOfTc";
    private final static int NUMBER_OF_TC_H = HashHelper.hash(NUMBER_OF_TC);

    /**
     * Constructor
     *
     * @param p_world world
     * @param p_time  time
     * @param p_id    id of the node
     * @param p_graph graph
     */
    public DictionnaryNode(long p_world, long p_time, long p_id, Graph p_graph) {
        super(p_world, p_time, p_id, p_graph);
    }

    /**
     * @return the name of the category
     */
    public final String getCategory() {
        return (String) getAt(CATEGORY_H);
    }


    public final void addTCToTCList(long id) {
        int ntc = (int) getAt(NUMBER_OF_TC_H);
        final TCListNode[] node = new TCListNode[1];
        Relation relation = (Relation) getAt(TC_LIST_H);
        if (ntc % 10000 == 0) {
            node[0] = (TCListNode) graph().newTypedNode(0, BEGINNING_OF_TIME, TCListNode.NAME);
            node[0].initNode();
            relation.add(node[0].id());
        } else {
            long nodeId = relation.get(relation.size() - 1);
            graph().lookup(0, BEGINNING_OF_TIME, nodeId, result -> node[0] = (TCListNode) result);
        }
        node[0].addTokenizeContentID(id);
        node[0].free();
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
     * Function to initialize the category node
     *
     * @param category name of the category
     */
    private void initDictionnary(String category, NodeIndex indexfather, Callback<DictionnaryNode> callback) {
        set(CATEGORY, Type.STRING, category);
        this.setTimeSensitivity(-1, 0);
        indexfather.update(this);
        indexfather.free();

        getOrCreateAt(TC_LIST_H, Type.RELATION);
        setAt(NUMBER_OF_TC_H, Type.INT, 0);

        DictionnaryNode categoryNode = this;

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
    public final static void getOrCreateDictionnaryNode(Graph graph, String category, Callback<DictionnaryNode> callback) {
        graph.index(0, BEGINNING_OF_TIME, INDEX_DICTIONNARY, index ->
                index.findFrom(
                        result -> {
                            DictionnaryNode categoryNode;
                            if (result.length != 0) {
                                categoryNode = (DictionnaryNode) result[0];
                                index.free();
                                callback.on(categoryNode);
                            } else {
                                categoryNode = (DictionnaryNode) graph.newTypedNode(0, BEGINNING_OF_TIME, DictionnaryNode.NAME);
                                categoryNode.initDictionnary(category, index, callback);
                            }
                        }
                        , category));
    }
}