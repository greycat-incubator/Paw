package paw.tokeniser.tokenisation;

public class TokenizerType {

    /**
     * Primitive Types
     */
    public static final byte IDENTITY = 1;
    public static final byte SIMPLE = 2;
    public static final byte ENGLISH = 3;
    public static final byte UTF = 4;
    public static final byte TWITTER = 5;

    /**
     * Convert a Tokenizer type that represent a byte to a readable String representation
     *
     * @param p_type byte encoding a particular Tokenizer type
     * @return readable string representation of the type
     */
    public static String typeName(byte p_type) {
        switch (p_type) {
            case IDENTITY:
                return IdentityTokenizer.ID;
            case SIMPLE:
                return SimpleTokenizer.ID;
            case ENGLISH:
                return EnglishTokenizer.ID;
            case UTF:
                return UTFTokeniser.ID;
            case TWITTER:
                return TwitterTokenizer.ID;
            default:
                return "unknown";
        }
    }

    /**
     * Convert a String Representation of a tokenizer type to its byte representation
     *
     * @param name String representation of a Tokenizer
     * @return byte representation of the tokenization type
     */
    public static byte typeFromName(String name) {
        switch (name) {
            case IdentityTokenizer.ID:
                return IDENTITY;
            case SimpleTokenizer.ID:
                return SIMPLE;
            case EnglishTokenizer.ID:
                return ENGLISH;
            case UTFTokeniser.ID:
                return UTF;
            case TwitterTokenizer.ID:
                return TWITTER;
            default:
                return -1;
        }
    }


}
