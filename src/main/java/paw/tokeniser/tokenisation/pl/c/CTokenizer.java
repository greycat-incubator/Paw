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
package paw.tokeniser.tokenisation.pl.c;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import paw.tokeniser.TokenizedString;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.TokenizerType;
import paw.tokeniser.tokenisation.pl.c.antlr.CLexer;
import paw.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A C tokenizer
 * relying on antlr C grammar
 * works on preprocessed files
 *
 * Comments are removed
 */
public class CTokenizer extends Tokenizer {

    public final static String ID = "C TOKENIZER";

    @Override
    public TokenizedString tokenize(Reader reader) throws IOException {
        final Map<Integer, String> tokens = new HashMap<>();
        final Map<Integer, Integer> delimiter = new HashMap<>();
        final Map<Integer, Integer> integerPosition = new HashMap<>();
        final Map<Integer, String> outcast = new HashMap<>();
        ANTLRInputStream inputStream = new ANTLRInputStream(reader);
        CLexer lexer = new CLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        commonTokenStream.fill();
        List<Token> list = commonTokenStream.getTokens();
        for (int i = 0; i < list.size(); i++) {
            String res = list.get(i).getText();
            if (Utils.isNumericArray(res)) {
                try {
                    int integer = Integer.parseInt(res);
                    integerPosition.put(i, integer);
                } catch (NumberFormatException e) {
                    outcast.put(i, res);
                }
            } else {
                if (res.length() == 1 && !Character.isAlphabetic(res.codePointAt(0))) {
                    delimiter.put(i, res.codePointAt(0));
                } else {
                    tokens.put(i, applyAllTokenPreprocessorTo(res));
                }
            }
        }
        return new TokenizedString(tokens, integerPosition, delimiter, outcast, list.size());
    }


    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }

    @Override
    public byte getType() {
        return TokenizerType.C;
    }
}
