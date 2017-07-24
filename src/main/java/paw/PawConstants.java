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
package paw;

public class PawConstants {

    /**
     * GraphToken Type
     */
    public final static byte DELIMITER_TOKEN = 0;
    public final static byte NUMBER_TOKEN = 1;
    public final static byte CONTENT_TOKEN = 2;


    /**
     * Tokenizer Type
     */
    public final static byte IDENTITY_TOKENIZER = 0;
    public final static byte SIMPLE_TOKENIZER = 1;
    public final static byte UTF_TOKENIZER = 2;
    public final static byte TWITTER_TOKENIZER = 3;
    public final static byte JAVA_TOKENIZER = 4;
    public final static byte CPP_TOKENIZER = 5;

    /**
     * Index
     */
    public final static String INDEX_CATEGORY = "category";




    /**
     * Attribute Name
     */
    public final static String NODE_TYPE = "Ntype";
    public final static String NODE_NAME = "Nname";

    public final static String RELATION_INDEX_ENTRY_POINT = "entryPoint";
    //public final static String INVERTED_INDEX_NODE_LIST = "list";
    public final static String DELIMITER_VOCABULARY = "delvocab";

    public final static String TOKENIZED_CONTENT_SUB_ID = "subs";
    public final static String TOKENIZED_CONTENT_HASH = "hash";
    public final static String TOKENIZED_CONTENT_MASKS = "masks";
    public final static String TOKENIZED_CONTENT_DELN = "del";
    public final static String TOKENIZED_CONTENT_NUMBER = "numb";

    public final static String TOKENIZED_CONTENT_LIST = "list";

    public final static String SUB_INDEX_HASH_ID = "hash";

    /**
     * Node Types
     */
    public final static int VOCABULARY_NODE = 0;
    public final static int TCLIST_NODE = 1;
    public final static int INDEXING_NODE = 2;
    public final static int DELIMITER_NODE = 3;
    public final static int SUB_INDEXING_NODE = 4;
    public final static int TOKENIZED_CONTENT = 5;

    public final static int SUB_INDEX_RADIX = 6;
    public final static int SUB_INDEX_MAP = 7;
    public final static int SUB_INDEX_II =8;

    /**
     * Relation
     */
    public final static String RELATION_VOCAB_CACHE_INDEX_DEL = "cacheIndex";
    public final static String RELATION_INDEXED_INDEX_TO_SUBINDEX = "subIndex";
    public final static String RELATION_SUBINDEX_TO_DATA = "data";
    public final static String RELATION_INDEXED_NODE_TO_TOKENIZECONTENT = "tokenizedContents";
    public final static String RELATION_TOKENIZECONTENT_TO_NODE = "fatherNode";

    public final static String RELATION_INDEXED_II_TO_SUBII  = "subii";
}
