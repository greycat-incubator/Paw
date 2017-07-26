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
package paw.graph.customTypes.tokenizedContent;

public class Word {
    protected final byte type;
    protected final int wordID;
    protected int firstChar;


    public Word(byte type, int wordID) {
        this.type = type;
        this.wordID = wordID;
    }

    public Word(byte type, int wordID, int firstChar) {
        this.type = type;
        this.wordID = wordID;
        this.firstChar = firstChar;
    }

    public byte getType() {
        return type;
    }

    public int getWordID() {
        return wordID;
    }

    public int getFirstChar() {
        return firstChar;
    }

}
