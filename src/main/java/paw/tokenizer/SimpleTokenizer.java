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
 */
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
