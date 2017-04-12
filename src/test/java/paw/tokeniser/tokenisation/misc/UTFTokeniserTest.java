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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.tokeniser.preprocessing.LowerCasePreprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UTFTokeniserTest {
    private UTFTokeniser tokenizer;

    @BeforeEach
    void buildTokenizer(){
        this.tokenizer = new UTFTokeniser();
        tokenizer.addPreprocessor(new LowerCasePreprocessor());

    }

    @Test
    void emptyText(){
        String[] result = tokenizer.tokenize("");
        assertEquals(0, result.length);
    }

    @Test
    void oneWordText(){
        String[] result = tokenizer.tokenize("this");
        Assertions.assertEquals(1,result.length);
        Assertions.assertEquals("this",result[0]);
    }


    @Test
    void twoWordText(){
        String[] result = tokenizer.tokenize("this is");
        Assertions.assertEquals(2,result.length);
        Assertions.assertEquals("this",result[0]);
    }

    @Test
    void twoLinesTextD(){
        tokenizer.setKeepDelimiter(true);
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(7, result.length);
        Assertions.assertEquals("\n", result[4]);
    }

    @Test
    void twoLinesText(){
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(3,result.length);
        Assertions.assertEquals("me",result[2]);
    }

    @Test
    void utfTest(){
        String[] result = tokenizer.tokenize("mais où est-ce-que ça se trouve?");
        Assertions.assertEquals(8,result.length);
        Assertions.assertEquals("où",result[1]);
    }
}