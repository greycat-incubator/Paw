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

package paw.old.tokeniser.tokenisation.pl.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import paw.old.tokeniser.TokenizedString;
import paw.old.tokeniser.Tokenizer;
import paw.old.tokeniser.tokenisation.TokenizerType;
import paw.old.utils.LowerString;
import paw.old.utils.Utils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.Providers.provider;

*/
/**
 * A Java tokenizer
 * relying on javaParser library
 *
 * Not working on anything else than java file!
 *//*

public class JavaTokenizer extends Tokenizer {
    public final static String ID = "JAVA TOKENIZER";

    @Override
    public TokenizedString tokenize(Reader reader) throws IOException {
        final Map<Integer, LowerString> tokens = new HashMap<>();
        final Map<Integer, Integer> delimiter = new HashMap<>();
        final Map<Integer, Integer> ints = new HashMap<>();
        final Map<Integer, String> outcast = new HashMap<>();

        JavaParser jp = new JavaParser(JavaParser.getStaticConfiguration());
        ParseResult<CompilationUnit> result = jp.parse(COMPILATION_UNIT, provider(reader));
        if (result.isSuccessful()) {
            List<JavaToken> l = result.getTokens().get();
            for (int i = 0; i < l.size(); i++) {
                JavaToken jt = l.get(i);
                String res = jt.getText();
                if (res.contains("*/
/**") || res.contains("//")) {
                    outcast.put(i, res);
                } else {
                    if (Utils.isNumericArray(res)) {
                        try {
                            int number = Integer.parseInt(res);
                            ints.put(i, number);
                        } catch (NumberFormatException e) {
                            outcast.put(i, res);
                        }
                    } else {
                        if (res.length() == 1 && !Character.isAlphabetic(res.codePointAt(0))) {
                            delimiter.put(i, res.codePointAt(0));
                        } else {
                            tokens.put(i, new LowerString(res));
                        }
                    }
                }
            }
            return new TokenizedString(tokens, ints, delimiter, outcast, l.size());
        }
        return null;
    }

    @Override
    public String toString() {
        return ID + "\n" + super.toString();
    }

    @Override
    public byte getType() {
        return TokenizerType.JAVA;
    }
}
*/
