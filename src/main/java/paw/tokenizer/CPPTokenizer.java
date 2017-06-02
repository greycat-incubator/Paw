package paw.tokenizer;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import paw.PawConstants;
import paw.tokenizer.cpp.CPP14Lexer;
import paw.tokenizer.token.ContentT;
import paw.tokenizer.token.NumberT;
import paw.tokenizer.token.Token;
import paw.tokenizer.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CPPTokenizer extends AbstractTokenizer {
    @Override
    public List<Token> tokenize(Reader reader) throws IOException {
        List<Token> tokens = new ArrayList<>();
        ANTLRInputStream inputStream = new ANTLRInputStream(reader);
        CPP14Lexer lexer = new CPP14Lexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        commonTokenStream.fill();
        List<org.antlr.v4.runtime.Token> list = commonTokenStream.getTokens();
        for (int i = 0; i < list.size(); i++) {
            String res = list.get(i).getText();
            if (Utils.isNumericArray(res) && !res.startsWith("0")) {
                int number = Integer.parseInt(res);
                tokens.add(new NumberT(number));
            } else {
                tokens.add(new ContentT(res));
            }
        }
        return tokens;
    }

    @Override
    public byte getType() {
        return PawConstants.CPP_TOKENIZER;
    }
}
