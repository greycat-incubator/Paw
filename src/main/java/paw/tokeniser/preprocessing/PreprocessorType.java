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
package paw.tokeniser.preprocessing;

public class PreprocessorType {

    /**
     * Primitive Types
     */
    public static final byte LOWER_CASE = 1;
    public static final byte UPPER_CASE = 2;

    /**
     * Convert a Preprocessor type that represent a byte to a readable String representation
     *
     * @param p_type byte encoding a particular Preprocessor type
     * @return readable string representation of the type
     */
    public static String typeName(byte p_type) {
        switch (p_type) {
            case LOWER_CASE:
                return LowerCasePreprocessor.ID;
            case UPPER_CASE:
                return UpperCasePreprocessor.ID;
            default:
                return "unknown";
        }
    }

    /**
     * Convert a String Representation of a Preprocessor type to its byte representation
     *
     * @param name String representation of a Preprocessor
     * @return byte representation of the tokenization type
     */
    public static byte typeFromName(String name) {
        switch (name) {
            case LowerCasePreprocessor.ID:
                return LOWER_CASE;
            case UpperCasePreprocessor.ID:
                return UPPER_CASE;
            default:
                return -1;
        }
    }

}
