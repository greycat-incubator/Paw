package paw.tokeniser.tokenisation;

import paw.tokeniser.Tokenizer;

import java.io.IOException;
import java.io.Reader;

public class SimpleTokenizer extends Tokenizer{

    public final static String ID = "SIMPLE TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {
        return new String[0];
    }
}
