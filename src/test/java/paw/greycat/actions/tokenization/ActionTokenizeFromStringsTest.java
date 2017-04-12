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

import greycat.TaskResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;
import paw.tokeniser.tokenisation.TokenizerType;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static paw.greycat.actions.Pawctions.createTokenizer;
import static paw.greycat.actions.Pawctions.tokenizeFromStrings;

@SuppressWarnings("Duplicates")
class ActionTokenizeFromStringsTest extends ActionTest{

    @BeforeEach
    void setUp() {
        initGraph();
    }

    @AfterEach
    void tearDown() {
        removeGraph();
    }

    @Test
    void tokenizeFromSingleStringInVar() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .then(tokenizeFromStrings("tokenizer", "this is me, and you."))
                .thenDo(ctx -> {
                    assertEquals(1, ctx.result().size());
                    TaskResult<String> result = (TaskResult<String>) ctx.result().get(0);
                    assertEquals(11, result.size());
                    assertEquals(",", result.get(5));
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void tokenizeFromSeveralStringInVar() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .then(tokenizeFromStrings("tokenizer", "this is me, and you.", "and this is another sentence."))
                .thenDo(ctx -> {
                    assertEquals(2, ctx.result().size());
                    TaskResult<String> result = (TaskResult<String>) ctx.result().get(1);
                    assertEquals(10, result.size());
                    assertEquals(".", result.get(9));
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

}