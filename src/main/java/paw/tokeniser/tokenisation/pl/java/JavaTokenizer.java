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
package paw.tokeniser.tokenisation.pl.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.TokenizerType;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.Providers.provider;

/**
 * A Java tokenizer
 * relying on javaParser library
 *
 * Not working on anything else than java file!
 */
public class JavaTokenizer extends Tokenizer {
    public final static String ID = "JAVA TOKENIZER";

    private boolean removeComments = false;

    @Override
    public String[] tokenize(Reader reader) throws IOException {

        JavaParser jp = new JavaParser(JavaParser.getStaticConfiguration());
        ParseResult<CompilationUnit> result = jp.parse(COMPILATION_UNIT, provider(reader));
        if (result.isSuccessful()) {
            List<JavaToken> l = result.getTokens().get();
            List<String> tokens = new ArrayList<>(l.size());
            for (int i = 0; i < l.size(); i++) {
                JavaToken jt = l.get(i);

                String s = applyAllTokenPreprocessorTo(jt.getText());
                if (removeComments && (s.contains("/**") || s.contains("//")))
                    s = "";
                if (!isKeepingDelimiterActivate()) {
                    if (!s.contains("\n"))
                        s = s.trim();
                    if (!s.isEmpty()) {
                        tokens.add(s);
                    }
                } else {
                    tokens.add(s);
                }
            }
            return tokens.toArray(new String[tokens.size()]);
        }
        return new String[0];
    }

    public boolean isRemoveComments() {
        return removeComments;
    }

    public void setRemoveComments(boolean removeComments) {
        this.removeComments = removeComments;
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
