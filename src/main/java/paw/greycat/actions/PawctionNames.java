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
package paw.greycat.actions;

public class PawctionNames {
    /**
     * Vocabulary
     */
    public static String RETRIEVE_VOCABULARY_NODE = "retrieveVocabularyNode";
    public static String GET_OR_CREATE_TOKENS_FROM_STRINGS = "getOrCreateTokensFromStrings";
    public static String GET_OR_CREATE_TOKENS_FROM_VAR = "getOrCreateTokensFromVar";

    /**
     * Tokenization
     */
    public static String CREATE_TOKENIZER = "createTokenizer";
    public static String ADD_PREPROCESSORS = "addPreprocessors";
    public static String SET_TYPE_OF_TOKEN = "setTypeOfToken";
    public static String SET_REMOVE_COMMENTS = "setRemoveContent";
    public static String TOKENIZE_FROM_VAR = "tokenizeFromVar";
    public static String TOKENIZE_FROM_STRINGS = "tokenizeFromStrings";

    /**
     * Tokenized Content
     */
    public static String UPDATE_OR_CREATE_TOKENIZE_RELATION_FROM_STRING = "uocTokenizeRelationFromString";
    public static String UPDATE_OR_CREATE_TOKENIZE_RELATION_FROM_VAR = "uocTokenizeRelationFromVar";
}
