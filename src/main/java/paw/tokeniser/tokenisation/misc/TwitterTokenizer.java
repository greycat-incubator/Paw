package paw.tokeniser.tokenisation.misc;

import paw.tokeniser.Tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A twitter tokenizer implementation
 * based on Terrier Twitter tokenizer
 *
 * The tokenizer keep utf-8 encoding and mentions
 */
public class TwitterTokenizer extends Tokenizer {

    public final static String ID = "TWITTER TOKENIZER";

    private final static int maxNumberOfDigitPerTerm = 10;
    private final static int maxNumOfSameConseqLetterPerTerm = 10;
    private final static int maxWordLength = 140;
    private final static boolean DROP_LONG_TOKENS = true;


    @Override
    public String[] tokenize(Reader reader) throws IOException {
        List<String> tokens = new ArrayList<>();
        int ch = reader.read();
        while (ch != -1) {

            while (ch != -1 && !(ch == '/') && !(ch == '@') && !(Character.isLetterOrDigit((char) ch) || Character.getType((char) ch) == Character.NON_SPACING_MARK || Character.getType((char) ch) == Character.COMBINING_SPACING_MARK)
                    )

            {
                if (isKeepingDelimiterActivate())
                    tokens.add(applyAllTokenPreprocessorTo(String.valueOf((char) ch)));
                ch = reader.read();
            }
            StringBuilder sw = new StringBuilder(maxWordLength);
            while (ch != -1 && (Character.isLetterOrDigit((char) ch) || Character.getType((char) ch) == Character.NON_SPACING_MARK || Character.getType((char) ch) == Character.COMBINING_SPACING_MARK || ch == '/' || ch == '@')) {
                sw.append((char) ch);
                ch = reader.read();
            }
            if (sw.length()>0 && (sw.length() < maxWordLength || !DROP_LONG_TOKENS)) {
                sw.setLength(maxWordLength);
                String s = check(sw.toString());
                tokens.add(applyAllTokenPreprocessorTo(s));
            }
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    @SuppressWarnings("Duplicates")
    static String check(String s) {
        s = s.trim();
        final int length = s.length();
        int counter = 0;
        int counterdigit = 0;
        int ch = -1;
        int chNew;
        for (int i = 0; i < length; i++) {
            chNew = s.charAt(i);
            if (Character.isDigit(chNew))
                counterdigit++;
            if (ch == chNew)
                counter++;
            else
                counter = 1;
            ch = chNew;
            if (counter > maxNumOfSameConseqLetterPerTerm
                    || counterdigit > maxNumberOfDigitPerTerm)
                return "";
        }
        return s;
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }
}
