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
package paw.greycat.actions.tokenizedcontent;

import greycat.DeferCounter;
import greycat.Node;
import greycat.struct.EGraph;
import greycat.struct.IntArray;
import greycat.struct.LongLongMap;
import greycat.struct.RelationIndexed;
import mylittleplugin.MyLittleActions;
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
import static paw.greycat.tasks.VocabularyTasks.VOCABULARY_VAR;

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
        int counter = 1;
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

                    int[] tokens = ((IntArray) node.get(TOKENIZE_CONTENT_TOKENS)).extract();
                    assertEquals(13, tokens.length);

                    Node vocab = (Node) ctx.variable(VOCABULARY_VAR).get(0);
                    EGraph eGraph = (EGraph) vocab.get(VOCABULARY);
                    DeferCounter deferCounter = ctx.graph().newCounter(13);
                    for (int j = 0; j < tokens.length; j++) {
                        RelationIndexed relationIndexed = (RelationIndexed) eGraph.node(tokens[j]).get(RELATION_INDEX_TOKEN_II);
                        int finalJ = j;
                        relationIndexed.find(result -> {
                            assertEquals(1, result.length);
                            IntArray array = (IntArray) result[0].get(INVERTEDINDEX_POSITION);
                            int[] position = array.extract();
                            Arrays.sort(position);
                            assertNotEquals(-1, Arrays.binarySearch(position, finalJ));
                            deferCounter.count();
                        }, ctx.world(), ctx.time(), INVERTEDINDEX_TOKENIZEDCONTENT, String.valueOf(node.id()));
                    }
                    deferCounter.then(() -> {
                        i[0]++;
                        ctx.continueTask();
                    });

                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void TwoRelationTwoText() {
        int counter = 1;
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

                    int[] tokens = ((IntArray) node.get(TOKENIZE_CONTENT_TOKENS)).extract();
                    assertEquals(11, tokens.length);

                    Node vocab = (Node) ctx.variable(VOCABULARY_VAR).get(0);
                    EGraph eGraph = (EGraph) vocab.get(VOCABULARY);
                    DeferCounter deferCounter = ctx.graph().newCounter(11);
                    for (int j = 0; j < tokens.length; j++) {
                        RelationIndexed relationIndexed = (RelationIndexed) eGraph.node(tokens[j]).get(RELATION_INDEX_TOKEN_II);
                        int finalJ = j;
                        relationIndexed.find(result -> {
                            assertEquals(1, result.length);
                            IntArray array = (IntArray) result[0].get(INVERTEDINDEX_POSITION);
                            int[] position = array.extract();
                            Arrays.sort(position);
                            assertNotEquals(-1, Arrays.binarySearch(position, finalJ));
                            deferCounter.count();
                        }, ctx.world(), ctx.time(), INVERTEDINDEX_TOKENIZEDCONTENT, String.valueOf(node.id()));
                    }
                    deferCounter.then(() -> {
                        i[0]++;
                        ctx.continueTask();
                    });
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

    @Test
    void oneRelationUpdated() {
        int counter = 1;
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
                .then(MyLittleActions.readUpdatedTimeVar("nodevar"))
                .traverse(RELATION_INDEX_NODE_TO_TOKENIZECONTENT)
                .defineAsVar("tokenizedContent")
                .thenDo(ctx -> {
                    assertEquals(1, ctx.resultAsNodes().size());
                    Node node = ctx.resultAsNodes().get(0);
                    assertEquals(TYPE_TOKEN_WITHOUT_TYPE, node.get(TOKENIZE_CONTENT_TYPE));
                    assertTrue((boolean) node.get(TOKENIZE_CONTENT_DELIMITERS));
                    assertNotEquals(0, ((LongLongMap) node.get(TOKENIZE_CONTENT_PATCH)).size());

                    int[] tokens = ((IntArray) node.get(TOKENIZE_CONTENT_TOKENS)).extract();
                    assertEquals(17, tokens.length);

                    Node vocab = (Node) ctx.variable(VOCABULARY_VAR).get(0);
                    EGraph eGraph = (EGraph) vocab.get(VOCABULARY);
                    DeferCounter deferCounter = ctx.graph().newCounter(17);
                    for (int j = 0; j < tokens.length; j++) {
                        RelationIndexed relationIndexed = (RelationIndexed) eGraph.node(tokens[j]).get(RELATION_INDEX_TOKEN_II);
                        int finalJ = j;
                        relationIndexed.find(result -> {
                            assertEquals(1, result.length);
                            IntArray array = (IntArray) result[0].get(INVERTEDINDEX_POSITION);
                            int[] position = array.extract();
                            Arrays.sort(position);
                            assertNotEquals(-1, Arrays.binarySearch(position, finalJ));
                            deferCounter.count();
                        }, ctx.world(), ctx.time(), INVERTEDINDEX_TOKENIZEDCONTENT, String.valueOf(node.id()));
                    }
                    deferCounter.then(() -> {
                        i[0]++;
                        ctx.continueTask();
                    });
                })
                .execute(graph, null);
        assertEquals(counter, i[0]);
    }

}