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

import greycat.Node;
import greycat.Task;
import greycat.Type;
import greycat.struct.EGraph;
import paw.greycat.indexing.WordIndex;
import paw.greycat.indexing.radix.RadixTree;

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
                .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                        newTask()
                                .createNode()
                                .setAttribute(NODE_TYPE, Type.INT, NODE_TYPE_VOCABULARY)
                                .timeSensitivity("-1", "0")
                                .addToGlobalIndex(RELATION_INDEX_ENTRY_POINT, NODE_TYPE)
                                .thenDo(ctx -> {
                                    Node node = ctx.resultAsNodes().get(0);
                                    node.getOrCreate(VOCABULARY, Type.EGRAPH);
                                    ctx.continueTask();
                                })
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
                .defineAsGlobalVar(VOCABULARY_VAR);
    }

    /**
     * Task to retrieve or create if not existing the nodes in the graph corresponding to tokens from an array of String
     *
     * @param tokens Array of string to retrieve or create
     * @return Task with all corresponding nodes in the current result
     */
    public static Task getOrCreateTokensFromStrings(String... tokens) {
        return newTask()
                .inject(tokens)
                .map(retrieveToken())
                .flat();
    }

    /**
     * Task to retrieve or create if not existing the nodes in the graph that correspond to tokens (String) stored in a variable
     *
     * @param variable in which the tokens are stored
     * @return Task with all corresponding nodes in the current result
     */
    public static Task getOrCreateTokensFromVar(String variable) {
        return newTask()
                .readVar(variable)
                .map(retrieveToken())
                .flat();
    }

    /**
     * Task to retrieve or create if not existing the nodes in the graph that correspond to tokens (String) present in the result
     *
     * @return Task with all corresponding nodes in the current result
     */
    static Task retrieveToken() {
        return newTask()
                .defineAsVar("token")
                .ifThen(ctx -> ctx.variable(VOCABULARY_VAR) == null,
                        retrieveVocabularyNode())
                .thenDo(ctx -> {
                    String token = (String) ctx.variable("token").get(0);
                    Node node = (Node) ctx.variable(VOCABULARY_VAR).get(0);
                    EGraph eGraph = (EGraph) node.get(VOCABULARY);
                    WordIndex wordIndex = new RadixTree(eGraph);
                    int nodeId = wordIndex.getOrCreate(token);
                    ctx.continueWith(ctx.wrap(nodeId));
                });
    }


    public static String VOCABULARY_VAR = "vocabulary2";

}
