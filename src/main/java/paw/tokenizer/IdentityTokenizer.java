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
                int number = Integer.parseInt(s);
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
