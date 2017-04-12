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

import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.TokenizerType;

import java.io.IOException;
import java.io.Reader;

/**
 * Identity Tokenizer
 *
 * Tokenizer that just return the content of the reader without tokenizing it just apply all register preprocessor
 */
public class IdentityTokenizer extends Tokenizer {

    public final static String ID = "IDENTITY TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {

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

        s = applyAllTokenPreprocessorTo(s);

        if (s.length() != 0) {
            return new String[]{s};
        } else return new String[0];
    }

    /**
     * As it is an identity tokenizer, i.e., without real tokenization the tokenize String method can ba overwritten
     * @param string to tokenize
     * @return the same string but with all preprocessor applied.
     */
    @Override
    public String[] tokenize(String string) {
        if (string.length() == 0) {
            return new String[0];
        }
        return new String[]{applyAllTokenPreprocessorTo(string)};
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
