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

import greycat.Node;
import greycat.Task;
import greycat.TaskContext;
import greycat.Type;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.IntArray;
import greycat.struct.LongLongMap;
import greycat.struct.Relation;
import paw.tokeniser.Tokenizer;
import paw.utils.MinimumEditDistance;

import java.util.Arrays;
import java.util.List;

import static greycat.Constants.BEGINNING_OF_TIME;
import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.*;
import static paw.PawConstants.*;
import static paw.greycat.tasks.TokenizationTasks.tokenizeFromVar;
import static paw.greycat.tasks.VocabularyTasks.retrieveToken;

public class TokenizedRelationTasks {

    /**
     * @param tokenizerVar
     * @param nodeVar
     * @param content
     * @param relationName
     * @return
     */
    public static Task updateOrCreateTokenizeRelationFromString(String tokenizerVar, String nodeVar, String content, String relationName) {
        return newTask()
                .inject(content)
                .defineAsVar("contentVar")
                .pipe(updateOrCreateTokenizeRelationFromVar(tokenizerVar, nodeVar, "contentVar", relationName));

    }

    /**
     * @param tokenizerVar
     * @param nodeVar
     * @param contentVar
     * @param relationName
     * @return
     */
    public static Task updateOrCreateTokenizeRelationFromVar(String tokenizerVar, String nodeVar, String contentVar, String... relationName) {
        return newTask()
                .readVar(contentVar)
                .ifThenElse(ctx -> ctx.result().size() == relationName.length,
                        newTask()
                                .pipeTo(tokenizeFromVar(tokenizerVar, contentVar), "tokenizedContentsVar")
                                .then(readUpdatedTimeVar(nodeVar))
                                .forEach(
                                        updateOrCreateTokenizeRelationOfNode(tokenizerVar, "tokenizedContentsVar", relationName)
                                )
                        ,
                        newTask()
                                .thenDo(ctx ->
                                        ctx.continueTask())
                                .thenDo(ctx -> ctx.endTask(ctx.result(), new IllegalArgumentException("number of content to Tokenize and relation Name are not similar")))
                );
    }

    /**
     * @param tokenizerVar
     * @param tokenizedContents
     * @param relationName
     * @return
     */
    private static Task updateOrCreateTokenizeRelationOfNode(String tokenizerVar, String tokenizedContents, String[] relationName) {
        return newTask()
                .defineAsVar("nodeVar")
                .readVar(tokenizedContents)
                .forEach(
                        newTask()
                                .map(retrieveToken())
                                .flat()
                                .defineAsVar("tokenizedContentVar")
                                .thenDo(
                                        ctx -> {
                                            int increment = ctx.intVar("i");
                                            String relation = relationName[increment];
                                            ctx.defineVariable("relationVar", relation);
                                            ctx.continueTask();
                                        })
                                .pipe(uocTokenizeRelation(tokenizerVar))
                );

    }

    /**
     * @param tokenizerVar
     * @return
     */
    private static Task uocTokenizeRelation(String tokenizerVar) {
        return newTask()
                .readVar("nodeVar")
                .traverse(RELATION_INDEX_NODE_TO_TOKENIZECONTENT, NODE_NAME, "{{relationVar}}")
                .then(ifEmptyThenElse(
                        createTokenRelation(tokenizerVar),
                        updateTokenRelation(tokenizerVar)
                ));
    }

    /**
     * @param tokenizerVar
     * @return
     */
    private static Task updateTokenRelation(String tokenizerVar) {
        return newTask()
                .defineAsVar("relationNode")
                .then(checkForFuture()) //TODO children world
                .thenDo(ctx -> {
                    long dephasing = ctx.resultAsNodes().get(0).timeDephasing();
                    if (dephasing == 0L)
                        ctx.endTask(ctx.result(), new RuntimeException("Trying to modify a tokenize content at the time of the previous modification"));
                    else
                        ctx.continueTask();
                })
                .thenDo(
                        ctx -> {
                            Node node = ctx.resultAsNodes().get(0);
                            long relationNodeId = node.id();
                            String type = (String) node.get(TOKENIZE_CONTENT_TYPE);
                            Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                            if (!type.equals(tokenizer.getTypeOfToken()) || ((boolean) node.get(TOKENIZE_CONTENT_DELIMITERS) != tokenizer.isKeepingDelimiterActivate())) {
                                ctx.endTask(ctx.result(), new RuntimeException("Different Tokenizer use for the update"));
                            }
                            //node.rephase() TODO Check if not necessary
                            node.remove(TOKENIZE_CONTENT_PATCH);
                            LongLongMap mapPatch = (LongLongMap) node.getOrCreate(TOKENIZE_CONTENT_PATCH, Type.LONG_TO_LONG_MAP);
                            Relation relation = (Relation) node.get(RELATION_TOKENIZECONTENT_TO_TOKENS);
                            long[] relationsId = relation.all();
                            Object[] newContent = ctx.variable("tokenizedContentVar").asArray();
                            long[] newContentId = Arrays.stream(newContent).mapToLong(obj -> ((Node) obj).id()).toArray();
                            MinimumEditDistance med = new MinimumEditDistance(relationsId, newContentId);
                            List<long[]> path = med.path();
                            ctx.setVariable("formerIndex", 0);
                            ctx.setVariable("newIndex", 0);
                            ctx.setVariable("relation", relation);
                            ctx.setVariable("relationId", relationNodeId);
                            ctx.setVariable("type", type);
                            ctx.setVariable("mapPatch", mapPatch);
                            ctx.continueWith(ctx.wrap(path));
                        })
                .map(
                        newTask()
                                .thenDo((TaskContext ctx) -> {
                                            long[] action = new long[]{(long) ctx.result().get(0), (long) ctx.result().get(1)};
                                            LongLongMap mapPatch = (LongLongMap) ctx.variable("mapPatch").get(0);
                                            int index = (int) ctx.variable("i").get(0);
                                            Relation relation = (Relation) ctx.variable("relation").get(0);
                                            int newIndex = (int) ctx.variable("newIndex").get(0);
                                            int formerIndex = (int) ctx.variable("formerIndex").get(0);
                                            long relationNodeId = (long) ctx.variable("relationId").get(0);
                                            String type = (String) ctx.variable("type").get(0);
                                            if (action[1] == MinimumEditDistance.DELETION) {
                                                relation.delete(newIndex);
                                                mapPatch.put(-index, action[0]);
                                                newTask()
                                                        .lookup("" + action[0])
                                                        .traverse(RELATION_INDEX_TOKEN_TO_TYPEINDEX, NODE_NAME_TYPEINDEX, type)
                                                        .traverse(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX, INVERTEDINDEX_TOKENIZEDCONTENT, "" + relationNodeId)
                                                        .thenDo(
                                                                tctx -> {
                                                                    Node node = tctx.resultAsNodes().get(0);
                                                                    IntArray position = (IntArray) node.getOrCreate("position", Type.INT_ARRAY);
                                                                    position.removeElement(formerIndex);
                                                                    node.free(); //Added to check
                                                                    tctx.continueTask();
                                                                })
                                                        .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, result -> {
                                                            ctx.setVariable("formerIndex", formerIndex + 1);
                                                            ctx.continueTask();
                                                        });

                                            } else if (action[1] == MinimumEditDistance.INSERTION) {
                                                relation.insert(newIndex, action[0]);
                                                mapPatch.put(index, action[0]);
                                                newTask()
                                                        .lookup("" + action[0])
                                                        .defineAsVar("token")
                                                        .traverse(RELATION_INDEX_TOKEN_TO_TYPEINDEX, NODE_NAME_TYPEINDEX, type)
                                                        .then(ifEmptyThen(
                                                                newTask()
                                                                        .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                                                                                newTask()
                                                                                        .createNode()
                                                                                        .setAttribute(NODE_NAME_TYPEINDEX, Type.STRING, type)
                                                                                        .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TYPEINDEX)
                                                                                        .addVarToRelation(RELATION_TYPEINDEX_TO_TOKEN, "token")
                                                                                        .defineAsVar("typeIndex")
                                                                                        .readVar("token")
                                                                                        .addVarToRelation(RELATION_INDEX_TOKEN_TO_TYPEINDEX, "typeIndex", NODE_NAME_TYPEINDEX)
                                                                                        .readVar("typeIndex")
                                                                        ))))
                                                        .defineAsVar("typeIndex")
                                                        .traverse(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX, INVERTEDINDEX_TOKENIZEDCONTENT, "" + relationNodeId)
                                                        .then(
                                                                ifEmptyThen(
                                                                        newTask()
                                                                                .then(
                                                                                        executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                                                                                                newTask()
                                                                                                        .createNode()
                                                                                                        .setAttribute(INVERTEDINDEX_TOKENIZEDCONTENT, Type.LONG, "{{relationId}}")
                                                                                                        .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_INVERTED_INDEX)
                                                                                                        .thenDo(ctx1 ->
                                                                                                        {
                                                                                                            Node node = ctx1.resultAsNodes().get(0);
                                                                                                            node.getOrCreate(INVERTEDINDEX_POSITION, Type.INT_ARRAY);
                                                                                                            ctx1.continueTask();
                                                                                                        })
                                                                                                        .defineAsVar("invertedIndex")
                                                                                                        .addVarToRelation(RELATION_INVERTEDINDEX_TO_TOKEN, "token")
                                                                                                        .addVarToRelation(RELATION_INVERTEDINDEX_TO_TYPEINDEX, "typeIndex")
                                                                                                        .readVar("typeIndex")
                                                                                                        .addVarToRelation(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX, "invertedIndex", INVERTEDINDEX_TOKENIZEDCONTENT)
                                                                                                        .readVar("invertedIndex")
                                                                                        )
                                                                                )
                                                                )
                                                        )
                                                        .thenDo(ctxa -> {
                                                            Node node = ctxa.resultAsNodes().get(0);
                                                            IntArray position = (IntArray) node.getOrCreate("position", Type.INT_ARRAY);
                                                            position.addElement(newIndex);
                                                            node.free();
                                                            ctxa.continueTask();
                                                        })
                                                        .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, result -> {
                                                            ctx.setVariable("newIndex", newIndex + 1);
                                                            ctx.continueTask();
                                                        });

                                            } else if (action[1] == MinimumEditDistance.KEEP) {
                                                newTask().lookup("" + action[0])
                                                        .traverse(RELATION_INDEX_TOKEN_TO_TYPEINDEX, NODE_NAME_TYPEINDEX, type)
                                                        .traverse(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX, INVERTEDINDEX_TOKENIZEDCONTENT, "" + relationNodeId)
                                                        .thenDo(ctxa -> {
                                                            Node node = ctxa.resultAsNodes().get(0);
                                                            IntArray position = (IntArray) node.getOrCreate("position", Type.INT_ARRAY);
                                                            position.removeElement(formerIndex);
                                                            position.addElement(newIndex);
                                                            node.free();
                                                            ctxa.continueTask();
                                                        }).executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD, result -> {
                                                    ctx.setVariable("formerIndex", formerIndex + 1);
                                                    ctx.setVariable("newIndex", newIndex + 1);
                                                    ctx.continueTask();
                                                });
                                            }
                                        }
                                )
                );

    }

    /**
     * @param tokenizerVar
     * @return
     */
    private static Task createTokenRelation(String tokenizerVar) {
        return newTask()
                .createNode()
                .defineAsVar("relationNode")
                .thenDo(ctx -> {
                    Node node = ctx.resultAsNodes().get(0);
                    ctx.setVariable("relationNodeId", node.id());
                    node.getOrCreate(TOKENIZE_CONTENT_PATCH, Type.LONG_TO_LONG_MAP);
                    ctx.continueTask();
                })
                .addVarToRelation(RELATION_TOKENIZECONTENT_TO_NODE, "nodeVar")
                .setAttribute(NODE_NAME, Type.STRING, "{{relationVar}}")
                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TOKENIZE_CONTENT)

                .readVar("nodeVar")
                .addVarToRelation(RELATION_INDEX_NODE_TO_TOKENIZECONTENT, "relationNode", NODE_NAME)

                .readVar(tokenizerVar)
                .thenDo(
                        ctx -> {
                            Tokenizer tokenizer = (Tokenizer) ctx.result().get(0);
                            ctx.setVariable("type", tokenizer.getTypeOfToken());
                            ctx.setVariable("delimiters", tokenizer.isKeepingDelimiterActivate());
                            ctx.setVariable("tokenizerType", tokenizer.getType());
                            ctx.continueTask();
                        })

                .readVar("tokenizedContentVar")
                .forEach(
                        newTask()
                                .defineAsVar("token")
                                .traverse(RELATION_INDEX_TOKEN_TO_TYPEINDEX, NODE_NAME_TYPEINDEX, "{{type}}")
                                .then(ifEmptyThen(
                                        newTask()
                                                .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                                                        newTask()
                                                                .createNode()
                                                                .setAttribute(NODE_NAME_TYPEINDEX, Type.STRING, "{{type}}")
                                                                .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_TYPEINDEX)
                                                                .addVarToRelation(RELATION_TYPEINDEX_TO_TOKEN, "token")
                                                                .defineAsVar("typeIndex")
                                                                .readVar("token")
                                                                .addVarToRelation(RELATION_INDEX_TOKEN_TO_TYPEINDEX, "typeIndex", NODE_NAME_TYPEINDEX)
                                                                .readVar("typeIndex")
                                                ))))
                                .defineAsVar("typeIndex")

                                .traverse(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX, INVERTEDINDEX_TOKENIZEDCONTENT, "{{relationNodeId}}")
                                .then(
                                        ifEmptyThenElse(
                                                newTask()
                                                        .then(executeAtWorldAndTime("0", "" + BEGINNING_OF_TIME,
                                                                newTask()
                                                                        .createNode()
                                                                        .setAttribute(INVERTEDINDEX_TOKENIZEDCONTENT, Type.LONG, "{{relationNodeId}}")
                                                                        .setAttribute(NODE_TYPE, Type.STRING, NODE_TYPE_INVERTED_INDEX)
                                                                        .thenDo(ctx ->
                                                                        {
                                                                            Node node = ctx.resultAsNodes().get(0);
                                                                            node.getOrCreate(INVERTEDINDEX_POSITION, Type.INT_ARRAY);
                                                                            ctx.continueTask();
                                                                        })
                                                                        .defineAsVar("invertedIndex")
                                                                        .addVarToRelation(RELATION_INVERTEDINDEX_TO_TOKEN, "token")
                                                                        .addVarToRelation(RELATION_INVERTEDINDEX_TO_TYPEINDEX, "typeIndex")
                                                                        .readVar("typeIndex")
                                                                        .addVarToRelation(RELATION_INDEX_TYPEINDEX_TO_INVERTEDINDEX, "invertedIndex", INVERTEDINDEX_TOKENIZEDCONTENT)
                                                                        .readVar("invertedIndex")
                                                                )
                                                        )
                                                ,
                                                newTask().then(checkForFuture())
                                        )
                                )
                                .thenDo(
                                        ctx -> {
                                            Node node = ctx.resultAsNodes().get(0);
                                            IntArray position = (IntArray) node.getOrCreate(INVERTEDINDEX_POSITION, Type.INT_ARRAY);
                                            position.addElement(ctx.intVar("i"));
                                            node.free();
                                            ctx.continueTask();
                                        })

                                .readVar("relationNode")
                                .addVarToRelation(RELATION_TOKENIZECONTENT_TO_TOKENS, "token")

                )

                .readVar("relationNode")
                .setAttribute(TOKENIZE_CONTENT_TYPE, Type.STRING, "{{type}}")
                .setAttribute(TOKENIZE_CONTENT_DELIMITERS, Type.BOOL, "{{delimiters}}");
    }


}
