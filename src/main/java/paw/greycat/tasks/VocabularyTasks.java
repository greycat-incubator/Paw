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
package paw.greycat.tasks;

import greycat.Task;
import greycat.Type;

import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.*;
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
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_VOCABULARY)
                                .timeSensitivity("-1", "0")
                                .addToGlobalIndex(RELATION_INDEX_ENTRY_POINT, NODE_TYPE)
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
                ));
    }

    /**
     * Task to retrieve or create if not existing the nodes in the graph corresponding to tokens from an array of String
     *
     * @param tokens Array of string to retrieve or create
     * @return Task with all corresponding nodes in the current result
     */
    public static Task getOrCreateTokensFromStrings(String... tokens) {
        return newTask()
                .then(injectAsVar("myTokens", tokens))
                .pipe(getOrCreateTokensFromVar("myTokens"));
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
                .defineAsVar(TOKEN_VAR)
                .thenDo(ctx -> {
                    String token = ctx.resultAsStrings().get(0);
                    String sub = (token.length() > SIZE_OF_INDEX) ? token.substring(0, SIZE_OF_INDEX) : token;
                    ctx.setVariable(INDEXING_LETTER_VAR, sub);
                    ctx.continueTask();
                })
                .pipe(retrieveVocabularyNode())
                .defineAsVar(VOCABULARY_VAR)
                .traverse(RELATION_INDEX_VOCABULARY_TO_TOKENINDEX, NODE_NAME_TOKENINDEX, "{{" + INDEXING_LETTER_VAR + "}}")
                .then(ifEmptyThen(
                        createIndexing()
                ))
                .defineAsVar(NEW_TOKEN_INDEX_VAR)
                .traverse(RELATION_INDEX_TOKENINDEX_TO_TOKEN, NODE_NAME, "{{" + TOKEN_VAR + "}}")
                .then(
                        ifEmptyThen(
                                createToken()
                        )
                );
    }

    /**
     * Task to create a node in the graph that correspond to a token (String) present in the result
     *
     * @return Task with the corresponding node in the result
     */
    private static Task createToken() {
        String NEW_TOKEN_VAR = "newToken";
        return newTask()

                .then(executeAtWorldAndTime(
                        "0",
                        "" + BEGINNING_OF_TIME,
                        newTask()
                                //Token
                                .createNode()
                                .timeSensitivity("-1", "0")
                                .setAttribute(NODE_NAME, Type.STRING, "{{" + TOKEN_VAR + "}}")
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TOKEN)
                                .addVarToRelation(RELATION_TOKEN_TO_TOKENINDEX, NEW_TOKEN_INDEX_VAR)
                                .defineAsVar(NEW_TOKEN_VAR)
                                .then(readUpdatedTimeVar(NEW_TOKEN_INDEX_VAR))
                                .addVarToRelation(RELATION_INDEX_TOKENINDEX_TO_TOKEN, NEW_TOKEN_VAR, NODE_NAME)
                                .readVar(NEW_TOKEN_VAR)
                ))
                ;
    }

    /**
     * Task to create an indexing node in the graph
     *
     * @return Task with the corresponding node in the result
     */
    private static Task createIndexing() {
        return newTask()
                .then(executeAtWorldAndTime(
                        "0",
                        "" + BEGINNING_OF_TIME,
                        newTask()
                                //Token
                                .createNode()
                                .timeSensitivity("-1", "0")
                                .setAttribute(NODE_NAME_TOKENINDEX, Type.STRING, "{{" + INDEXING_LETTER_VAR + "}}")
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TOKENINDEX)
                                .defineAsVar(NEW_TOKEN_INDEX_VAR)
                                .then(readUpdatedTimeVar(VOCABULARY_VAR))
                                .addVarToRelation(RELATION_INDEX_VOCABULARY_TO_TOKENINDEX, NEW_TOKEN_INDEX_VAR, NODE_NAME_TOKENINDEX)
                                .readVar(NEW_TOKEN_INDEX_VAR)
                ));
    }

    private static String VOCABULARY_VAR = "vocabulary";
    private static String INDEXING_LETTER_VAR = "firstLetter";
    private static String NEW_TOKEN_INDEX_VAR = "newTokenIndexVar";
    private static String TOKEN_VAR = "token";
}
