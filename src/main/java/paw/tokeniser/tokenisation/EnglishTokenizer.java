package paw.tokeniser.tokenisation;

import paw.tokeniser.Tokenizer;

import java.io.IOException;
import java.io.Reader;

public class EnglishTokenizer extends Tokenizer{
    public final static String ID = "ENGLISH TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {
        return new String[0];
    }
}
