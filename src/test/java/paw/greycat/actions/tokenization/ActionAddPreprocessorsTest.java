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
package paw.greycat.actions.tokenization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.preprocessing.LowerCasePreprocessor;
import paw.tokeniser.preprocessing.PreprocessorType;
import paw.tokeniser.preprocessing.UpperCasePreprocessor;
import paw.tokeniser.tokenisation.TokenizerType;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.*;
import static paw.greycat.actions.Pawctions.addPreprocessors;
import static paw.greycat.actions.Pawctions.createTokenizer;

class ActionAddPreprocessorsTest extends ActionTest{
    @BeforeEach
    void setUp() {
        initGraph();
    }

    @AfterEach
    void tearDown() {
        removeGraph();
    }

    @Test
    void addOnePreprocessor(){
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH,true))
                .then(addPreprocessors("tokenizer", PreprocessorType.LOWER_CASE))
                .thenDo(ctx -> {
                    Tokenizer tok = (Tokenizer) ctx.variable("tokenizer").get(0);
                    assertEquals(1,tok.getListOfPreprocessor().size());
                    assertTrue(tok.getListOfPreprocessor().get(0) instanceof LowerCasePreprocessor);
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void addSeveralPreprocessor(){
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH,true))
                .then(addPreprocessors("tokenizer", PreprocessorType.LOWER_CASE,PreprocessorType.UPPER_CASE))
                .thenDo(ctx -> {
                    Tokenizer tok = (Tokenizer) ctx.variable("tokenizer").get(0);
                    assertEquals(2,tok.getListOfPreprocessor().size());
                    assertTrue(tok.getListOfPreprocessor().get(1) instanceof UpperCasePreprocessor);
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void addNotExistingPreprocessor(){
        int counter = 0;
        final int[] i = {0};
        assertThrows(AssertionError.class,() -> newTask()
                .then(createTokenizer("tokenizer", (byte) 22,true))
                .thenDo(ctx -> {
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null));
        assertEquals(counter, i[0]);
    }
}