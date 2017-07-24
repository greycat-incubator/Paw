package paw.graph.customTypes.tokenizedContent;

import greycat.Type;
import greycat.struct.EStructArray;
import greycat.utility.HashHelper;
import paw.graph.customTypes.bitset.roaring.CTRoaringBitMap;

import java.util.List;

@SuppressWarnings("Duplicates")
public class CTTCRoaring extends CTRoaringBitMap implements InterfaceTokenizeContent {
    public final static String NAME = "ROARING_ENCODED";

    private static final String CURRENTSTOP = "cs";
    private static final int CURRENTSTOP_H = HashHelper.hash(CURRENTSTOP);

    private int currentStop;
    private boolean dirty = false;


    public CTTCRoaring(EStructArray backend) {
        super(backend);
        Object result = root.getAt(CURRENTSTOP_H);
        if (result == null) {
            currentStop = 0;
        } else {
            currentStop = (int) result;
        }
    }

    @Override
    public void clear() {
        currentStop = 0;
        root.setAt(CURRENTSTOP_H, Type.INT, 0);
        super.clear();
        dirty = true;
    }

    @Override
    public void save() {
        if (dirty) {
            root.setAt(CURRENTSTOP_H, Type.INT, currentStop);
            super.save();
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void addWord(Word word) {
        currentStop = CTTokenizeContent.addWord(word, this, currentStop);
        dirty = true;
    }

    public List<Word> decodeWords() {
        return CTTokenizeContent.decodeWords(this);
    }
}
