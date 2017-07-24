package paw.graph.customTypes.tokenizedContent;

public class Word {
    protected final byte type;
    protected final int wordID;
    protected int firstChar;


    public Word(byte type, int wordID) {
        this.type = type;
        this.wordID = wordID;
    }

    public Word(byte type, int wordID, int firstChar) {
        this.type = type;
        this.wordID = wordID;
        this.firstChar = firstChar;
    }

    public byte getType() {
        return type;
    }

    public int getWordID() {
        return wordID;
    }

    public int getFirstChar() {
        return firstChar;
    }
}
