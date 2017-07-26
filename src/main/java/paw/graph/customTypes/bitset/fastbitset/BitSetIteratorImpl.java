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

import org.roaringbitmap.IntIterator;

public final class BitSetIteratorImpl implements IntIterator {

    private final BitSet that;
    private int nextIndex;
    private int currentIndex = -1;
    private boolean reversed;

    public BitSetIteratorImpl(BitSet that, int from, boolean reversed) {
        this.that = that;
        this.nextIndex = reversed ? that.previousSetBit(from) : that.nextSetBit(from);
        this.reversed = reversed;
    }

    public IntIterator clone() {
        return this;
    }

    public boolean hasNext() {
        return (nextIndex >= 0);
    }

    public int next() {
        if (nextIndex < 0)
            return -1;
        currentIndex = nextIndex;
        nextIndex = reversed ? that.previousSetBit(nextIndex - 1) : that.nextSetBit(nextIndex + 1);
        return currentIndex;
    }


}