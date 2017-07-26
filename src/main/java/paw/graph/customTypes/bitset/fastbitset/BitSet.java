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

import java.util.Arrays;
import java.util.NoSuchElementException;

public class BitSet {

    private static final int[] ALL_CLEARED = new int[0];
    /**
     * Holds the bits (32 bits per int).
     */
    private int[] bits;

    /**
     * Creates a new bit-set (all bits cleared).
     */
    public BitSet(int[] content) {
        bits = content;
    }

    /**
     * Creates a new bit-set (all bits cleared).
     */
    public BitSet() {
        bits = new int[0];
    }


    @Override
    public BitSet clone() {
        BitSet copy = new BitSet(bits);
        return copy;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Set operations.
    //

    public boolean add(int index) {
        return !getAndSet(index, true);
    }


    public void clear() {
        bits = ALL_CLEARED;
    }

    public int first() {
        int i = nextSetBit(0);
        if (i < 0) throw new NoSuchElementException();
        return i;
    }

    public int last() {
        int i = previousSetBit(length() - 1);
        if (i < 0) throw new NoSuchElementException();
        return i;
    }


    public int size() {
        return cardinality();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int pollFirst() {
        int i = nextSetBit(0);
        if (i < 0) return -1;
        clear(i);
        return i;
    }

    public int pollLast() {
        int i = previousSetBit(length());
        if (i < 0) return -1;
        clear(i);
        return i;
    }

    ////////////////////////////////////////////////////////////////////////////
    // BitSet Operations.
    //

    /**
     * Performs the logical AND operation on this bit set and the
     * given bit set. This means it builds the intersection
     * of the two sets. The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void applyAnd(BitSet that) {
        int[] thatBits = that.toIntArray();
        int n = min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; i++) {
            this.bits[i] &= thatBits[i];
        }
        for (int i = n; i < this.bits.length; i++) {
            this.bits[i] = 0;
        }
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * given bit set. This means it builds the intersection
     * of the two sets. The result is return.
     *
     * @param that the second bit set.
     */
    public BitSet and(BitSet that) {
        int[] thatBits = that.toIntArray();
        int[] result = Arrays.copyOf(this.bits, this.bits.length);
        int n = min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; i++) {
            result[i] &= thatBits[i];
        }
        for (int i = n; i < this.bits.length; i++) {
            result[i] = 0;
        }
        return new BitSet(result);
    }


    public static int min(int x, int y) {
        return (x < y) ? x : y;
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * complement of the given bit set.  This means it
     * selects every element in the first set, that isn't in the
     * second set. The result is stored into this bit set.
     *
     * @param that the second bit set
     */
    public void applyAndNot(BitSet that) {
        int[] thatBits = that.toIntArray();
        int n = min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; i++) {
            this.bits[i] &= ~thatBits[i];
        }
    }

    /**
     * Performs the logical AND operation on this bit set and the
     * complement of the given bit set.  This means it
     * selects every element in the first set, that isn't in the
     * second set. The result is stored into this bit set.
     *
     * @param that the second bit set
     */
    public BitSet andNot(BitSet that) {
        int[] thatBits = that.toIntArray();
        int[] result = Arrays.copyOf(this.bits, this.bits.length);
        int n = min(this.bits.length, thatBits.length);
        for (int i = 0; i < n; i++) {
            result[i] &= ~thatBits[i];
        }
        return new BitSet(result);
    }

    /**
     * Returns the number of bits set to {@code true} (or the size of this
     * set).
     *
     * @return the number of bits being set.
     */
    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < bits.length; i++) {
            sum += Integer.bitCount(bits[i]);
        }
        return sum;
    }

    /**
     * Removes the specified integer value from this set. That is
     * the corresponding bit is cleared.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code index < 0}
     */
    public void clear(int bitIndex) {
        int intIndex = bitIndex >> 5;
        if (intIndex >= bits.length)
            return;
        bits[intIndex] &= ~(1 << bitIndex);
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param fromIndex index of the first bit to be cleared.
     * @param toIndex   index after the last bit to be cleared.
     * @throws IndexOutOfBoundsException if
     *                                   {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void clear(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 5;
        if (i >= bits.length)
            return; // Ensures that i < _length
        int j = toIndex >>> 5;
        if (i == j) {
            bits[i] &= ((1 << fromIndex) - 1) | (-1 << toIndex);
            return;
        }
        bits[i] &= (1 << fromIndex) - 1;
        if (j < bits.length) {
            bits[j] &= -1 << toIndex;
        }
        for (int k = i + 1; (k < j) && (k < bits.length); k++) {
            bits[k] = 0;
        }
    }

    /**
     * Sets the bit at the index to the opposite value.
     *
     * @param bitIndex the index of the bit.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void flip(int bitIndex) {
        int i = bitIndex >> 5;
        ensureCapacity(i + 1);
        bits[i] ^= 1L << bitIndex;
    }

    /**
     * Sets a range of bits to the opposite value.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex   the high index (exclusive).
     * @throws IndexOutOfBoundsException if
     *                                   {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void flip(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 5;
        int j = toIndex >>> 5;
        ensureCapacity(j + 1);
        if (i == j) {
            bits[i] ^= (-1 << fromIndex) & ((1 << toIndex) - 1);
            return;
        }
        bits[i] ^= -1 << fromIndex;
        bits[j] ^= (1 << toIndex) - 1;
        for (int k = i + 1; k < j; k++) {
            bits[k] ^= -1;
        }
    }

    /**
     * Returns {@code true } if the specified integer is in
     * this bit set; {@code false } otherwise.
     *
     * @param bitIndex a non-negative integer.
     * @return the value of the bit at the specified index.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public boolean get(int bitIndex) {
        int i = bitIndex >> 5;
        return i < bits.length && (bits[i] & (1 << bitIndex)) != 0;
    }

    /**
     * Returns a new bit set composed of a range of bits from this one.
     *
     * @param fromIndex the low index (inclusive).
     * @param toIndex   the high index (exclusive).
     * @return a context allocated bit set instance.
     * @throws IndexOutOfBoundsException if
     *                                   {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public BitSet get(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex)
            throw new IndexOutOfBoundsException();
        BitSet bitSet = new BitSet();
        int length = min(bits.length, (toIndex >>> 5) + 1);
        bitSet.bits = new int[length];
        System.arraycopy(bits, 0, bitSet.bits, 0, length);
        bitSet.clear(0, fromIndex);
        bitSet.clear(toIndex, length << 5);
        return bitSet;
    }

    /**
     * Sets the specified bit, returns <code>true</code>
     * if previously set.
     */
    public boolean getAndSet(int bitIndex, boolean value) {
        int i = bitIndex >> 5;
        ensureCapacity(i + 1);
        boolean previous = (bits[i] & (1 << bitIndex)) != 0;
        if (value) {
            bits[i] |= 1 << bitIndex;
        } else {
            bits[i] &= ~(1 << bitIndex);
        }
        return previous;
    }

    /**
     * Returns {@code true} if this bit set shares at least one
     * common bit with the specified bit set.
     *
     * @param that the bit set to check for intersection
     * @return {@code true} if the sets intersect; {@code false} otherwise.
     */
    public boolean intersects(BitSet that) {
        int[] thatBits = that.toIntArray();
        int i = min(this.bits.length, thatBits.length);
        while (--i >= 0) {
            if ((bits[i] & thatBits[i]) != 0) return true;
        }
        return false;
    }

    /**
     * Returns the logical number of bits actually used by this bit
     * set.  It returns the index of the highest set bit plus one.
     *
     * <p> Note: This method does not return the number of set bits
     * which is returned by {@link #size} </p>
     *
     * @return the index of the highest set bit plus one.
     */
    public int length() {
        trim();
        if (bits.length == 0) return 0;
        return (bits.length << 5) - Integer.numberOfLeadingZeros(bits[bits.length - 1]);
    }


    /**
     * Returns the index of the next {@code false} bit, from the specified bit
     * (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0}
     */
    public int nextClearBit(int fromIndex) {
        int offset = fromIndex >> 5;
        int mask = 1 << fromIndex;
        while (offset < bits.length) {
            long h = bits[offset];
            do {
                if ((h & mask) == 0) {
                    return fromIndex;
                }
                mask <<= 1;
                fromIndex++;
            } while (mask != 0);
            mask = 1;
            offset++;
        }
        return fromIndex;
    }

    /**
     * Returns the index of the next {@code true} bit, from the specified bit
     * (inclusive). If there is none, {@code -1} is returned.
     * The following code will iterates through the bit set:[code]
     * for (int i=nextSetBit(0); i >= 0; i = nextSetBit(i+1)) {
     * ...
     * }[/code]
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < 0}
     */
    public int nextSetBit(int fromIndex) {
        int offset = fromIndex >> 5;
        int mask = 1 << fromIndex;
        while (offset < bits.length) {
            long h = bits[offset];
            do {
                if ((h & mask) != 0)
                    return fromIndex;
                mask <<= 1;
                fromIndex++;
            } while (mask != 0);
            mask = 1;
            offset++;
        }
        return -1;
    }

    /**
     * Performs the logical OR operation on this bit set and the one specified.
     * In other words, builds the union of the two sets.
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void applyOr(BitSet that) {
        int[] thatBits = (that instanceof BitSet) ? ((BitSet) that).bits
                : that.toIntArray();
        ensureCapacity(thatBits.length);
        for (int i = thatBits.length; --i >= 0; ) {
            bits[i] |= thatBits[i];
        }
    }

    /**
     * Performs the logical OR operation on this bit set and the one specified.
     * In other words, builds the union of the two sets.
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public BitSet or(BitSet that) {
        int[] thatBits = (that instanceof BitSet) ? ((BitSet) that).bits
                : that.toIntArray();
        int[] result = Arrays.copyOf(bits, bits.length);
        ensureCapacity(thatBits.length);
        for (int i = thatBits.length; --i >= 0; ) {
            result[i] |= thatBits[i];
        }
        return new BitSet(result);
    }

    /**
     * Returns the index of the previous {@code false} bit,
     * from the specified bit (inclusive).
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < -1}
     */
    public int previousClearBit(int fromIndex) {
        int offset = fromIndex >> 5;
        int mask = 1 << fromIndex;
        while (offset >= 0) {
            long h = bits[offset];
            do {
                if ((h & mask) == 0)
                    return fromIndex;
                mask >>= 1;
                fromIndex--;
            } while (mask != 0);
            mask = 1 << 31;
            offset--;
        }
        return -1;
    }

    /**
     * Returns the index of the previous {@code true} bit, from the
     * specified bit (inclusive). If there is none, {@code -1} is returned.
     * The following code will iterates through the bit set:[code]
     * for (int i = length(); (i = previousSetBit(i-1)) >= 0; ) {
     * ...
     * }[/code]
     *
     * @param fromIndex the start location.
     * @return the first {@code false} bit.
     * @throws IndexOutOfBoundsException if {@code fromIndex < -1}
     */
    public int previousSetBit(int fromIndex) {
        int offset = fromIndex >> 5;
        int mask = 1 << fromIndex;
        while (offset >= 0) {
            long h = bits[offset];
            do {
                if ((h & mask) != 0)
                    return fromIndex;
                mask >>= 1;
                fromIndex--;
            } while (mask != 0);
            mask = 1 << 31;
            offset--;
        }
        return -1;
    }

    /**
     * Adds the specified integer to this set (corresponding bit is set to
     * {@code true}.
     *
     * @param bitIndex a non-negative integer.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex) {
        int i = bitIndex >> 5;
        ensureCapacity(i + 1);
        bits[i] |= 1L << bitIndex;
    }

    /**
     * Sets the bit at the given index to the specified value.
     *
     * @param bitIndex the position to set.
     * @param value    the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int bitIndex, boolean value) {
        if (value) {
            set(bitIndex);
        } else {
            clear(bitIndex);
        }
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param fromIndex index of the first bit to be set.
     * @param toIndex   index after the last bit to be set.
     * @throws IndexOutOfBoundsException if
     *                                   {@code (fromIndex < 0) | (toIndex < fromIndex)}
     */
    public void set(int fromIndex, int toIndex) {
        if ((fromIndex < 0) || (toIndex < fromIndex))
            throw new IndexOutOfBoundsException();
        int i = fromIndex >>> 5;
        int j = toIndex >>> 5;
        ensureCapacity(j + 1);
        if (i == j) {
            bits[i] |= (-1 << fromIndex) & ((1 << toIndex) - 1);
            return;
        }
        bits[i] |= -1 << fromIndex;
        bits[j] |= (1 << toIndex) - 1;
        for (int k = i + 1; k < j; k++) {
            bits[k] = -1;
        }
    }

    /**
     * Sets the bits between from (inclusive) and to (exclusive) to the
     * specified value.
     *
     * @param fromIndex the start range (inclusive).
     * @param toIndex   the end range (exclusive).
     * @param value     the value to set it to.
     * @throws IndexOutOfBoundsException if {@code bitIndex < 0}
     */
    public void set(int fromIndex, int toIndex, boolean value) {
        if (value) {
            set(fromIndex, toIndex);
        } else {
            clear(fromIndex, toIndex);
        }
    }

    /**
     * Returns the minimal length <code>long[]</code> representation of this bitset.
     *
     * @return Array of longs representing this bitset
     */
    public int[] toIntArray() {
        trim();
        return bits;
    }

    /**
     * Performs the logical XOR operation on this bit set and the one specified.
     * In other words, builds the symmetric remainder of the two sets
     * (the elements that are in one set, but not in the other).
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public void applyXor(BitSet that) {
        int[] thatBits = (that instanceof BitSet) ? ((BitSet) that).bits
                : that.toIntArray();
        ensureCapacity(thatBits.length);
        for (int i = thatBits.length; --i >= 0; ) {
            bits[i] ^= thatBits[i];
        }
    }

    /**
     * Performs the logical XOR operation on this bit set and the one specified.
     * In other words, builds the symmetric remainder of the two sets
     * (the elements that are in one set, but not in the other).
     * The result is stored into this bit set.
     *
     * @param that the second bit set.
     */
    public BitSet xor(BitSet that) {
        int[] thatBits = (that instanceof BitSet) ? ((BitSet) that).bits
                : that.toIntArray();
        int[] result = Arrays.copyOf(bits, bits.length);
        ensureCapacity(thatBits.length);
        for (int i = thatBits.length; --i >= 0; ) {
            result[i] ^= thatBits[i];
        }
        return new BitSet(result);
    }

    // Checks capacity.
    private void ensureCapacity(int capacity) {
        if (bits.length < capacity) {
            bits = Arrays.copyOf(bits, max(bits.length * 2, capacity));
        }
    }

    public static int max(int x, int y) {
        return (x >= y) ? x : y;
    }

    // Removes trailing zeros.
    protected void trim() {
        int n = bits.length;
        while ((--n >= 0) && (bits[n] == 0L)) {
        }
        if (++n != bits.length) { // Trim.
            bits = Arrays.copyOf(bits, n);
        }
    }


    public IntIterator iterator() {
        return new BitSetIteratorImpl(this, 0, false);
    }

    public IntIterator iterator(int fromElement) {
        return new BitSetIteratorImpl(this, fromElement, false);
    }


    public IntIterator descendingIterator(int fromElement) {
        return new BitSetIteratorImpl(this, fromElement, true);
    }


    public IntIterator descendingIterator() {
        return new BitSetIteratorImpl(this, this.length() - 1, true);
    }


}
