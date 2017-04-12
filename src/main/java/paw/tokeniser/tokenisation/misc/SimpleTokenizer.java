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
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Tokenizer
 *
 * Tokenizer that just split according to space
 * and apply the preprocessor on each tokens
 */
public class SimpleTokenizer extends Tokenizer {

    public final static String ID = "SIMPLE TOKENIZER";

    @Override
    public String[] tokenize(Reader reader) throws IOException {
        final List<String> tokens = new ArrayList<>();
        int ch;
        StringBuilder sw = new StringBuilder();
        while ((ch = reader.read()) != -1) {
            if (!Character.isSpaceChar((char) ch)) {
                sw.append((char) ch);
            } else {
                tokens.add(applyAllTokenPreprocessorTo(sw.toString()));
                sw = new StringBuilder();
            }
        }
        if (sw.length() != 0)
            tokens.add(applyAllTokenPreprocessorTo(sw.toString()));
        return tokens.toArray(new String[tokens.size()]);
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }

    @Override
    public byte getType() {
        return TokenizerType.SIMPLE;
    }
}
