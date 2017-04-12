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

import paw.tokeniser.TokenPreprocessor;

/**
 * Lower case Token Preprocessor
 */
public class LowerCasePreprocessor implements TokenPreprocessor {

    public final static String ID = "LOWER CASE PREPROCESSOR";

    /**
     * @param token on which to apply the transformation
     * @return token in lower case
     */
    @Override
    public String transform(String token) {
        return token.toLowerCase();
    }

    @Override
    public String toString(){
        return ID;
    }
}
