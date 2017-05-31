package paw.tokenizer;

import paw.PawConstants;
import paw.tokenizer.token.ContentT;
import paw.tokenizer.token.DelimiterT;
import paw.tokenizer.token.NumberT;
import paw.tokenizer.token.Token;
import paw.tokenizer.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class TwitterTokenizer extends AbstractTokenizer {
    @Override
    public List<Token> tokenize(Reader reader) throws IOException {
        List<Token> tokens = new ArrayList<>();
        int ch = reader.read();
        StringBuilder sw;
        String s;
        while (ch != -1) {
        /*
         * Delimiters
         */
            sw = new StringBuilder();

            while (ch != -1 && !(ch == '/') && !(ch == '@') && !(Character.isLetterOrDigit((char) ch) || Character.getType((char) ch) == Character.NON_SPACING_MARK || Character.getType((char) ch) == Character.COMBINING_SPACING_MARK)
                    ) {
                sw.append(ch);
                ch = reader.read();
            }
            s = sw.toString();
            if (!s.trim().isEmpty()) {
                tokens.add(new DelimiterT(s));
            }
            /*
              ContentT
             */
            sw = new StringBuilder();
            while (ch != -1 && (Character.isLetterOrDigit((char) ch) || Character.getType((char) ch) == Character.NON_SPACING_MARK || Character.getType((char) ch) == Character.COMBINING_SPACING_MARK || ch == '/' || ch == '@')) {
                sw.append((char) ch);
                ch = reader.read();
            }
            s = sw.toString();
            if (s.length() != 0) {
                if (Utils.isNumericArray(s) && !s.startsWith("0")) {
                    long number = Long.parseLong(s);
                    tokens.add(new NumberT(number));
                } else {
                    tokens.add(new ContentT(s));
                }
            }
        }
        return tokens;
    }

    @Override
    public List<Token> tokenize(String s) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        StringBuilder sw;
        String res;
        while (i < s.length()) {
            /*
         * Delimiters
         */
            sw = new StringBuilder();
            while (i < s.length() && !(s.charAt(i) == '/') && !(s.charAt(i) == '@') && !(Character.isLetterOrDigit(s.charAt(i)) || Character.getType(s.charAt(i)) == Character.NON_SPACING_MARK || Character.getType(s.charAt(i)) == Character.COMBINING_SPACING_MARK)
                    ) {
                sw.append(s.charAt(i));
                i++;
            }
            res = sw.toString();
            if (!res.trim().isEmpty()) {
                tokens.add(new DelimiterT(res));
            }
            /*
              ContentT
             */
            sw = new StringBuilder();
            while (i < s.length() && (Character.isLetterOrDigit(s.charAt(i)) || Character.getType(s.charAt(i)) == Character.NON_SPACING_MARK || Character.getType(s.charAt(i)) == Character.COMBINING_SPACING_MARK || s.charAt(i) == '/' || s.charAt(i) == '@')) {
                sw.append(s.charAt(i));
                i++;
            }
            res = sw.toString();
            if (res.length() != 0) {
                if (Utils.isNumericArray(res) && !res.startsWith("0")) {
                    long number = Long.parseLong(res);
                    tokens.add(new NumberT(number));
                } else {
                    tokens.add(new ContentT(res));
                }
            }
        }
        return tokens;
    }

    @Override
    public byte getType() {
        return PawConstants.TWITTER_TOKENIZER;
    }
}
