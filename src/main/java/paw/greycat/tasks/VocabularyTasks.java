package paw.greycat.tasks;

import greycat.Task;
import greycat.Type;

import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.*;
import static paw.PawConstants.*;

public class VocabularyTasks {

    public static Task initializeVocabulary() {
        return newTask()
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
                .then(ifEmptyThen(
                        newTask().then(executeAtWorldAndTime("0", ""+BEGINNING_OF_TIME,
                                newTask()
                                        .createNode()
                                        .setAttribute(ENTRY_POINT_NODE_NAME, Type.STRING, VOCABULARY_NODE_NAME)
                                        .timeSensitivity("-1", "0")
                                        .addToGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME)
                        ))
                ));
    }


    public static Task retrieveVocabularyNode() {
        return newTask()
                .readGlobalIndex(ENTRY_POINT_INDEX, ENTRY_POINT_NODE_NAME, VOCABULARY_NODE_NAME)
                .then(ifEmptyThen(
                        initializeVocabulary()
                ));
    }

    public static Task getOrCreateTokensFromStrings(String... tokens) {
        return newTask()
                .pipe(retrieveVocabularyNode())
                .defineAsVar("Vocabulary")
                .then(injectAsVar("myTokens", tokens))
                .pipe(getOrCreateTokensFromVar("myTokens"));

    }

    public static Task getOrCreateTokensFromVar(String variable) {
        return newTask()
                .readVar(variable)
                .map(retrieveToken())
                .flat();

    }

    private static Task retrieveToken() {
        return newTask()
                .defineAsVar("token")
                .thenDo(ctx -> {
                    String token = ctx.resultAsStrings().get(0);
                    ctx.setVariable("firstLetter", token.substring(0, SIZE_OF_INDEX));
                    ctx.continueTask();
                })
                .readVar("Vocabulary")
                .traverse(VOCABULARY_TOKENINDEX_INDEX, TOKENINDEX_NAME, "{{firstLetter}}")
                .then(ifEmptyThen(
                        createIndexing()
                ))
                .defineAsVar("indexing")
                .traverse(TOKENINDEX_TOKEN_INDEX, TOKEN_NAME, "{{token}}")
                .then(
                        ifEmptyThen(
                                createToken()
                        )
                );

    }

    private static Task createToken() {
        return newTask()
                .then(executeAtWorldAndTime(
                        "0",
                        "" + BEGINNING_OF_TIME,
                        newTask()
                                //Token
                                .createNode()
                                .timeSensitivity("-1", "0")
                                .setAttribute(TOKEN_NAME, Type.STRING, "{{token}}")
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TOKEN)
                                .addVarToRelation(TOKEN_TOKENINDEX_INDEX, "indexing")
                                .defineAsVar("newToken")
                                .readVar("indexing")
                                .addVarToRelation(TOKENINDEX_TOKEN_INDEX, "newToken", TOKEN_NAME)
                                .readVar("newToken")
                ));
    }

    private static Task createIndexing() {
        return newTask()
                .then(executeAtWorldAndTime(
                        "0",
                        "" + BEGINNING_OF_TIME,
                        newTask()
                                //Token
                                .createNode()
                                .timeSensitivity("-1", "0")
                                .setAttribute(TOKENINDEX_NAME, Type.STRING, "{{firstLetter}}")
                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TOKEN_INDEX)
                                .defineAsVar("newTokenIndex")
                                .readVar("Vocabulary")
                                .addVarToRelation(VOCABULARY_TOKENINDEX_INDEX, "newTokenIndex", TOKENINDEX_NAME)
                                .readVar("newTokenIndex")
                ));
    }
}
