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
package paw.greycat.actions.tokenization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.tokenisation.TokenizerType;
import paw.tokeniser.tokenisation.nl.EnglishTokenizer;
import paw.tokeniser.tokenisation.pl.java.JavaTokenizer;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.*;
import static paw.greycat.actions.Pawctions.createTokenizer;

class ActionCreateTokenizerTest extends ActionTest{

    @BeforeEach
    void setUp() {
        initGraph();
    }

    @AfterEach
    void tearDown() {
        removeGraph();
    }

    @Test
    void createATokenizer(){
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH,true))
                .thenDo(ctx -> {
                    Tokenizer tok = (Tokenizer) ctx.variable("tokenizer").get(0);
                    assertTrue(tok.isKeepingDelimiterActivate());
                    assertTrue(tok instanceof EnglishTokenizer);
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void createAnotherTokenizer(){
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.JAVA,false))
                .thenDo(ctx -> {
                    Tokenizer tok = (Tokenizer) ctx.variable("tokenizer").get(0);
                    assertFalse(tok.isKeepingDelimiterActivate());
                    assertTrue(tok instanceof JavaTokenizer);
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void createANotExistingTokenizer(){
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