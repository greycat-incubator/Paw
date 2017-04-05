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
package paw.tokeniser.tokenisation.pl.c;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.TokenizerType;
import paw.tokeniser.tokenisation.pl.c.antlr.CLexer;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

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
    public String[] tokenize(Reader reader) throws IOException {
        ANTLRInputStream inputStream = new ANTLRInputStream(reader);
        CLexer lexer = new CLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        commonTokenStream.fill();
        List<Token> list = commonTokenStream.getTokens();
        String[] result = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = applyAllTokenPreprocessorTo(list.get(i).getText());
        }
        return result;
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
