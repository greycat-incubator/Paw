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
package paw.graph.customTypes.bitset;


import greycat.base.BaseCustomType;
import greycat.struct.EStructArray;
import greycat.utility.HashHelper;
import org.roaringbitmap.IntIterator;

public abstract class CTBitset  extends BaseCustomType{
    public static final String BITS = "bits";
    protected static final int BITS_H = HashHelper.hash(BITS);

    public CTBitset(EStructArray p_backend) {
        super(p_backend);
    }

    public abstract void save();

    public abstract void clear();

    public abstract boolean add(int index);

    public abstract void clear(int index);

    public abstract int size();

    public abstract int cardinality();

    public abstract boolean get(int index);

    public abstract int nextSetBit(int startIndex);

    public abstract IntIterator iterator();

}
