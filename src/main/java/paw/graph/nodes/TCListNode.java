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
import greycat.struct.LongSet;
import greycat.utility.HashHelper;

*/
/**
 *
 *//*

public class TCListNode extends BaseNode {

    public final static String NAME = "TcList";
    public final static String TC_LIST = "List";
    private final static int TC_LIST_H = HashHelper.hash(TC_LIST);

    */
/**
     * Constructor
     *
     * @param p_world
     * @param p_time
     * @param p_id
     * @param p_graph
     *//*

    public TCListNode(long p_world, long p_time, long p_id, Graph p_graph) {
        super(p_world, p_time, p_id, p_graph);
    }

    */
/**
     * method to initialize the node
     *//*

    protected final void initNode() {

        getOrCreateAt(TC_LIST_H, Type.LONG_SET);
        setTimeSensitivity(-1, 0);
    }

    */
/**
     * method to add a tokenize Content id
     *
     * @param nodeId
     *//*

    protected final int addTokenizeContentID(long nodeId) {
        LongSet longSet = (LongSet) getAt(TC_LIST_H);
        int result = longSet.index(nodeId);
        if (result == -1) {
            result = longSet.size();
            longSet.put(nodeId);
        }
        return result;
    }

    */
/**
     * method to retrieve the tc id at a given position
     *
     * @param position to look for
     * @return the tokenize content id
     *//*

    public final long tcIdAtPosition(int position) {
        return ((LongSet) getAt(TC_LIST_H)).extract()[position];
    }


    */
/**
     * method to check wether a TC is present in the list
     *
     * @param nodeId
     * @return
     *//*

    public final boolean containsTC(int nodeId) {
        return ((LongSet) getAt(TC_LIST_H)).contains(nodeId);
    }


}
*/
