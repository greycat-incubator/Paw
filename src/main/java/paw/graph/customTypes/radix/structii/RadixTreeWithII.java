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
package paw.graph.customTypes.radix.structii;

import greycat.struct.EStruct;
import greycat.struct.EStructArray;
import paw.graph.customTypes.bitset.CTBitset;
import paw.graph.customTypes.bitset.fastbitset.CTFastBitSet;
import paw.graph.customTypes.radix.struct.RadixTree;

@SuppressWarnings("Duplicates")
public class RadixTreeWithII extends RadixTree {
    public static final String NAME = "RadixTreeStructWithII" ;

    public RadixTreeWithII(final EStructArray eGraph) {
        super(eGraph);
    }


    public int getOrCreateWithID(String key, int docId) {
        int index = getOrCreate(key);
        addIDToNode(index, docId);
        return index;
    }

    public void addIDToNode(int node, int docId) {
        EStruct enode = _backend.estruct(node);
        CTFastBitSet bitMap = (CTFastBitSet) enode.getOrCreateCustom("ii", CTFastBitSet.NAME);
        bitMap.add(docId);
        bitMap.save();
    }

    public CTBitset retrieveInvertedIndexFor(int node) {
        EStruct enode = _backend.estruct(node);
        return (CTFastBitSet) enode.getOrCreateCustom("ii", CTFastBitSet.NAME);
    }
}
