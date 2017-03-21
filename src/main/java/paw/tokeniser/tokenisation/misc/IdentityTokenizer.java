package paw.tokeniser.tokenisation.misc;

import paw.tokeniser.Tokenizer;

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

        return new String[]{s};
    }

    @Override
    public String[] tokenize(String string) {
        return new String[]{applyAllTokenPreprocessorTo(string)};
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }
}
