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
/*
package paw.graph.nodes;

import greycat.Graph;
import greycat.Type;
import greycat.base.BaseNode;
import greycat.struct.IntStringMap;
import greycat.utility.HashHelper;

*/
/**
 * Delimiter Vocabulary Node (one per category) node handling all delimiters present in a category storing them and their hash
 *//*

public class DelimiterVocabularyNode extends BaseNode {

    public final static String NAME = "DelVoc";

    public final static String DELIMITER_MAP = "delimiterMap";
    private final static int DELIMITER_MAP_H = HashHelper.hash(DELIMITER_MAP);

    */
/**
     * Constructor
     *
     * @param p_world world
     * @param p_time  time
     * @param p_id    node id
     * @param p_graph graph
     *//*

    public DelimiterVocabularyNode(long p_world, long p_time, long p_id, Graph p_graph) {
        super(p_world, p_time, p_id, p_graph);
    }

    */
/**
     * method to initialize the node
     *//*

    protected final void initNode() {
        getOrCreateAt(DELIMITER_MAP_H, Type.INT_TO_STRING_MAP);
    }

    */
/**
     * Method to retrieve the delimiter corresponding to a given hash
     *
     * @param hash to look for
     * @return the delimiter
     *//*

    public final String retrieveDelimiterCorrespondingTo(int hash) {
        IntStringMap map = (IntStringMap) getAt(DELIMITER_MAP_H);
        return map.get(hash);
    }

    */
/**
     * Method to add a delimiter to the map
     *
     * @param hash      to add
     * @param delimiter corresponding
     * @return true if inserted false if already present
     *//*

    public boolean addDelimiter(int hash, String delimiter) {
        IntStringMap map = (IntStringMap) getAt(DELIMITER_MAP_H);
        if (!delimiter.equals(map.get(hash))) {
            map.put(hash, delimiter);
            return true;
        } else {
            return false;
        }
    }

}
*/
