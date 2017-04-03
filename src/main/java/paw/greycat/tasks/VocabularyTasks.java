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
                .defineAsVar("token")
                .thenDo(ctx -> {
                    String token = ctx.resultAsStrings().get(0);
                    ctx.setVariable("firstLetter", token.substring(0, SIZE_OF_INDEX));
                    ctx.continueTask();
                })
                .pipe(retrieveVocabularyNode())
                .traverse(RELATION_INDEX_VOCABULARY_TO_TOKENINDEX, NODE_NAME_TOKENINDEX, "{{firstLetter}}")
                .then(ifEmptyThen(
                        createIndexing()
                ))
                .defineAsVar("indexing")
                .traverse(RELATION_INDEX_TOKENINDEX_TO_TOKEN, NODE_NAME, "{{token}}")
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
        return newTask()
                .then(executeAtWorldAndTime(
                        "0",
                        "" + BEGINNING_OF_TIME,
                        newTask()
                                //Token
                                .createNode()
                                .timeSensitivity("-1", "0")
                                .setAttribute(NODE_NAME, Type.STRING, "{{token}}")
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TOKEN)
                                .addVarToRelation(RELATION_TOKEN_TO_TOKENINDEX, "indexing")
                                .defineAsVar("newToken")
                                .readVar("indexing")
                                .addVarToRelation(RELATION_INDEX_TOKENINDEX_TO_TOKEN, "newToken", NODE_NAME)
                                .readVar("newToken")
                ));
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
                                .setAttribute(NODE_NAME_TOKENINDEX, Type.STRING, "{{firstLetter}}")
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TOKENINDEX)
                                .defineAsVar("newTokenIndex")
                                .readVar("Vocabulary")
                                .addVarToRelation(RELATION_INDEX_VOCABULARY_TO_TOKENINDEX, "newTokenIndex", NODE_NAME_TOKENINDEX)
                                .readVar("newTokenIndex")
                ));
    }
}
