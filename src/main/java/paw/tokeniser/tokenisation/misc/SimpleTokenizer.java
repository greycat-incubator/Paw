package paw.tokeniser.tokenisation.misc;

import paw.tokeniser.Tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class SimpleTokenizer extends Tokenizer {

    public final static String ID = "SIMPLE TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {
        final List<String> tokens = new ArrayList<>();
        int ch = 0;
        StringBuilder sw = new StringBuilder();
        while ((ch = reader.read()) != -1) {
            if (!Character.isSpaceChar((char) ch)) {
                sw.append((char) ch);
            } else {
                tokens.add(applyAllTokenPreprocessorTo(sw.toString()));
                sw = new StringBuilder();
            }
        }
        return tokens.toArray(new String[tokens.size()]);
    }
}
