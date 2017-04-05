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
package paw.greycat.actions.vocabulary;

import greycat.Node;
import greycat.TaskResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static paw.PawConstants.NODE_NAME;
import static paw.greycat.actions.Pawctions.getOrCreateTokensFromStrings;

@SuppressWarnings("Duplicates")
class ActionGetOrCreateTokensFromStringsTest extends ActionTest {

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
                .then(getOrCreateTokensFromStrings("Token7"))
                .thenDo(ctx -> {
                    TaskResult<Node> tok = ctx.resultAsNodes();
                    assertEquals(1, tok.size());
                    Node n = tok.get(0);
                    assertEquals("Token7", n.get(NODE_NAME));
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
                .then(getOrCreateTokensFromStrings("Token", "Token2", "Token3", "Token4"))
                .println("{{result}}")
                .thenDo(ctx -> {
                    TaskResult<Node> tok = ctx.resultAsNodes();
                    assertEquals(4, tok.size());
                    Node n = tok.get(0);
                    assertEquals("Token", n.get(NODE_NAME));
                    Node n1 = tok.get(1);
                    assertEquals("Token2", n1.get(NODE_NAME));
                    Node n2 = tok.get(2);
                    assertEquals("Token3", n2.get(NODE_NAME));
                    Node n3 = tok.get(3);
                    assertEquals("Token4", n3.get(NODE_NAME));
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
                .then(getOrCreateTokensFromStrings("Token4"))
                .thenDo(ctx -> {
                    i[0]++;
                    ctx.continueWith(ctx.wrap(ctx.resultAsNodes().get(0).id()));
                })
                .defineAsVar("id")
                .then(getOrCreateTokensFromStrings("Token4"))
                .thenDo(context -> context.continueWith(context.wrap(context.resultAsNodes().get(0).id())))
                .thenDo(ctx -> {
                    i[0]++;
                    assertEquals(ctx.longVar("id"), ctx.longResult());
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void retrieveSeveralAlreadyExistingToken() {
        int counter = 2;
        final int[] i = {0};
        newTask()
                .then(getOrCreateTokensFromStrings("Token", "Token2", "Token3", "Token4"))
                .thenDo(ctx -> {
                    Long[] ids = new Long[ctx.resultAsNodes().size()];
                    i[0]++;
                    TaskResult<Node> nodes = ctx.resultAsNodes();
                    int size = nodes.size();
                    for (int i1 = 0; i1 < size; i1++) {
                        ids[i1] = nodes.get(i1).id();
                    }
                    ctx.continueWith(ctx.wrap(ids));
                })
                .defineAsVar("ids")
                .then(getOrCreateTokensFromStrings("Token", "Token2", "Token3", "Token4"))
                .thenDo(ctx -> {
                    assertEquals(ctx.variable("ids").get(0), ctx.resultAsNodes().get(0).id());
                    assertEquals(ctx.variable("ids").get(1), ctx.resultAsNodes().get(1).id());
                    assertEquals(ctx.variable("ids").get(2), ctx.resultAsNodes().get(2).id());
                    assertEquals(ctx.variable("ids").get(3), ctx.resultAsNodes().get(3).id());
                    i[0]++;
                    ctx.continueTask();
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void mix() {
        int counter = 2;
        final int[] i = {0};
        newTask()
                .then(getOrCreateTokensFromStrings("Token", "Token2", "Token3", "Token4"))
                .thenDo(ctx -> {
                    Long[] ids = new Long[ctx.resultAsNodes().size()];

                    TaskResult<Node> nodes = ctx.resultAsNodes();
                    int size = nodes.size();
                    for (int i1 = 0; i1 < size; i1++) {
                        ids[i1] = nodes.get(i1).id();
                    }
                    i[0]++;
                    ctx.continueWith(ctx.wrap(ids));
                })
                .defineAsVar("ids")
                .then(getOrCreateTokensFromStrings("Token", "Token5", "Token3", "Token7"))
                .thenDo(ctx -> {
                    i[0]++;
                    assertEquals(ctx.variable("ids").get(0), ctx.resultAsNodes().get(0).id());
                    assertNotEquals(ctx.variable("ids").get(1), ctx.resultAsNodes().get(1).id());
                    assertEquals(ctx.variable("ids").get(2), ctx.resultAsNodes().get(2).id());
                    assertNotEquals(ctx.variable("ids").get(3), ctx.resultAsNodes().get(3).id());
                    ctx.continueTask();
                })
                //.addHook(new VerboseHook())
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }
}