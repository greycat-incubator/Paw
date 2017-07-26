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
package paw.graph.customTypes.bitset.fastbitset;


import greycat.Type;
import greycat.struct.EStruct;
import greycat.struct.EStructArray;
import greycat.struct.IntArray;
import org.roaringbitmap.IntIterator;
import paw.graph.customTypes.bitset.CTBitset;

import java.util.List;

public class CTFastBitSet extends CTBitset {

    private IntArray gBits;
    protected BitSet bitSet;
    protected EStruct root;

    public static final String NAME = "FastBitSet";

    public CTFastBitSet(EStructArray array) {
        super(array);
        root = array.root();
        if (root == null) {
            root = array.newEStruct();
            array.setRoot(root);
        }
        gBits = (IntArray) root.getOrCreateAt(BITS_H, Type.INT_ARRAY);
        bitSet = new BitSet(gBits.extract());
    }

    public void clear() {
        gBits.initWith(new int[0]);
        bitSet.clear();
    }

    @Override
    public boolean add(int index) {
        return bitSet.add(index);
    }

    @Override
    public boolean addAll(List<Integer> indexs) {
        for (int index : indexs) {
            bitSet.add(index);
        }
        return true;
    }

    @Override
    public void clear(int index) {
        bitSet.clear(index);
    }

    @Override
    public int size() {
        return bitSet.size();
    }

    @Override
    public int cardinality() {
        return bitSet.cardinality();
    }

    @Override
    public boolean get(int index) {
        return bitSet.get(index);
    }

    @Override
    public int nextSetBit(int startIndex) {
        return bitSet.nextSetBit(startIndex);
    }

    @Override
    public IntIterator iterator() {
        return bitSet.iterator();
    }

    public void save() {
        gBits.initWith(bitSet.toIntArray());
    }

    public BitSet getBitSet() {
        return bitSet;
    }

    public void setBitset(BitSet bitSet) {
        this.bitSet = bitSet;
    }
}
