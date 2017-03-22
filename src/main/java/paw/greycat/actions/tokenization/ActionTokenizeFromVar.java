package paw.greycat.actions.tokenization;

import greycat.Action;
import greycat.Constants;
import greycat.TaskContext;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import paw.greycat.actions.PawctionNames;
import paw.greycat.tasks.TokenizationTasks;

public class ActionTokenizeFromVar implements Action {

    private final String _tokenizer;
    private final String _var;

    public ActionTokenizeFromVar(String p_tokenizer, String p_var){
        this._tokenizer = p_tokenizer;
        this._var = p_var;
    }

    @Override
    public void eval(TaskContext ctx) {
        TokenizationTasks.tokenizeFromVar(_tokenizer,_var)
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
        builder.writeString(PawctionNames.TOKENIZE_FROM_VAR);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizer, builder, true);
        builder.writeChar(Constants.TASK_PARAM_SEP);
        TaskHelper.serializeString(_var, builder, true);
        builder.writeChar(Constants.TASK_PARAM_CLOSE);
    }
}
