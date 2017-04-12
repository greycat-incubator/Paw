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
package paw;

public class PawConstants {


    public final static String TYPE_TOKEN_WITHOUT_TYPE = "noType";


    public final static String RELATION_INDEX_ENTRY_POINT = "entryPoint";
    public final static String RELATION_INDEX_NODE_TO_TOKENIZECONTENT = "tokenizedContents";
    public final static String RELATION_TOKENIZECONTENT_TO_NODE = "father";
    public final static String RELATION_INDEX_TOKEN_II = "invertedIndex";



    public final static String NODE_NAME = "name";

    public final static String NODE_TYPE = "type";
    public final static String NODE_TYPE_VOCABULARY = "0";
    public final static String NODE_TYPE_TOKENIZE_CONTENT = "1";
    public final static String NODE_TYPE_INVERTED_INDEX = "2";


    public final static String VOCABULARY = "vocabulary";


    public final static String TOKENIZE_CONTENT_PLUGIN = "plugin";
    public final static String TOKENIZE_CONTENT_PATCH = "patch";
    public final static String TOKENIZE_CONTENT_DELIMITERS = "delimiters";
    public final static String TOKENIZE_CONTENT_TYPE = "typeOfToken";
    public final static String TOKENIZE_CONTENT_TOKENIZERTYPE = "typeOfTokenizer";
    public final static String TOKENIZE_CONTENT_TOKENS = "tokens";

    public final static String INVERTEDINDEX_TOKENIZEDCONTENT = "tokenizedContent";
    public final static String INVERTEDINDEX_POSITION = "position";
    public final static String INVERTEDINDEX_TOKEN = "token";
    public final static String INVERTEDINDEX_TYPE = "typeOfToken";
}
