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
package paw.tokeniser.tokenisation.misc;

import paw.tokeniser.TokenizedString;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.TokenizerType;
import paw.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Identity Tokenizer
 *
 * Tokenizer that just return the content of the reader without tokenizing it just apply all register preprocessor
 */
public class IdentityTokenizer extends Tokenizer {

    public final static String ID = "IDENTITY TOKENIZER";

    @Override
    public TokenizedString tokenize(Reader reader) throws IOException {

        final StringBuilder sb = new StringBuilder();
        int ch;
        try {
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        String s = sb.toString();

        return tokenize(s);
    }

    /**
     * As it is an identity tokenizer, i.e., without real tokenization the tokenize String method can ba overwritten
     *
     * @param string to tokenize
     * @return the same string but with all preprocessor applied.
     */
    @Override
    public TokenizedString tokenize(String string) {
        String s = applyAllTokenPreprocessorTo(string);

        if (s.length() != 0) {

            if (Utils.isNumericArray(s)) {
                try{
                    int integer = Integer.parseInt(s);
                    Map<Integer, Integer> integers = new HashMap<>(1);
                    integers.put(0, integer);
                    return new TokenizedString(null, integers, null, null, 1);
                }catch (NumberFormatException e){
                    Map<Integer, String> outcasts = new HashMap<>(1);
                    outcasts.put(0,s);
                    return new TokenizedString(null, null, null, outcasts, 1);
                }
            } else {
                Map<Integer, String> onlyTokenMap = new HashMap<>(1);
                onlyTokenMap.put(0, s);
                return new TokenizedString(onlyTokenMap, null, null, null, 1);
            }
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }

    @Override
    public byte getType() {
        return TokenizerType.IDENTITY;
    }
}
