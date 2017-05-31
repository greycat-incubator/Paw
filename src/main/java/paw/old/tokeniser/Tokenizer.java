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
 *//*

package paw.old.tokeniser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

*/
/**
 * Tokenizer abstract class
 *//*

public abstract class Tokenizer {


    */
/**
     * Tokenize
     *//*


    */
/**
     * Method to tokenize a reader
     *
     * @param reader to use
     * @return an array of token on which all preprocessor registered have been applied
     * @throws IOException in case of reader exception
     *//*

    public abstract TokenizedString tokenize(Reader reader) throws IOException;

    */
/**
     * Method to tokenize a string
     *
     * @param s string to tokenize
     * @return an array of token on which all preprocessor registered have been applied
     *//*

    public TokenizedString tokenize(String s) {
        try {
            Reader r = new StringReader(s);
            TokenizedString result = tokenize(r);
            r.close();
            return result;
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }


    */
/**
     * Outcast Check
     *//*

    protected boolean checkContent = false;

    public boolean isCheckContent() {
        return checkContent;
    }

    public void setCheckContent(boolean checkContent) {
        this.checkContent = checkContent;
    }

    */
/**
     * Type of the Tokenizer
     * @return
     *//*

    public abstract byte getType();
}
*/
