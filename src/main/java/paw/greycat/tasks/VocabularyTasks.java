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
package paw.greycat.tasks;

import greycat.*;
import greycat.struct.EGraph;
import greycat.struct.IntIntMap;
import greycat.utility.HashHelper;
import paw.greycat.indexing.WordIndex;
import paw.greycat.indexing.radix.RadixTree;

import java.util.Arrays;

import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.executeAtWorldAndTime;
import static mylittleplugin.MyLittleActions.ifEmptyThen;
import static paw.PawConstants.*;

public class VocabularyTasks {

    /**
     * Task initializing the vocabulary node, the time remain unchanged
     *
     * @return a Task with the created Vocabulary Main node in the current result
     */
    private static Task initializeVocabulary() {
        return newTask()
                .then(executeAtWorldAndTime("0", String.valueOf(BEGINNING_OF_TIME),
                        newTask()
                                .createNode()
                                .setAttribute(NODE_TYPE, Type.INT, NODE_TYPE_VOCABULARY)
                                .timeSensitivity("-1", "0")
                                .addToGlobalIndex(RELATION_INDEX_ENTRY_POINT, NODE_TYPE)
                                .setAsVar("voca")
                                .createNode()
                                .thenDo(ctx -> {
                                    Node node = ctx.resultAsNodes().get(0);
                                    node.setTimeSensitivity(-1, 0);
                                    node.getOrCreate(VOCABULARY_MAP, Type.INT_TO_INT_MAP);
                                    ctx.continueTask();
                                })
                                .setAsVar("map")
                                .readVar("voca")
                                .addVarToRelation("map", "map")
                                .createNode()
                                .thenDo(ctx -> {
                                    Node node = ctx.resultAsNodes().get(0);
                                    node.setTimeSensitivity(-1, 0);
                                    node.getOrCreate(VOCABULARY, Type.EGRAPH);
                                    ctx.continueTask();
                                })
                                .setAsVar("egraph")
                                .readVar("voca")
                                .addVarToRelation("egraph", "egraph")
                ));
    }

    /**
     * Task to retrieve the vocabulary main node, if it doesn't exist then one is created.
     *
     * @return Task with the Vocabulary Main node in the current result
     */
    public static Task retrieveVocabularyNode() {
        return newTask()
                .readGlobalIndex(RELATION_INDEX_ENTRY_POINT, NODE_TYPE, NODE_TYPE_VOCABULARY)
                .then(ifEmptyThen(
                        initializeVocabulary()
                ))
                .setAsVar(VOCABULARY_VAR)
                .traverse("map")
                .defineAsGlobalVar(VOCABULARY_MAP)
                .readVar(VOCABULARY_VAR)
                .traverse("egraph")
                .defineAsGlobalVar(VOCABULARY);

    }

    /**
     * Task to retrieve or create if not existing the nodes in the graph corresponding to tokens from an array of String
     *
     * @param tokens Array of string to retrieve or create
     * @return Task with all corresponding nodes in the current result
     */
    public static Task getOrCreateTokensFromStrings(String... tokens) {
        return newTask()
                .ifThen(ctx -> ctx.variable(VOCABULARY_MAP) == null,
                        retrieveVocabularyNode())
                .thenDo(ctx -> {
                    int[] enodesId = retriveNodes(ctx, tokens);
                    ctx.continueWith(ctx.wrap(enodesId));
                });

    }

    /**
     * Task to retrieve or create if not existing the nodes in the graph that correspond to tokens (String) present in the result
     *
     * @return Task with all corresponding nodes in the current result
     */
    public static Task getOrCreateTokensFromResult() {
        return newTask()
                .thenDo(ctx -> {
                    TaskResult<String> tokens = ctx.resultAsStrings();
                    String[] tokensA = Arrays.stream(tokens.asArray()).map(obj -> (String) (obj)).toArray(String[]::new);
                    int[] enodesId = retriveNodes(ctx, tokensA);
                    ctx.continueWith(ctx.wrap(enodesId));
                });
    }

    static int[] retriveNodes(TaskContext ctx, String[] tokens) {
        Node node = (Node) ctx.variable(VOCABULARY_MAP).get(0);
        IntIntMap intIntMapMap = node.getIntIntMap(VOCABULARY_MAP);
        int[] eNodesId = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            int hash = HashHelper.hash(tokens[i]);
            int enode = intIntMapMap.get(hash);
            if (enode != Constants.NULL_INT) {
                eNodesId[i] = enode;
            } else {
                Node voc = (Node) ctx.variable(VOCABULARY).get(0);
                EGraph eGraph = (EGraph) voc.get(VOCABULARY);
                WordIndex wordIndex = new RadixTree(eGraph);
                int nodeId = wordIndex.getOrCreate(tokens[i]);
                intIntMapMap.put(hash, nodeId);
                eNodesId[i] = nodeId;
            }
        }
        return eNodesId;
    }

    public static String VOCABULARY_VAR = "vocabulary2";
    public static String VOCABULARY_MAP = "vocabularyMap";
}
