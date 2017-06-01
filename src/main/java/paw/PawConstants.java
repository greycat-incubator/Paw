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
     * Threshold
     */
    public final static int CACHE_THRESHOLD = 5;

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
     * Attribute Name
     */
    public final static String NODE_TYPE = "Ntype";
    public final static String NODE_NAME = "Nname";
    public final static String CACHE_MAP_HASH_ID = "mapHashId";
    public final static String CACHE_EGRAPH = "egraph";
    public final static String RELATION_INDEX_ENTRY_POINT = "entryPoint";
    public final static String INDEXING_NODE_RADIX_TREE = "iradix";
    public final static String INDEXING_NODE_MAP_HASH_ID = "mapHashId";
    public final static String CACHING_ENODE_COUNT = "count";
    public final static String CACHING_ENODE_CONTENT = "content";
    public final static String CACHING_ENODE_WHEN_WHERE = "whenwhere";
    public final static String INVERTED_INDEX_NODE_LIST = "list";
    public final static String TOKENIZED_CONTENT_TOKENS = "tokens";
    public final static String EGRAPH_TOKEN_TYPE = "type";
    public final static String EGRAPH_TOKEN_MASK = "mask";
    public final static String EGRAPH_TOKEN_CONTENT = "content";
    public final static String EGRAPH_TOKEN_CACHE_SUB = "cache";
    public final static String EGRAPH_TOKEN_ID = "id";
    public final static String LOCALSTATIC_MAP = "lsmap";
    public final static String LOCALSTATIC_DELIMITER = "delim";
    public final static String LOCALSTATIC_CACHE = "cache";
    public final static String LOCALSTATIC_NUMBER = "number";
    /**
     * Node Types
     */
    public final static int VOCABULARY_NODE = 0;
    public final static int CACHING_NODE = 1;
    public final static int INDEXING_NODE = 2;
    public final static int SUB_CACHING_NODE = 3;
    public final static int TOKENIZED_CONTENT = 4;
    public final static int LOCAL_STATISTIC = 5;
    /**
     * Relation
     */
    public final static String RELATION_INDEXED_MAIN_NODES_CACHE_INDEX = "cacheIndex";
    public final static String RELATION_INDEXED_CACHE_TO_SUBCACHE = "subcaches";
    public final static String RELATION_INDEXED_NODE_TO_TOKENIZECONTENT = "tokenizedContents";
    public final static String RELATION_TOKENIZECONTENT_TO_NODE = "fatherNode";
    public final static String RELATION_TOKENIZECONTENT_TO_LOCAL_STAT = "localStat";


}
