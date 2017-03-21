package paw.tokeniser.tokenisation.pl.c;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.pl.c.antlr.CLexer;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class CTokenizer extends Tokenizer {

    public final static String ID = "C TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {
        ANTLRInputStream inputStream = new ANTLRInputStream(reader);
        CLexer lexer = new CLexer(inputStream);
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
