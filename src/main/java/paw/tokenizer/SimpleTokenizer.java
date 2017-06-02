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

@SuppressWarnings("Duplicates")
public class SimpleTokenizer extends AbstractTokenizer {
    @Override
    public List<Token> tokenize(Reader reader) throws IOException {
        final List<Token> tokens = new ArrayList<>();
        int ch;
        StringBuilder sw = new StringBuilder();
        while ((ch = reader.read()) != -1) {
            if (!Character.isSpaceChar((char) ch)) {
                sw.append((char) ch);
            } else {
                if (sw.length() > 0) {
                    String s = sw.toString();
                    if (Utils.isNumericArray(s) && !s.startsWith("0")) {
                        int number = Integer.parseInt(s);
                        tokens.add(new NumberT(number));
                    } else {
                        tokens.add(new ContentT(s));
                    }
                    sw = new StringBuilder();
                }
            }
        }
        if (sw.length() != 0) {
            String s = sw.toString();
            if (Utils.isNumericArray(s) && !s.startsWith("0")) {
                int number = Integer.parseInt(s);
                tokens.add(new NumberT(number));

            } else {
                tokens.add(new ContentT(s));
            }
        }
        return tokens;
    }

    @Override
    public List<Token> tokenize(String s) {
        final List<Token> tokens = new ArrayList<>();
        int i = 0;
        StringBuilder sw = new StringBuilder();
        while (i < s.length()) {
            if (!Character.isSpaceChar(s.charAt(i))) {
                sw.append(s.charAt(i));
            } else {
                if (sw.length() > 0) {
                    String res = sw.toString();
                    if (Utils.isNumericArray(res) && !res.startsWith("0")) {
                        int number = Integer.parseInt(res);
                        tokens.add(new NumberT(number));
                    } else {
                        tokens.add(new ContentT(res));
                    }
                    sw = new StringBuilder();
                }
            }
            i++;
        }
        if (sw.length() != 0) {
            String res = sw.toString();
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
        return PawConstants.SIMPLE_TOKENIZER;
    }
}
