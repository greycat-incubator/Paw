package paw.tokeniser.tokenisation.misc;

import paw.tokeniser.Tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Tokenizer
 *
 * Tokenizer that just split according to space
 * and apply the preprocessor on each tokens
 */
public class SimpleTokenizer extends Tokenizer {

    public final static String ID = "SIMPLE TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {
        final List<String> tokens = new ArrayList<>();
        int ch;
        StringBuilder sw = new StringBuilder();
        while ((ch = reader.read()) != -1) {
            if (!Character.isSpaceChar((char) ch)) {
                sw.append((char) ch);
            } else {
                tokens.add(applyAllTokenPreprocessorTo(sw.toString()));
                sw = new StringBuilder();
            }
        }
        if (sw.length() != 0)
            tokens.add(applyAllTokenPreprocessorTo(sw.toString()));
        return tokens.toArray(new String[tokens.size()]);
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }
}
