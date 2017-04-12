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
package paw.greycat;

import greycat.Graph;
import greycat.Type;
import greycat.plugin.Plugin;
import paw.greycat.actions.PawctionNames;
import paw.greycat.actions.Pawctions;

public class PawPlugin implements Plugin {
    @Override
    public void start(Graph graph) {

        /**
         * Vocabulary
         */

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.RETRIEVE_VOCABULARY_NODE)
                .setParams()
                .setDescription("retrieve the Vocabulary Node")
                .setFactory(params -> Pawctions.retrieveVocabularyNode());

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.GET_OR_CREATE_TOKENS_FROM_STRINGS)
                .setParams(Type.STRING_ARRAY)
                .setDescription("Retrieve all the node corresponding to tokens stored in a String[] and create one if not existing")
                .setFactory(params -> {
                            if (params[0] != null) {
                                return Pawctions.getOrCreateTokensFromStrings((String[]) params[0]);
                            } else return null;
                        }
                );

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.GET_OR_CREATE_TOKENS_FROM_VAR)
                .setParams(Type.STRING)
                .setDescription("Retrieve all the node corresponding to tokens stored in a variable and create one if not existing")
                .setFactory(params -> Pawctions.getOrCreateTokensFromVar((String) params[0]));


        /**
         *  Tokenization
         */

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.CREATE_TOKENIZER)
                .setParams(Type.STRING, Type.INT, Type.BOOL)
                .setDescription("Create a tokenizer stored it in a var (first parameter) of a given type (second parameter) that keep or not its delimiter (third parameter)")
                .setFactory(params -> Pawctions.createTokenizer((String) params[0], (byte) params[1], (boolean) params[2]));

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.ADD_PREPROCESSORS)
                .setParams(Type.STRING, Type.INT_ARRAY)
                .setDescription("add multiple preprocessor (second parameter) to a tokenizer stored in a var (first parameter) of ")
                .setFactory(params -> {
                    if (params[1] != null) {
                        return Pawctions.addPreprocessors((String) params[0], (byte[]) params[1]);
                    } else return null;
                });

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.SET_TYPE_OF_TOKEN)
                .setParams(Type.STRING, Type.STRING)
                .setDescription("Set the type of token (second parameter) the tokenizer stored in a var(first parameter) receive as input")
                .setFactory(params -> Pawctions.setTypOfToken((String) params[0], (String) params[1]));

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.SET_REMOVE_COMMENTS)
                .setParams(Type.STRING, Type.BOOL)
                .setDescription("Remove comment option working only with the java tokenizer")
                .setFactory(params -> Pawctions.setRemoveContent((String) params[0], (boolean) params[1]));

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.TOKENIZE_FROM_STRINGS)
                .setParams(Type.STRING, Type.STRING_ARRAY)
                .setDescription("tokenize some contents (second parameter) using a tokenizer stored in var(first parameter)")
                .setFactory(params -> {
                    if (params[1] != null) {
                        return Pawctions.tokenizeFromStrings((String) params[0], (String[]) params[1]);
                    } else return null;
                });

        graph.actionRegistry()
                .getOrCreateDeclaration(PawctionNames.TOKENIZE_FROM_VAR)
                .setParams(Type.STRING, Type.STRING)
                .setDescription("tokenize some content store in a var (second parameter) using a tokenizer stored in var(first parameter)")
                .setFactory(params -> Pawctions.tokenizeFromVar((String) params[0], (String) params[1]));
    }

    @Override
    public void stop() {

    }
}
