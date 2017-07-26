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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import paw.PawConstants;
import paw.tokenizer.cpp.CPP14Lexer;
import paw.tokenizer.token.ContentT;
import paw.tokenizer.token.NumberT;
import paw.tokenizer.token.Token;
import paw.tokenizer.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CPPTokenizer extends AbstractTokenizer {
    @Override
    public List<Token> tokenize(Reader reader) throws IOException {
        List<Token> tokens = new ArrayList<>();
        ANTLRInputStream inputStream = new ANTLRInputStream(reader);
        CPP14Lexer lexer = new CPP14Lexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        commonTokenStream.fill();
        List<org.antlr.v4.runtime.Token> list = commonTokenStream.getTokens();
        for (int i = 0; i < list.size(); i++) {
            String res = list.get(i).getText();
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
        return PawConstants.CPP_TOKENIZER;
    }
}
