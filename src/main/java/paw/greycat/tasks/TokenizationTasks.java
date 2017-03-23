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
import static mylittleplugin.MyLittleActions.injectAsVar;

public class TokenizationTasks {

    public static Task createTokenizer(String tokenizerVar, byte tokenizerType, boolean keepDelimiter) {
        Tokenizer tokenizer = TokenizerFactory.getTokenizer(tokenizerType);
        assert tokenizer != null;
        tokenizer.setKeepDelimiter(keepDelimiter);
        return newTask().thenDo(ctx -> {
            ctx.setGlobalVariable(tokenizerVar, tokenizer);
            ctx.continueTask();
        });
    }

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

    public static Task setTypeOfToken(String tokenizerVar, String type) {
        return newTask()
                .thenDo(ctx -> {
                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                    tokenizer.setTypeOfToken(type);
                    ctx.setVariable(tokenizerVar, tokenizer);
                    ctx.continueTask();
                });
    }

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

    public static Task tokenizeFromStrings(String tokenizerVar, String... content) {
        return newTask()
                .then(injectAsVar("mycontents", content))
                .pipe(tokenizeFromVar(tokenizerVar, "mycontents"));
    }

    public static Task tokenizeFromReader(String tokenizerVar, Reader... reader) {
        return newTask()
                .inject(reader)
                .map(
                        newTask()
                                .thenDo(ctx -> {
                                    Tokenizer tokenizer = (Tokenizer) ctx.variable(tokenizerVar).get(0);
                                    Reader content = (Reader) ctx.result().get(0);
                                    try {
                                        String[] result = tokenizer.tokenize(content);
                                        ctx.continueWith(ctx.wrap(result));
                                    } catch (IOException e) {
                                        ctx.endTask(ctx.result(), e);
                                    }
                                })
                );
    }

    public static Task tokenizeFromVar(String tokenizerVar, String variable) {
        return newTask()
                .readVar(variable)
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
