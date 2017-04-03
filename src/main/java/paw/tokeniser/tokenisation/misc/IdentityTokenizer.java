package paw.tokeniser.tokenisation.misc;

import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.TokenizerType;

import java.io.IOException;
import java.io.Reader;

/**
 * Identity Tokenizer
 *
 * Tokenizer that just return the content of the reader without tokenizing it just apply all register preprocessor
 */
public class IdentityTokenizer extends Tokenizer {

    public final static String ID = "IDENTITY TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {

        final StringBuilder sb = new StringBuilder();
        int ch;
        try {
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        String s = sb.toString();

        s = applyAllTokenPreprocessorTo(s);

        if (s.length() != 0) {
            return new String[]{s};
        } else return new String[0];
    }

    /**
     * As it is an identity tokenizer, i.e., without real tokenization the tokenize String method can ba overwritten
     * @param string to tokenize
     * @return the same string but with all preprocessor applied.
     */
    @Override
    public String[] tokenize(String string) {
        if (string.length() == 0) {
            return new String[0];
        }
        return new String[]{applyAllTokenPreprocessorTo(string)};
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }

    @Override
    public byte getType() {
        return TokenizerType.IDENTITY;
    }
}
