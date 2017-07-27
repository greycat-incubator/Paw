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

import greycat.Callback;
import greycat.Graph;
import greycat.NodeIndex;
import greycat.Type;
import greycat.base.BaseNode;
import greycat.struct.IntStringMap;
import greycat.utility.HashHelper;

import static greycat.Constants.BEGINNING_OF_TIME;
import static paw.PawConstants.INDEX_DELIMITER;
import static paw.graph.nodes.TokenizeContentNode.CATEGORY;


/**
 * Delimiter Vocabulary Node (one per category) node handling all delimiters present in a category storing them and their hash
 */
public class DelimiterVocabularyNode extends BaseNode {
    public final static String NAME = "DelimiterVocabulary";

    public final static String DELIMITER_MAP = "delimiterMap";
    private final static int DELIMITER_MAP_H = HashHelper.hash(DELIMITER_MAP);


    /**
     * Constructor
     *
     * @param p_world world
     * @param p_time  time
     * @param p_id    node id
     * @param p_graph graph
     */
    public DelimiterVocabularyNode(long p_world, long p_time, long p_id, Graph p_graph) {
        super(p_world, p_time, p_id, p_graph);
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
     * Function to initialize the Delimiter node
     *
     * @param category name of the category
     */
    private void initDelimiter(String category, NodeIndex indexfather, Callback<DelimiterVocabularyNode> callback) {
        set(CATEGORY, Type.STRING, category);
        this.setTimeSensitivity(-1, 0);
        indexfather.update(this);
        indexfather.free();
        getOrCreateAt(DELIMITER_MAP_H, Type.INT_TO_STRING_MAP);
        callback.on(this);
    }

    /**
     * Function to retrieve or create a category node
     *
     * @param graph    graph
     * @param category name of the category
     * @param callback in which the node will be returned
     */
    public final static void getOrCreateDelimiterNode(Graph graph, String category, Callback<DelimiterVocabularyNode> callback) {
        graph.index(0, BEGINNING_OF_TIME, INDEX_DELIMITER, index ->
                index.findFrom(
                        result -> {
                            DelimiterVocabularyNode delimiterNode;
                            if (result.length != 0) {
                                delimiterNode = (DelimiterVocabularyNode) result[0];
                                index.free();
                                callback.on(delimiterNode);
                            } else {
                                delimiterNode = (DelimiterVocabularyNode) graph.newTypedNode(0, BEGINNING_OF_TIME, NAME);
                                delimiterNode.initDelimiter(category, index, callback);
                            }
                        }
                        , category));
    }

}

