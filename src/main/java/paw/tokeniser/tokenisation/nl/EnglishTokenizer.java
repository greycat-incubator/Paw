package paw.tokeniser.tokenisation.nl;

import paw.tokeniser.Tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class EnglishTokenizer extends Tokenizer {
    public final static String ID = "ENGLISH TOKENIZER";

    private final static int maxNumberOfDigitPerTerm = 4;
    private final static int maxNumOfSameConseqLetterPerTerm = 3;
    private final static int maxWordLength = 30;
    private final static boolean DROP_LONG_TOKENS = true;

    @Override
    public String[] tokenize(Reader reader) throws IOException {
        List<String> tokens = new ArrayList<>();
        int ch = reader.read();
        while (ch != -1) {

            if (isKeepingDelimiterActivate())
                while (ch != -1 && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z')
                        && (ch < '0' || ch > '9')
                         /* removed by Craig: && ch != '<' && ch != '&' */
                        ) {
                    if (!Character.isSpaceChar((char) ch))
                        tokens.add(String.valueOf((char) ch));
                    ch = reader.read();
                }

            StringBuilder sw = new StringBuilder(maxWordLength);
            while (ch != -1 && (
                    ((ch >= 'A') && (ch <= 'Z'))
                            || ((ch >= 'a') && (ch <= 'z'))
                            || ((ch >= '0') && (ch <= '9')))) {
                sw.append((char) ch);
                ch = reader.read();
            }
            if (sw.length() < maxWordLength || !DROP_LONG_TOKENS) {
                sw.setLength(maxWordLength);
                String s = check(sw.toString());
                tokens.add(applyAllTokenPreprocessorTo(s));
            }
        }
        return tokens.toArray(new String[tokens.size()]);
    }


    /**
     * Checks whether a term is shorter than the maximum allowed length,
     * and whether a term does not have many numerical digits or many
     * consecutive same digits or letters.
     *
     * @param s String the term to check if it is valid.
     * @return String the term if it is valid, otherwise it returns null.
     */
    static String check(String s) {
        s = s.trim();
        final int length = s.length();
        int counter = 0;
        int counterdigit = 0;
        int ch = -1;
        int chNew = -1;
        for (int i = 0; i < length; i++) {
            chNew = s.charAt(i);
            if (chNew >= 48 && chNew <= 57)
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


}
