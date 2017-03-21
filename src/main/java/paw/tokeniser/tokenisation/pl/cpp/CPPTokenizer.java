package paw.tokeniser.tokenisation.pl.cpp;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.pl.cpp.antlr.CPP14Lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * A CPP tokenizer
 * relying on antlr CPP14 grammar
 */
public class CPPTokenizer extends Tokenizer {

    public final static String ID = "CPP TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {
        ANTLRInputStream inputStream = new ANTLRInputStream(reader);
        CPP14Lexer lexer = new CPP14Lexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        List<Token> list = commonTokenStream.getTokens();
        String[] result = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = applyAllTokenPreprocessorTo(list.get(i).getText());
        }
        return result;
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }
}
