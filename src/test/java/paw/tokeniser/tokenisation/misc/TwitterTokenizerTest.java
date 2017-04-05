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
package paw.tokeniser.tokenisation.misc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.tokeniser.preprocessing.LowerCasePreprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TwitterTokenizerTest {
    private TwitterTokenizer tokenizer;

    @BeforeEach
    void buildTokenizer(){
        this.tokenizer = new TwitterTokenizer();
        tokenizer.addPreprocessor(new LowerCasePreprocessor());
        tokenizer.setKeepDelimiter(true);
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
        Assertions.assertEquals(3, result.length);
        Assertions.assertEquals("this",result[0]);
    }

    @Test
    void twoLinesText(){
        String[] result = tokenizer.tokenize("this is \n me");
        Assertions.assertEquals(7, result.length);
        Assertions.assertEquals("\n", result[4]);
    }

    @Test
    void tweet(){
        String[] result = tokenizer.tokenize("What can you do with LSTM? \"The magic of LSTM neural networks\" by @assaadm http://buff.ly/2m0b2pm  #neuralnetworks #machinelearning");
        Assertions.assertEquals(43, result.length);
    }
}