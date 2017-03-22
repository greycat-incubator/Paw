package paw.greycat.actions.tokenization;

import greycat.*;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import paw.greycat.actions.PawctionNames;
import paw.greycat.tasks.TokenizationTasks;
import paw.tokeniser.preprocessing.PreprocessorType;

public class ActionAddPreprocessors implements Action {

    private final String _tokenizer;
    private final byte[] _preprocessor;

    public ActionAddPreprocessors(String p_tokenizer, byte... p_preprocessor){
        this._tokenizer = p_tokenizer;
        this._preprocessor = p_preprocessor;
    }

    @Override
    public void eval(TaskContext ctx) {
        TokenizationTasks.addPreprocessors(_tokenizer,_preprocessor)
                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD,
                        res -> {
                            Exception exceptionDuringTask = null;
                            if (res != null) {
                                if (res.output() != null) {
                                    ctx.append(res.output());
                                }
                                if (res.exception() != null) {
                                    exceptionDuringTask = res.exception();
                                }
                            }
                            if (exceptionDuringTask != null) {
                                ctx.endTask(res, exceptionDuringTask);
                            } else {
                                ctx.continueWith(res);
                            }
                        });
    }

    @Override
    public void serialize(Buffer builder) {
        builder.writeString(PawctionNames.ADD_PREPROCESSORS);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizer,builder,true);
        for(int i = 0;i<_preprocessor.length;i++ ){
            builder.writeChar(Constants.TASK_PARAM_SEP);
            builder.writeString(PreprocessorType.typeName(_preprocessor[i]));
        }
        builder.writeChar(Constants.TASK_PARAM_CLOSE);
    }
}
