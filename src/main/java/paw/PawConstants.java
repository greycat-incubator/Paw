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
    public final static String INDEX_DICTIONNARY = "dictionnary";
    public final static String INDEX_DELIMITER = "delimiter";



    /**
     * Attribute Name
     */
    public final static String CATEGORY_OF_TOKENIZE_CONTENT = "category";
    public final static String NODE_TYPE = "Ntype";

    public final static String RELATION_INDEX_ENTRY_POINT = "entryPoint";

}
