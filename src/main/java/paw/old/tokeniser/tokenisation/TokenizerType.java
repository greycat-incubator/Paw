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
package paw.old.tokeniser.tokenisation;

import paw.old.tokeniser.tokenisation.misc.IdentityTokenizer;
import paw.old.tokeniser.tokenisation.misc.SimpleTokenizer;
import paw.old.tokeniser.tokenisation.misc.TwitterTokenizer;
import paw.old.tokeniser.tokenisation.misc.UTFTokeniser;
import paw.old.tokeniser.tokenisation.nl.EnglishTokenizer;
import paw.old.tokeniser.tokenisation.pl.c.CTokenizer;
import paw.old.tokeniser.tokenisation.pl.java.JavaTokenizer;

public class TokenizerType {

    /**
     * Primitive Types
     */
    public static final byte IDENTITY = 1;
    public static final byte SIMPLE = 2;
    public static final byte UTF = 3;
    public static final byte TWITTER = 4;

    /**
     * Natural language
     */
    public static final byte ENGLISH = 5;

    /**
     *
     */
    public static final byte C = 10;
    public static final byte CPP = 11;
    public static final byte JAVA = 12;

    /**
     * Convert a Tokenizer type that represent a byte to a readable String representation
     *
     * @param p_type byte encoding a particular Tokenizer type
     * @return readable string representation of the type
     */
    public static String typeName(byte p_type) {
        switch (p_type) {
            case IDENTITY:
                return IdentityTokenizer.ID;
            case SIMPLE:
                return SimpleTokenizer.ID;
            case UTF:
                return UTFTokeniser.ID;
            case TWITTER:
                return TwitterTokenizer.ID;
            case ENGLISH:
                return EnglishTokenizer.ID;
            case C:
                return CTokenizer.ID;
            case CPP:
                return CPPTokenizer.ID;
            case JAVA:
                return JavaTokenizer.ID;
            default:
                return "unknown";
        }
    }

    /**
     * Convert a String Representation of a tokenizer type to its byte representation
     *
     * @param name String representation of a Tokenizer
     * @return byte representation of the tokenization type
     */
    public static byte typeFromName(String name) {
        switch (name) {
            case IdentityTokenizer.ID:
                return IDENTITY;
            case SimpleTokenizer.ID:
                return SIMPLE;
            case UTFTokeniser.ID:
                return UTF;
            case TwitterTokenizer.ID:
                return TWITTER;
            case EnglishTokenizer.ID:
                return ENGLISH;
            case CTokenizer.ID:
                return C;
            case CPPTokenizer.ID:
                return CPP;
            case JavaTokenizer.ID:
                return JAVA;
            default:
                return -1;
        }
    }


}
