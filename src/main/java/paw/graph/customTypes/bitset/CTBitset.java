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
