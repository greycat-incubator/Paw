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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class JavaTokenizerTest {
    private final static String hello = "public class HelloWorld {\n" +
            "   public static void main(String[] args) {\n" +
            "      // Prints \"Hello, World\" in the terminal window.\n" +
            "      System.out.println(\"Hello, World\");\n" +
            "   }\n" +
            "}";

    private final static String not_java_prog = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";

    private JavaTokenizer tokenizer;

    @BeforeEach
    void buildTokenizer() {
        this.tokenizer = new JavaTokenizer();
    }

    @Test
    void jProg() {
        String[] result = tokenizer.tokenize(hello);
        System.out.println(Arrays.toString(result));
    }

    @Test
    void notJProg() {
        String[] result = tokenizer.tokenize(not_java_prog);
        System.out.println(Arrays.toString(result));
    }
}