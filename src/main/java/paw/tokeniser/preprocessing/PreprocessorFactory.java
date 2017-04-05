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
package paw.tokeniser.preprocessing;

import paw.tokeniser.TokenPreprocessor;

public class PreprocessorFactory {

    /**
     * Method to retrieve a preprocessor instance from its byte representation
     * @param preprocessorType byte representation of the preprocessor type
     * @return the token preprocessor
     */
    public static TokenPreprocessor getPreprocessor(byte preprocessorType) {
        switch (preprocessorType) {
            case PreprocessorType.UPPER_CASE:
                return new UpperCasePreprocessor();
            case PreprocessorType.LOWER_CASE:
                return new LowerCasePreprocessor();
            default:
                return null;
        }
    }
}
