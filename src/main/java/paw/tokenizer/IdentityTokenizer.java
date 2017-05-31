package paw.tokenizer;

import paw.PawConstants;
import paw.tokenizer.token.ContentT;
import paw.tokenizer.token.NumberT;
import paw.tokenizer.token.Token;
import paw.tokenizer.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class IdentityTokenizer extends AbstractTokenizer {
    @Override
    public List<Token> tokenize(Reader reader) throws IOException {
        StringBuilder sw = new StringBuilder();
        int ch = reader.read();
        while (ch != -1) {
            sw.append((char) ch);
            ch = reader.read();
        }
        return tokenize(sw.toString());
    }

    @Override
    public List<Token> tokenize(String s) {
        List<Token> tokens = new ArrayList<>(1);
        if (s.length() != 0) {
            if (Utils.isNumericArray(s) && !s.startsWith("0")) {
                long number = Long.parseLong(s);
                tokens.add(new NumberT(number));
                return tokens;

            } else {
                tokens.add(new ContentT(s));
                return tokens;
            }
        } else {
            return null;
        }
    }

    @Override
    public byte getType() {
        return PawConstants.IDENTITY_TOKENIZER;
    }
}
