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

package paw.old.tokeniser.tokenisation.pl.cpp;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import paw.old.tokeniser.TokenizedString;
import paw.old.tokeniser.Tokenizer;
import paw.old.tokeniser.tokenisation.TokenizerType;
import paw.old.tokeniser.tokenisation.pl.cpp.antlr.CPP14Lexer;
import paw.old.utils.LowerString;
import paw.old.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

*/
/**
 * A CPP tokenizer
 * relying on antlr CPP14 grammar
 * if unpreprocessed C file use this class
 *
 * Warning comments are removed
 *//*

public class CPPTokenizer extends Tokenizer {

    public final static String ID = "CPP TOKENIZER";

    @Override
    public TokenizedString tokenize(Reader reader) throws IOException {
        final Map<Integer, LowerString> tokens = new HashMap<>();
        final Map<Integer, Integer> delimiter = new HashMap<>();
        final Map<Integer, Integer> ints = new HashMap<>();
        final Map<Integer, String> outcast = new HashMap<>();
        ANTLRInputStream inputStream = new ANTLRInputStream(reader);
        CPP14Lexer lexer = new CPP14Lexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        commonTokenStream.fill();
        List<Token> list = commonTokenStream.getTokens();
        for (int i = 0; i < list.size(); i++) {
            String res = list.get(i).getText();
            if (Utils.isNumericArray(res)) {
                int number = Integer.parseInt(res);
                ints.put(i, number);

            } else {
                if (res.length() == 1 && !Character.isAlphabetic(res.codePointAt(0))) {
                    delimiter.put(i, res.codePointAt(0));
                } else {
                    tokens.put(i, new LowerString(res));
                }
            }
        }
        return new TokenizedString(tokens, ints, delimiter, outcast, list.size());
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }

    @Override
    public byte getType() {
        return TokenizerType.CPP;
    }
}
*/
