package paw.tokeniser.tokenisation.misc;

import paw.tokeniser.Tokenizer;

import java.io.IOException;
import java.io.Reader;

public class UTFTokeniser extends Tokenizer {

    public final static String ID = "UTF TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {
        return new String[0];
    }
}
