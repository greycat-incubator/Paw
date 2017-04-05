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
package paw.greycat.actions.tokenizedcontent;

import greycat.Node;
import greycat.struct.IntArray;
import greycat.struct.LongLongMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paw.greycat.actions.ActionTest;
import paw.tokeniser.tokenisation.TokenizerType;

import java.util.Arrays;

import static greycat.Tasks.newTask;
import static org.junit.jupiter.api.Assertions.*;
import static paw.PawConstants.*;
import static paw.greycat.actions.Pawctions.*;

@SuppressWarnings("Duplicates")
class ActionUpdateOrCreateTokenizeRelationFromVarTest extends ActionTest {

    private static String text1 = "the apple was looking over the cloud";
    private static String text11 = "the strange apple was indeed looking at the cloud";

    @BeforeEach
    void setUp() {
        initGraph();
    }

    @AfterEach
    void tearDown() {
        removeGraph();
    }

    @Test
    void oneRelationOneText() {
        int counter = 4;
        final int[] i = {0};
        newTask()
                .travelInTime("0")
                .inject(text1)
                .defineAsVar("text1")
                .then(retrieveVocabularyNode())
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .readGlobalIndex("roots")
                .defineAsVar("nodevar")
                .then(updateOrCreateTokenizeRelationFromVar("tokenizer", "nodevar", "text1", "text1"))
                .traverse(RELATION_INDEX_NODE_TO_TOKENIZECONTENT)
                .defineAsVar("tokenizedContent")
                .thenDo(ctx -> {
                    assertEquals(1, ctx.resultAsNodes().size());
                    Node node = ctx.resultAsNodes().get(0);
                    assertEquals(TYPE_TOKEN_WITHOUT_TYPE, node.get(TOKENIZE_CONTENT_TYPE));
                    assertTrue((boolean) node.get(TOKENIZE_CONTENT_DELIMITERS));
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_TOKENIZECONTENT_TO_TOKENS)
                .thenDo(ctx -> {
                    assertEquals(13, ctx.resultAsNodes().size());
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_INDEX_TOKEN_TO_TYPEINDEX)
                .thenDo(ctx -> {
                    assertEquals(13, ctx.resultAsNodes().size());
                    for (int j = 0; j < ctx.resultAsNodes().size(); j++) {
                        assertEquals(TYPE_TOKEN_WITHOUT_TYPE, ctx.resultAsNodes().get(j).get(NODE_NAME_TYPEINDEX));
                    }
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX)
                .thenDo(
                        ctx -> {
                            assertEquals(13, ctx.resultAsNodes().size());
                            long id = ((Node) ctx.variable("tokenizedContent").get(0)).id();
                            for (int j = 0; j < ctx.resultAsNodes().size(); j++) {
                                IntArray array = (IntArray) ctx.resultAsNodes().get(j).get(INVERTEDINDEX_POSITION);
                                int[] position = array.extract();
                                Arrays.sort(position);
                                assertNotEquals(-1, Arrays.binarySearch(position, j));
                                assertEquals(id, ctx.resultAsNodes().get(j).get(INVERTEDINDEX_TOKENIZEDCONTENT));
                            }
                            i[0]++;
                            ctx.continueTask();
                        }
                )
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void TwoRelationTwoText() {
        int counter = 4;
        final int[] i = {0};
        String text2 = "an orange was riding a skateboard";
        newTask()
                .travelInTime("0")
                .inject(text1)
                .defineAsVar("text")
                .inject(text2)
                .addToVar("text")
                .then(retrieveVocabularyNode())
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .readGlobalIndex("roots")
                .defineAsVar("nodevar")
                .then(updateOrCreateTokenizeRelationFromVar("tokenizer", "nodevar", "text", "text1", "text2"))
                .traverse(RELATION_INDEX_NODE_TO_TOKENIZECONTENT, NODE_NAME, "text2")
                .defineAsVar("tokenizedContent")
                .thenDo(ctx -> {
                    assertEquals(1, ctx.resultAsNodes().size());
                    Node node = ctx.resultAsNodes().get(0);
                    assertEquals(TYPE_TOKEN_WITHOUT_TYPE, node.get(TOKENIZE_CONTENT_TYPE));
                    assertTrue((boolean) node.get(TOKENIZE_CONTENT_DELIMITERS));
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_TOKENIZECONTENT_TO_TOKENS)
                .thenDo(ctx -> {
                    assertEquals(11, ctx.resultAsNodes().size());
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_INDEX_TOKEN_TO_TYPEINDEX)
                .thenDo(ctx -> {
                    assertEquals(11, ctx.resultAsNodes().size());
                    for (int j = 0; j < ctx.resultAsNodes().size(); j++) {
                        assertEquals(TYPE_TOKEN_WITHOUT_TYPE, ctx.resultAsNodes().get(j).get(NODE_NAME_TYPEINDEX));
                    }
                    i[0]++;
                    ctx.setVariable("id", ((Node) ctx.variable("tokenizedContent").get(0)).id());
                    ctx.continueTask();
                })
                .traverse(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX, INVERTEDINDEX_TOKENIZEDCONTENT, "{{id}}")
                .thenDo(
                        ctx -> {
                            assertEquals(11, ctx.resultAsNodes().size());
                            long id = ((Node) ctx.variable("tokenizedContent").get(0)).id();
                            for (int j = 0; j < ctx.resultAsNodes().size(); j++) {
                                IntArray array = (IntArray) ctx.resultAsNodes().get(j).get(INVERTEDINDEX_POSITION);
                                int[] position = array.extract();
                                Arrays.sort(position);
                                assertNotEquals(-1, Arrays.binarySearch(position, j));
                                assertEquals(id, ctx.resultAsNodes().get(j).get(INVERTEDINDEX_TOKENIZEDCONTENT));
                            }
                            i[0]++;
                            ctx.continueTask();
                        }
                )
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void oneRelationUpdated() {
        int counter = 4;
        final int[] i = {0};
        newTask()
                .travelInTime("0")
                .inject(text1)
                .defineAsVar("text1")
                .inject(text11)
                .defineAsVar("text11")
                .then(retrieveVocabularyNode())
                .then(createTokenizer("tokenizer", TokenizerType.ENGLISH, true))
                .readGlobalIndex("roots")
                .defineAsVar("nodevar")
                .then(updateOrCreateTokenizeRelationFromVar("tokenizer", "nodevar", "text1", "text1"))
                .travelInTime("1")
                .then(updateOrCreateTokenizeRelationFromVar("tokenizer", "nodevar", "text11", "text1"))
                .traverse(RELATION_INDEX_NODE_TO_TOKENIZECONTENT)
                .defineAsVar("tokenizedContent")
                .thenDo(ctx -> {
                    assertEquals(1, ctx.resultAsNodes().size());
                    Node node = ctx.resultAsNodes().get(0);
                    assertEquals(TYPE_TOKEN_WITHOUT_TYPE, node.get(TOKENIZE_CONTENT_TYPE));
                    assertTrue((boolean) node.get(TOKENIZE_CONTENT_DELIMITERS));
                    assertNotEquals(0, ((LongLongMap) node.get(TOKENIZE_CONTENT_PATCH)).size());
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_TOKENIZECONTENT_TO_TOKENS)
                .thenDo(ctx -> {
                    assertEquals(17, ctx.resultAsNodes().size());
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_INDEX_TOKEN_TO_TYPEINDEX)
                .thenDo(ctx -> {
                    assertEquals(17, ctx.resultAsNodes().size());
                    for (int j = 0; j < ctx.resultAsNodes().size(); j++) {
                        assertEquals(TYPE_TOKEN_WITHOUT_TYPE, ctx.resultAsNodes().get(j).get(NODE_NAME_TYPEINDEX));
                    }
                    i[0]++;
                    ctx.continueTask();
                })
                .traverse(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX)
                .thenDo(
                        ctx -> {
                            assertEquals(17, ctx.resultAsNodes().size());
                            long id = ((Node) ctx.variable("tokenizedContent").get(0)).id();
                            for (int j = 0; j < ctx.resultAsNodes().size(); j++) {
                                IntArray array = (IntArray) ctx.resultAsNodes().get(j).get(INVERTEDINDEX_POSITION);
                                int[] position = array.extract();
                                Arrays.sort(position);
                                assertNotEquals(-1, Arrays.binarySearch(position, j));
                                assertEquals(id, ctx.resultAsNodes().get(j).get(INVERTEDINDEX_TOKENIZEDCONTENT));
                            }
                            i[0]++;
                            ctx.continueTask();
                        }
                )
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

}