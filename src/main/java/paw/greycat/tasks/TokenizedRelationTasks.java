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
import greycat.struct.*;
import paw.tokeniser.Tokenizer;
import paw.utils.MinimumEditDistance;

import java.util.Arrays;
import java.util.List;

import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.checkForFuture;
import static mylittleplugin.MyLittleActions.ifEmptyThenElse;
import static paw.PawConstants.*;
import static paw.greycat.tasks.TokenizationTasks.tokenizeFromStrings;
import static paw.greycat.tasks.TokenizationTasks.tokenizeFromVar;
import static paw.greycat.tasks.VocabularyTasks.*;

@SuppressWarnings("Duplicates")
public class TokenizedRelationTasks {

    /**
     * @param tokenizerVar
     * @param nodesVar
     * @param content
     * @param relationName
     * @return
     */
    public static Task updateOrCreateTokenizeRelationFromString(String tokenizerVar, String nodesVar, String content, String relationName) {
        return newTask()
                .inject(relationName)
                .defineAsVar("relationVar")
                .pipeTo(tokenizeFromStrings(tokenizerVar, content), "tokenizedContentsVar")
                .ifThen(ctx -> ctx.variable(VOCABULARY_VAR) == null,
                        retrieveVocabularyNode())
                .readVar(nodesVar)
                .forEach(
                        newTask()
                                .defineAsVar("nodeVar")
                                .readVar("tokenizedContentsVar")
                                .forEach(
                                        newTask()
                                                .pipe(retrieveToken())
                                                .defineAsVar("tokenizedContentVar")
                                                .pipe(uocTokenizeRelation(tokenizerVar))
                                )
                );

    }

    /**
     * @param tokenizerVar
     * @param nodesVar
     * @param contentVar
     * @param relationName
     * @return
     */
    public static Task updateOrCreateTokenizeRelationFromVar(String tokenizerVar, String nodesVar, String contentVar, String... relationName) {
        return newTask()
                .readVar(contentVar)
                .ifThenElse(ctx -> ctx.result().size() == relationName.length,
                        newTask()
                                .pipeTo(tokenizeFromVar(tokenizerVar, contentVar), "tokenizedContentsVar")
                                .readVar(nodesVar)
                                .forEach(
                                        updateOrCreateTokenizeRelationOfNode(tokenizerVar, "tokenizedContentsVar", relationName)
                                )
                        ,
                        newTask()
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
                                .pipe(retrieveToken())
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
                .travelInTime("{{time}}")
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
                            node.remove(TOKENIZE_CONTENT_PATCH);
                            LongLongMap mapPatch = (LongLongMap) node.getOrCreate(TOKENIZE_CONTENT_PATCH, Type.LONG_TO_LONG_MAP);
                            IntArray relation = (IntArray) node.get(TOKENIZE_CONTENT_TOKENS);
                            int[] relationsId = relation.extract();

                            Object[] newContent = ctx.variable("tokenizedContentVar").asArray();
                            int[] newContentId = Arrays.stream(newContent).mapToInt(obj -> (int) obj).toArray();
                            MinimumEditDistance med = new MinimumEditDistance(relationsId, newContentId);
                            List<int[]> path = med.path();
                            ctx.setVariable("formerIndex", 0);
                            ctx.setVariable("newIndex", 0);
                            ctx.setVariable("relation", relation);
                            ctx.setVariable("relationNodeId", relationNodeId);
                            ctx.setVariable("type", type);
                            ctx.setVariable("mapPatch", mapPatch);
                            ctx.continueWith(ctx.wrap(path));
                        })
                .map(
                        newTask()
                                .thenDo((TaskContext ctx) -> {
                                            int[] action = new int[]{(int) ctx.result().get(0), (int) ctx.result().get(1)};
                                            LongLongMap mapPatch = (LongLongMap) ctx.variable("mapPatch").get(0);
                                            int index = (int) ctx.variable("i").get(0);
                                            IntArray relation = (IntArray) ctx.variable("relation").get(0);
                                            int newIndex = (int) ctx.variable("newIndex").get(0);
                                            int formerIndex = (int) ctx.variable("formerIndex").get(0);
                                            long relationNodeId = (long) ctx.variable("relationNodeId").get(0);
                                            String type = (String) ctx.variable("type").get(0);
                                            EGraph vocabulary = (EGraph) ((Node) ctx.variable(VOCABULARY_VAR).get(0)).get(VOCABULARY);

                                            if (action[1] == MinimumEditDistance.DELETION) {
                                                relation.removeElementbyIndex(newIndex);
                                                mapPatch.put(-index, action[0]);
                                                ENode enode = vocabulary.node(action[0]);
                                                RelationIndexed relationIndexed = (RelationIndexed) enode.get(RELATION_INDEX_TOKEN_II);
                                                relationIndexed.find(new Callback<Node[]>() {
                                                    @Override
                                                    public void on(Node[] result) {
                                                        IntArray position = (IntArray) result[0].get(INVERTEDINDEX_POSITION);
                                                        position.removeElement(formerIndex);
                                                        ctx.graph().freeNodes(result);
                                                        ctx.setVariable("formerIndex", formerIndex + 1);
                                                        ctx.continueTask();
                                                    }
                                                }, ctx.world(), ctx.time(), INVERTEDINDEX_TOKENIZEDCONTENT, String.valueOf(ctx.longVar("relationNodeId")));
                                            } else if (action[1] == MinimumEditDistance.INSERTION) {
                                                relation.insertElementAt(newIndex, action[0]);
                                                mapPatch.put(index, action[0]);
                                                ENode enode = vocabulary.node(action[0]);
                                                RelationIndexed relationIndexed = (RelationIndexed) enode.getOrCreate(RELATION_INDEX_TOKEN_II, Type.RELATION_INDEXED);
                                                long[] nodeExisting = relationIndexed.select(INVERTEDINDEX_TOKENIZEDCONTENT, String.valueOf(ctx.longVar("relationNodeId")));
                                                final Node[] ii = new Node[1];
                                                DeferCounter deferCounter = ctx.graph().newCounter(1);
                                                if (nodeExisting.length == 0) {
                                                    ii[0] = ctx.graph().newNode(ctx.world(), ctx.time());
                                                    ii[0].set(INVERTEDINDEX_TOKENIZEDCONTENT, Type.LONG, ctx.longVar("relationNodeId"));
                                                    ii[0].set(NODE_TYPE, Type.INT, Integer.valueOf(NODE_TYPE_INVERTED_INDEX));
                                                    ii[0].set(INVERTEDINDEX_TOKEN, Type.INT, enode.id());
                                                    ii[0].set(INVERTEDINDEX_TYPE, Type.STRING, ctx.variable("type").get(0));
                                                    relationIndexed.add(ii[0], INVERTEDINDEX_TOKENIZEDCONTENT);
                                                    deferCounter.count();
                                                } else {
                                                    ctx.graph().lookup(ctx.world(), ctx.time(), nodeExisting[0], result -> {
                                                        ii[0] = result;
                                                        deferCounter.count();
                                                    });
                                                }
                                                deferCounter.then(() -> {
                                                    IntArray position = (IntArray) ii[0].getOrCreate(INVERTEDINDEX_POSITION, Type.INT_ARRAY);
                                                    position.addElement(newIndex);
                                                    ii[0].free();
                                                    ctx.setVariable("newIndex", newIndex + 1);
                                                    ctx.continueTask();
                                                });

                                            } else if (action[1] == MinimumEditDistance.KEEP) {
                                                ENode enode = vocabulary.node(action[0]);
                                                RelationIndexed relationIndexed = (RelationIndexed) enode.get(RELATION_INDEX_TOKEN_II);
                                                relationIndexed.find(result -> {
                                                    IntArray position = (IntArray) result[0].get(INVERTEDINDEX_POSITION);
                                                    position.replaceElementby(formerIndex, newIndex);
                                                    ctx.graph().freeNodes(result);
                                                    ctx.setVariable("formerIndex", formerIndex + 1);
                                                    ctx.setVariable("newIndex", newIndex + 1);
                                                    ctx.continueTask();
                                                }, ctx.world(), ctx.time(), INVERTEDINDEX_TOKENIZEDCONTENT, String.valueOf(ctx.longVar("relationNodeId")));
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
                .thenDo(ctx -> {
                            Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                            Node node = ctx.resultAsNodes().get(0);
                            node.set(NODE_NAME, Type.STRING, ctx.variable("relationVar").get(0));
                            node.set(NODE_TYPE, Type.INT, Integer.valueOf(NODE_TYPE_TOKENIZE_CONTENT));
                            node.set(TOKENIZE_CONTENT_TYPE, Type.STRING, tokenizer.getTypeOfToken());
                            node.set(TOKENIZE_CONTENT_DELIMITERS, Type.BOOL, tokenizer.isKeepingDelimiterActivate());
                            node.set(TOKENIZE_CONTENT_TOKENIZERTYPE, Type.INT, tokenizer.getType());
                            node.getOrCreate(TOKENIZE_CONTENT_PATCH, Type.LONG_TO_LONG_MAP);
                            IntArray tokens = (IntArray) node.getOrCreate(TOKENIZE_CONTENT_TOKENS, Type.INT_ARRAY);
                            node.addToRelation(RELATION_TOKENIZECONTENT_TO_NODE, (Node) ctx.variable("nodeVar").get(0));
                            ((Node) ctx.variable("nodeVar").get(0)).addToRelation(RELATION_INDEX_NODE_TO_TOKENIZECONTENT, node, NODE_NAME);


                            Object[] tokenizedContentVar = ctx.variable("tokenizedContentVar").asArray();
                            int[] words = Arrays.stream(tokenizedContentVar).mapToInt(obj -> (int) obj).toArray();
                            //EGraph eGraph = (EGraph) ((Node) ctx.variable(VOCABULARY_VAR).get(0)).get(VOCABULARY);

                            tokens.initWith(words);

                            /**HashMap<Integer, List<Integer>> mapOfWords = new HashMap();
                             for (int i = 0; i < words.length; i++) {
                             if (mapOfWords.containsKey(words[i])) {
                             mapOfWords.get(words[i]).add(i);
                             } else {
                             List<Integer> position = new ArrayList<>();
                             position.add(i);
                             mapOfWords.put(words[i], position);
                             }
                             }
                             for (Map.Entry<Integer, List<Integer>> entry : mapOfWords.entrySet()) {
                             ENode enode = eGraph.node(entry.getKey());
                             RelationIndexed relationIndexed = (RelationIndexed) enode.getOrCreate(RELATION_INDEX_TOKEN_II, Type.RELATION_INDEXED);
                             Node newInvertedIndex = ctx.graph().newNode(ctx.world(), ctx.time());
                             newInvertedIndex.set(INVERTEDINDEX_TOKENIZEDCONTENT, Type.LONG, node.id());
                             newInvertedIndex.set(NODE_TYPE, Type.INT, Integer.valueOf(NODE_TYPE_INVERTED_INDEX));
                             newInvertedIndex.set(INVERTEDINDEX_TOKEN, Type.INT, enode.id());
                             newInvertedIndex.set(INVERTEDINDEX_TYPE, Type.STRING, tokenizer.getTypeOfToken());
                             relationIndexed.add(newInvertedIndex, INVERTEDINDEX_TOKENIZEDCONTENT);
                             IntArray position = (IntArray) newInvertedIndex.getOrCreate(INVERTEDINDEX_POSITION, Type.INT_ARRAY);
                             position.initWith(entry.getValue().stream().mapToInt(obj -> obj).toArray());
                             newInvertedIndex.free();
                             }*/
                            ctx.continueTask();
                        }
                );

    }

}
