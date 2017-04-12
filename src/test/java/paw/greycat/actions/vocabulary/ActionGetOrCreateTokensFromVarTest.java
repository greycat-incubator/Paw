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
package paw.greycat.actions.vocabulary;

import greycat.Node;
import greycat.TaskResult;
import greycat.struct.EGraph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;
import paw.greycat.indexing.WordIndex;
import paw.greycat.indexing.radix.RadixTree;

import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.injectAsVar;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static paw.PawConstants.VOCABULARY;
import static paw.greycat.actions.Pawctions.getOrCreateTokensFromVar;
import static paw.greycat.tasks.VocabularyTasks.VOCABULARY_VAR;

@SuppressWarnings("Duplicates")
class ActionGetOrCreateTokensFromVarTest extends ActionTest {
    @BeforeEach
    void setUp() {
        initGraph();
    }

    @AfterEach
    void tearDown() {
        removeGraph();
    }


    @Test
    void createOneToken() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token7"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    Node node = (Node) ctx.variable(VOCABULARY_VAR).get(0);
                    EGraph eGraph = (EGraph) node.get(VOCABULARY);
                    WordIndex wordIndex = new RadixTree(eGraph);
                    assertEquals("Token7", wordIndex.getNameOfToken(ctx.intResult()));

                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void createSeveralTokens() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token", "Token2", "Token3", "Token4"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    Node node = (Node) ctx.variable(VOCABULARY_VAR).get(0);
                    EGraph eGraph = (EGraph) node.get(VOCABULARY);
                    WordIndex wordIndex = new RadixTree(eGraph);
                    TaskResult tok = ctx.result();
                    assertEquals(4, tok.size());

                    assertEquals("Token", wordIndex.getNameOfToken((int) tok.get(0)));

                    assertEquals("Token2", wordIndex.getNameOfToken((int) tok.get(1)));

                    assertEquals("Token3", wordIndex.getNameOfToken((int) tok.get(2)));

                    assertEquals("Token4", wordIndex.getNameOfToken((int) tok.get(3)));
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void retrieveOneAlreadyExistingToken() {
        int counter = 2;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token4"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    i[0]++;
                    ctx.continueWith(ctx.wrap(ctx.result().get(0)));
                })
                .defineAsVar("id")
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    i[0]++;
                    assertEquals(ctx.intVar("id"), ctx.intResult());
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void retrieveSeveralAlreadyExistingToken() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token", "Token2", "Token3", "Token4"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .defineAsVar("ids")
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    assertEquals(ctx.variable("ids").get(0), ctx.result().get(0));
                    assertEquals(ctx.variable("ids").get(1), ctx.result().get(1));
                    assertEquals(ctx.variable("ids").get(2), ctx.result().get(2));
                    assertEquals(ctx.variable("ids").get(3), ctx.result().get(3));
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void mix() {
        int counter = 1;
        final int[] i = {0};
        newTask()
                .then(injectAsVar("mytok", new String[]{"Token", "Token2", "Token3", "Token4"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .defineAsVar("ids")
                .then(injectAsVar("mytok", new String[]{"Token", "Token5", "Token3", "Token7"}))
                .then(getOrCreateTokensFromVar("mytok"))
                .thenDo(ctx -> {
                    i[0]++;
                    assertEquals(ctx.variable("ids").get(0), ctx.result().get(0));
                    assertNotEquals(ctx.variable("ids").get(1), ctx.result().get(1));
                    assertEquals(ctx.variable("ids").get(2), ctx.result().get(2));
                    assertNotEquals(ctx.variable("ids").get(3), ctx.result().get(3));
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

}