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
import greycat.TaskContext;
import greycat.Type;
import greycat.struct.IntIntMap;
import greycat.struct.IntStringMap;
import paw.tokeniser.TokenizedString;
import paw.tokeniser.Tokenizer;

import java.util.Map;
import java.util.Set;

import static greycat.Tasks.newTask;
import static mylittleplugin.MyLittleActions.ifEmptyThenElse;
import static paw.PawConstants.*;
import static paw.greycat.tasks.VocabularyTasks.VOCABULARY_MAP;
import static paw.greycat.tasks.VocabularyTasks.retrieveVocabularyNode;

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
                // retrive vocabulary Node
                .ifThen(ctx -> ctx.variable(VOCABULARY_MAP) == null,
                        retrieveVocabularyNode())
                .thenDo(ctx -> {
                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                    TokenizedString tokenizedString = tokenizer.tokenize(content);
                    Map<Integer, String> tokenPosition = tokenizedString.get_tokensPosition();
                    if (tokenPosition == null) {
                        ctx.setVariable("positionIndexedTokens", new int[0]);
                        ctx.setVariable("indexedTokens", new int[0]);
                    } else {
                        Set<Map.Entry<Integer, String>> setToIndex = tokenPosition.entrySet();
                        int[] positionIndexedTokens = new int[setToIndex.size()];
                        String[] toIndex = setToIndex.stream().map(Map.Entry::getValue).toArray(String[]::new);
                        int[] indexedTokens = VocabularyTasks.retriveNodes(ctx, toIndex);
                        ctx.setVariable("positionIndexedTokens", positionIndexedTokens);
                        ctx.setVariable("indexedTokens", indexedTokens);
                    }
                    ctx.setVariable("sizeTC", tokenizedString.get_size());
                    ctx.setVariable("outcast", tokenizedString.get_outcastPosition());
                    ctx.setVariable("delimitersPosition", tokenizedString.get_delimitersPosition());
                    ctx.setVariable("integerPosition", tokenizedString.get_numberPosition());
                    ctx.continueTask();
                })
                .readVar(nodesVar)
                .forEach(
                        newTask()
                                .defineAsVar("nodeVar")
                                .pipe(uocTokenizeRelation(tokenizerVar, relationName))
                );

    }

    /**
     * @param tokenizerVar
     * @return
     */
    private static Task uocTokenizeRelation(String tokenizerVar, String relationName) {
        return newTask()
                .thenDo(ctx -> {
                    Node node = (Node) ctx.variable("nodeVar").get(0);
                    node.travelInTime(ctx.time(), result -> {
                        ctx.setVariable("nodeVar", result);
                        node.free();
                        ctx.continueWith(ctx.wrap(result));
                    });
                })
                .traverse(RELATION_INDEX_NODE_TO_TOKENIZECONTENT, NODE_NAME, relationName)
                .then(ifEmptyThenElse(
                        createTokenRelation(tokenizerVar, relationName),
                        updateTokenRelation(tokenizerVar)
                ));
    }

    /**
     * @param tokenizerVar
     * @return
     */
    private static Task updateTokenRelation(String tokenizerVar) {
        return newTask()
                /**.defineAsVar("relationNode")
                .then(checkForFuture())
                .thenDo(ctx -> {
                    Node node = ctx.resultAsNodes().get(0);
                    long dephasing = node.timeDephasing();
                    if (dephasing == 0L) {
                        ctx.endTask(ctx.result(), new RuntimeException("Trying to modify a tokenize content at the time of the previous modification"));
                    } else {
                        //long relationNodeId = node.id();
                        String type = (String) node.get(TOKENIZE_CONTENT_TYPE);
                        Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                        if (!type.equals(tokenizer.getTypeOfToken()) || ((boolean) node.get(TOKENIZE_CONTENT_DELIMITERS) != tokenizer.isKeepingDelimiterActivate())) {
                            ctx.endTask(ctx.result(), new RuntimeException("Different Tokenizer use for the update"));
                        }
                        LongLongMap mapPatch = (LongLongMap) node.getOrCreate(TOKENIZE_CONTENT_PATCH, Type.LONG_TO_LONG_MAP);
                        IntArray relation = (IntArray) node.get(TOKENIZE_CONTENT_TOKENS);
                        int[] relationsId = IntArrayHandler.uncompress(relation.extract());
                        Object[] newContent = ctx.variable("tokenizedContentVar").asArray();
                        int[] newContentId = Arrays.stream(newContent).mapToInt(obj -> (int) obj).toArray();
                        relation.initWith(IntArrayHandler.compress(newContentId));
                        MinimumEditDistance med = new MinimumEditDistance(relationsId, newContentId);
                        List<int[]> path = med.path();
                        //int formerIndex = 0;
                        int newIndex = 0;
                        //EGraph vocabulary = (EGraph) ((Node) ctx.variable(VOCABULARY_VAR).get(0)).get(VOCABULARY);
                        for (int i = 0; i < path.size(); i++) {
                            if (path.get(i)[1] == MinimumEditDistance.DELETION) {
                                //relation.removeElementbyIndex(newIndex);
                                mapPatch.put(-i, path.get(i)[0]);
                                //formerIndex++;
                            } else if (path.get(i)[1] == MinimumEditDistance.INSERTION) {
                                //relation.insertElementAt(newIndex, path.get(i)[0]);
                                mapPatch.put(i, path.get(i)[0]);
                                newIndex++;
                            } else if (path.get(i)[1] == MinimumEditDistance.KEEP) {
                                //formerIndex++;
                                newIndex++;
                            }
                        }
                    }
                    ctx.continueTask();
                })*/;

    }

    /**
     * @param tokenizerVar
     * @return
     */

    private static Task createTokenRelation(String tokenizerVar, String relationName) {
        return newTask()
                .thenDo((TaskContext ctx) -> {

                            Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);

                            Node node = ctx.graph().newNode(ctx.world(), ctx.time());
                            node.set(NODE_TYPE, Type.INT, Integer.valueOf(NODE_TYPE_TOKENIZE_CONTENT));

                            String typeOfToken = tokenizer.getTypeOfToken();
                            node.set(TOKENIZE_CONTENT_TYPE, Type.STRING, typeOfToken);
                            node.set(NODE_NAME, Type.STRING, relationName);
                            node.set(TOKENIZE_CONTENT_TOKENIZERTYPE, Type.INT, tokenizer.getType());

                            Object[] indexedTokens = ctx.variable("indexedTokens").asArray();
                            Object[] positionIndexedTokens = ctx.variable("positionIndexedTokens").asArray();
                            Map<Integer, String> outcastPosition = (Map<Integer, String>) ctx.variable("outcast").get(0);
                            int sizeTC = ctx.intVar("sizeTC");
                            Map<Integer, Integer> delimitersPosition = (Map<Integer, Integer>) ctx.variable("delimitersPosition").get(0);
                            Map<Integer, Integer> integerPosition = (Map<Integer, Integer>) ctx.variable("integerPosition").get(0);


                            IntIntMap itokens = (IntIntMap) node.getOrCreate(TOKENIZE_CONTENT_INDEXEDTOKENS, Type.INT_TO_INT_MAP);
                            for (int i = 0; i < indexedTokens.length; i++) {
                                itokens.put((int)positionIndexedTokens[i], (int)indexedTokens[i]);
                            }


                            IntIntMap delimiters = (IntIntMap) node.getOrCreate(TOKENIZE_CONTENT_DELIMITERS, Type.INT_TO_INT_MAP);
                            if (delimitersPosition != null) {
                                for (Map.Entry<Integer, Integer> delimiter : delimitersPosition.entrySet()) {
                                    delimiters.put(delimiter.getKey(), delimiter.getValue());
                                }
                            }

                            IntIntMap integers = (IntIntMap) node.getOrCreate(TOKENIZE_CONTENT_INTEGER, Type.INT_TO_INT_MAP);
                            if (integerPosition != null) {
                                for (Map.Entry<Integer, Integer> integer : integerPosition.entrySet()) {
                                    integers.put(integer.getKey(), integer.getValue());
                                }
                            }
                            IntStringMap outcasts = (IntStringMap) node.getOrCreate(TOKENIZE_CONTENT_OUTCAST, Type.INT_TO_STRING_MAP);
                            if (outcastPosition != null) {
                                for (Map.Entry<Integer, String> outcast : outcastPosition.entrySet()) {
                                    outcasts.put(outcast.getKey(), outcast.getValue());
                                }
                            }

                            node.set(TOKENIZE_CONTENT_SIZE, Type.INT, sizeTC);
                            node.addToRelation(RELATION_TOKENIZECONTENT_TO_NODE, (Node) ctx.variable("nodeVar").get(0));
                            ((Node) ctx.variable("nodeVar").get(0)).addToRelation(RELATION_INDEX_NODE_TO_TOKENIZECONTENT, node, NODE_NAME);
                            node.free();
                            ctx.continueTask();
                        }
                );

    }

}
