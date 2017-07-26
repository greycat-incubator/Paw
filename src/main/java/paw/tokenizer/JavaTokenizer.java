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

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import paw.PawConstants;
import paw.tokenizer.token.ContentT;
import paw.tokenizer.token.NumberT;
import paw.tokenizer.token.Token;
import paw.tokenizer.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.Providers.provider;

public class JavaTokenizer extends AbstractTokenizer {


    @Override
    public List<Token> tokenize(Reader reader) throws IOException {
        JavaParser jp = new JavaParser(JavaParser.getStaticConfiguration());
        ParseResult<CompilationUnit> result = jp.parse(COMPILATION_UNIT, provider(reader));
        List<Token> tokens = new ArrayList<>();
        if (result.isSuccessful()) {
            List<JavaToken> l = result.getTokens().get();
            for (int i = 0; i < l.size(); i++) {
                JavaToken jt = l.get(i);
                String res = jt.getText();
                if (Utils.isNumericArray(res) && !res.startsWith("0")) {
                    int number = Integer.parseInt(res);
                    tokens.add(new NumberT(number));
                } else {
                    tokens.add(new ContentT(res));
                }
            }
        }
        return tokens;
    }

    @Override
    public byte getType() {
        return PawConstants.JAVA_TOKENIZER;
    }
}
