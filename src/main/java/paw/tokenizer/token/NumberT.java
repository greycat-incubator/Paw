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
package paw.tokenizer.token;

import paw.PawConstants;

public class NumberT implements Token {

    private final int integer;

    public NumberT(int integer) {
        this.integer = integer;
    }

    @Override
    public String getToken() {
        return String.valueOf(integer);
    }

    @Override
    public byte getType() {
        return PawConstants.NUMBER_TOKEN;
    }

    public int getInt() {
        return integer;
    }
}
