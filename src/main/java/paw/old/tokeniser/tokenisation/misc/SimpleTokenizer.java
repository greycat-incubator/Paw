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

package paw.old.tokeniser.tokenisation.misc;

import paw.old.tokeniser.TokenizedString;
import paw.old.tokeniser.Tokenizer;
import paw.old.tokeniser.tokenisation.TokenizerType;
import paw.old.utils.LowerString;
import paw.old.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

*/
/**
 * Simple Tokenizer
 *
 * Tokenizer that just split according to space
 * and apply the preprocessor on each tokens
 *
 * Options : Drop URL
 *//*

public class SimpleTokenizer extends Tokenizer {

    public final static String ID = "SIMPLE TOKENIZER";
    private final static boolean DROP_URL = true;


    @Override
    @SuppressWarnings("Duplicates")
    public TokenizedString tokenize(Reader reader) throws IOException {

        final Map<Integer, LowerString> tokens = new HashMap<>();
        final Map<Integer, Integer> delimiter = new HashMap<>();
        final Map<Integer, Integer> ints = new HashMap<>();
        final Map<Integer, String> outcast = new HashMap<>();

        int ch;
        StringBuilder sw = new StringBuilder();
        int index = 0;

        while ((ch = reader.read()) != -1) {
            if (!Character.isSpaceChar((char) ch)) {
                sw.append((char) ch);
            } else {
                if (sw.length() > 0) {
                    String s = sw.toString();
                    if (checkContent && check(s)) {
                        outcast.put(index, s);
                    } else {
                        if (Utils.isNumericArray(s)) {
                            if (s.length() > 8) {
                                int number = Integer.parseInt(s);
                                ints.put(index, number);
                            } else {
                                outcast.put(index, s);
                            }
                        } else {
                            tokens.put(index, new LowerString(s));
                        }
                    }
                    sw = new StringBuilder();
                }
                index++;
                delimiter.put(index, ch);
                index++;
            }
        }
        if (sw.length() != 0) {
            String s = sw.toString();
            if (isCheckContent() && check(s)) {
                outcast.put(index, s);
            } else {
                if (Utils.isNumericArray(s)) {
                    if (s.length() > 8) {
                        int number = Integer.parseInt(s);
                        ints.put(index, number);
                    } else {
                        outcast.put(index, s);
                    }
                } else {
                    tokens.put(index, new LowerString(s));
                }
            }
            index++;
        }

        return new TokenizedString(tokens, ints, delimiter, outcast, index);
    }

    @SuppressWarnings("Duplicates")
    private boolean check(String s) {
        boolean ok;
        if (DROP_URL) {
            Matcher matcher = patt.matcher(s);
            ok = matcher.matches();
        }
        return ok;
    }

    static Pattern patt = Pattern.compile("\\\\b(https?|ftp|file)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]");

    @Override
    public String toString() {
        return ID;
    }

    @Override
    public byte getType() {
        return TokenizerType.SIMPLE;
    }
}
*/
