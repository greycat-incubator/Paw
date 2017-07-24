package paw.graph.customType.ctfastbitset;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class BitSetIteratorImpl implements Iterator<Integer> {

    private final BitSet that;
    private int nextIndex;
    private int currentIndex = -1;
    private boolean reversed;

    public BitSetIteratorImpl(BitSet that, int from, boolean reversed) {
        this.that = that;
        this.nextIndex = reversed ? that.previousSetBit(from) : that.nextSetBit(from);
        this.reversed = reversed;
    }

    public boolean hasNext() {
        return (nextIndex >= 0);
    }

    public Integer next() {
        if (nextIndex < 0)
            throw new NoSuchElementException();
        currentIndex = nextIndex;
        nextIndex = reversed ? that.previousSetBit(nextIndex - 1) : that.nextSetBit(nextIndex + 1);
        return currentIndex;
    }

    public void remove() {
        if (currentIndex < 0)
            throw new IllegalStateException();
        that.clear(currentIndex);
        currentIndex = -1;
    }

}