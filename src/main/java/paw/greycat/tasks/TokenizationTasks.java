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
package paw.greycat.tasks;

import greycat.Task;
import paw.tokeniser.TokenPreprocessor;
import paw.tokeniser.TokenizedString;
import paw.tokeniser.Tokenizer;
import paw.tokeniser.preprocessing.PreprocessorFactory;
import paw.tokeniser.tokenisation.TokenizerFactory;

import java.io.IOException;
import java.io.Reader;

import static greycat.Tasks.newTask;

/**
 * Class  wrapping tokenization method into tasks
 */
public class TokenizationTasks {


    /**
     * Task creating a tokenizer
     *
     * @param tokenizerVar  variable in which the tokenizer should be stored
     * @param tokenizerType type of the tokenizer
     * @param keepDelimiter should the delimiter appear in the tokenized contents
     * @return Task with the current result unchanged
     */
    public static Task createTokenizer(String tokenizerVar, byte tokenizerType) {
        Tokenizer tokenizer = TokenizerFactory.getTokenizer(tokenizerType);
        assert tokenizer != null;
        return newTask().thenDo(ctx -> {
            ctx.setGlobalVariable(tokenizerVar, tokenizer);
            ctx.continueTask();
        });
    }

    /**
     * Task adding preprocessors to a tokenizer stored in a variable
     *
     * @param tokenizerVar     variable in which the tokenizer is stored
     * @param preprocessorType type of preprocessor
     * @return Task with the current result unchanged
     */
    public static Task addPreprocessors(String tokenizerVar, byte... preprocessorType) {
        return newTask()
                .thenDo(ctx -> {
                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                    for (byte aPreprocessorType : preprocessorType) {
                        TokenPreprocessor prep = PreprocessorFactory.getPreprocessor(aPreprocessorType);
                        assert prep != null;
                        tokenizer.addPreprocessor(prep);
                    }
                    ctx.setVariable(tokenizerVar, tokenizer);
                    ctx.continueTask();
                });
    }

    /**
     * Task setting the type of Token that the tokenizer stored in a var expect to receive
     *
     * @param tokenizerVar variable in which the tokenizer is stored
     * @param type         of the token
     * @return Task with the current result unchanged
     */
    public static Task setTypeOfToken(String tokenizerVar, String type) {
        return newTask()
                .thenDo(ctx -> {
                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                    tokenizer.setTypeOfToken(type);
                    ctx.setVariable(tokenizerVar, tokenizer);
                    ctx.continueTask();
                });
    }

    /**
     * Task to tokenize an array of String using a tokenizer stored in a var
     *
     * @param tokenizerVar variable in which the tokenizer is stored
     * @param content      array of String to tokenize
     * @return Task with array of tokenized content in the current result
     */
    public static Task tokenizeFromStrings(String tokenizerVar, String... content) {
        return newTask()
                .thenDo(ctx -> {
                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                    TokenizedString[] tokenizedStrings = new TokenizedString[content.length];
                    for (int i = 0; i < content.length; i++) {
                        tokenizedStrings[i] = tokenizer.tokenize(content[i]);
                    }
                    ctx.continueWith(ctx.wrap(tokenizedStrings));
                });
    }

    /**
     * Task to tokenize from several readers using a tokenizer stored in a var
     *
     * @param tokenizerVar variable in which the tokenizer is stored
     * @param readers      array of readers
     * @return Task with array of tokenized content in the current result
     */
    public static Task tokenizeFromReader(String tokenizerVar, Reader... readers) {
        return newTask()
                .thenDo(ctx -> {
                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                    TokenizedString[] tokenizedStrings = new TokenizedString[readers.length];
                    for (int i = 0; i < readers.length; i++) {
                        try {
                            tokenizedStrings[i] = tokenizer.tokenize(readers[i]);
                            readers[i].close();
                        } catch (IOException e) {
                            ctx.endTask(ctx.result(), e);
                        }
                    }
                    ctx.continueWith(ctx.wrap(tokenizedStrings));
                });
    }


    /**static Task tokenize(String tokenizerVar) {
        return newTask()
                .thenDo(ctx -> {
                    TaskResult<String> toTokenize = ctx.resultAsStrings();
                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                    TokenizedString[] tokenizedStrings = new TokenizedString[toTokenize.size()];
                    for (int i = 0; i < toTokenize.size(); i++) {
                        tokenizedStrings[i] = tokenizer.tokenize(toTokenize.get(i));
                    }
                    ctx.continueWith(ctx.wrap(tokenizedStrings));
                });
    }*/

}
