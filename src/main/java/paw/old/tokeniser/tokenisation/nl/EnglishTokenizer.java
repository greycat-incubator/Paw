/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *//*

package paw.old.tokeniser.tokenisation.nl;

import paw.old.tokeniser.TokenizedString;
import paw.old.tokeniser.Tokenizer;
import paw.old.tokeniser.tokenisation.TokenizerType;
import paw.old.utils.LowerString;
import paw.old.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

*/
/**
 * An english Tokenizer
 * based on Terrier English tokenizer
 *//*

@SuppressWarnings("Duplicates")
public class EnglishTokenizer extends Tokenizer {
    public final static String ID = "ENGLISH TOKENIZER";

    private final static int maxNumberOfDigitPerTerm = 4;
    private final static int maxNumOfSameConseqLetterPerTerm = 3;
    private final static int maxWordLength = 30;
    private final static boolean DROP_LONG_TOKENS = true;
    private final static boolean MAX_NUMBER_LETTER = true;
    private final static boolean HASH = true;

    @Override
    public TokenizedString tokenize(Reader reader) throws IOException {
        final Map<Integer, LowerString> tokens = new HashMap<>();
        final Map<Integer, Integer> delimiter = new HashMap<>();
        final Map<Integer, Integer> ints = new HashMap<>();
        final Map<Integer, String> outcast = new HashMap<>();

        int index = 0;
        int ch = reader.read();
        while (ch != -1) {

            while (ch != -1 && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z')
                    && (ch < '0' || ch > '9')
                    ) {


                delimiter.put(index, ch);
                index++;

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
            String s = sw.toString();
            if (s.length() == 0) {
                if (isCheckContent() && check(s)) {
                    outcast.put(index, s);
                } else {
                    if (Utils.isNumericArray(s)) {
                            int number = Integer.parseInt(s);
                            ints.put(index, number);
                    } else {
                        tokens.put(index, new LowerString(s));
                    }
                }
                index++;
            }
        }
        return new TokenizedString(tokens, ints, delimiter, outcast, index);
    }


    */
/**
     * Checks whether a term is shorter than the maximum allowed length,
     * and whether a term does not have many numerical digits or many
     * consecutive same digits or letters.
     *
     * @param s String the term to check if it is valid.
     * @return String the term if it is valid, otherwise it returns null.
     *//*

    static boolean check(String s) {
        if (DROP_LONG_TOKENS) {
            if (s.length() > maxWordLength)
                return true;
        }
        if (MAX_NUMBER_LETTER) {
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
                    return true;
            }
        }
        if (HASH) {
            final int length = s.length();
            int counterCap = 0;
            int counterdigit = 0;
            int counterlow = 0;
            int ch;
            for (int i = 0; i < length; i++) {
                ch = s.charAt(i);
                if (Character.isDigit(ch))
                    counterdigit++;
                else if (Character.isUpperCase(ch))
                    counterCap++;
                if (Character.isLowerCase(ch))
                    counterlow++;
                if ((counterCap > 0 || counterlow > 0) && counterdigit > 0)
                    return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }

    @Override
    public byte getType() {
        return TokenizerType.ENGLISH;
    }


}
*/
