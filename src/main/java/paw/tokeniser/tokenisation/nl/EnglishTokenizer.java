/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package paw.tokeniser.tokenisation.nl;

import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.TokenizerType;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * An english Tokenizer
 * based on Terrier English tokenizer
 */
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

            while (ch != -1 && (ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z')
                    && (ch < '0' || ch > '9')
                    ) {
                if (isKeepingDelimiterActivate())
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
            if (sw.length() > 0 && (sw.length() < maxWordLength || !DROP_LONG_TOKENS)) {
                sw.setLength(maxWordLength);
                String s = check(sw.toString());
                if (s != null)
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
        int chNew;
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
                return null;
        }
        return s;
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
