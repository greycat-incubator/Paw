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
import paw.tokeniser.Tokenizer;
import paw.tokeniser.preprocessing.PreprocessorFactory;
import paw.tokeniser.tokenisation.TokenizerFactory;
import paw.tokeniser.tokenisation.pl.java.JavaTokenizer;

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
    public static Task createTokenizer(String tokenizerVar, byte tokenizerType, boolean keepDelimiter) {
        Tokenizer tokenizer = TokenizerFactory.getTokenizer(tokenizerType);
        assert tokenizer != null;
        tokenizer.setKeepDelimiter(keepDelimiter);
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
     * Task only working in the case of a Java Tokenizer stored in a var that indicate wether the tokenizer should keep comments
     *
     * @param tokenizerVar   variable in which the tokenizer is stored
     * @param removeComments should the comments be kept
     * @return Task with the current result unchanged
     */
    public static Task setRemoveComment(String tokenizerVar, boolean removeComments) {
        return newTask()
                .thenDo(ctx -> {
                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                    if (tokenizer instanceof JavaTokenizer) {
                        ((JavaTokenizer) tokenizer).setRemoveComments(removeComments);
                        ctx.setVariable(tokenizerVar, tokenizer);
                    }
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
                .inject(content)
                .pipe(tokenize(tokenizerVar));
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
                .inject(readers)
                .map(
                        newTask()
                                .thenDo(ctx -> {
                                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                                    Reader content = (Reader) ctx.result().get(0);
                                    try {
                                        String[] result = tokenizer.tokenize(content);
                                        content.close();
                                        ctx.continueWith(ctx.wrap(result));
                                    } catch (IOException e) {
                                        ctx.endTask(ctx.result(), e);
                                    }
                                })
                );
    }

    /**
     * Task to tokenize String stored in a var using a tokenizer stored in a var
     *
     * @param tokenizerVar variable in which the tokenizer is stored
     * @param variable     in which the string to tokenize are stored
     * @return Task with array of tokenized content in the current result
     */
    public static Task tokenizeFromVar(String tokenizerVar, String variable) {
        return newTask()
                .readVar(variable)
                .pipe(tokenize(tokenizerVar));
    }


    private static Task tokenize(String tokenizerVar) {
        return newTask()
                .map(
                        newTask()
                                .thenDo(ctx -> {
                                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                                    assert (ctx.result().get(0) instanceof String);
                                    String content = ctx.resultAsStrings().get(0);
                                    ctx.continueWith(ctx.wrap(tokenizer.tokenize(content)));
                                })
                );
    }

}
