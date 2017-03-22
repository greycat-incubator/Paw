package paw.greycat.actions.tokenization.java;

import greycat.*;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import paw.greycat.actions.PawctionNames;
import paw.greycat.tasks.TokenizationTasks;

public class ActionSetRemoveComment implements Action{

    private final String _tokenizer;
    private final boolean _removeComments;

    public ActionSetRemoveComment(String p_tokenizer, boolean p_removeComment){
        this._tokenizer = p_tokenizer;
        this._removeComments = p_removeComment;
    }
    
    @Override
    public void eval(TaskContext ctx) {
        TokenizationTasks.setRemoveComment(_tokenizer,_removeComments)
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
        builder.writeString(PawctionNames.SET_REMOVE_COMMENTS);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizer,builder,true);
        builder.writeChar(Constants.TASK_PARAM_SEP);
        builder.writeString(String.valueOf(_removeComments));
        builder.writeChar(Constants.TASK_PARAM_CLOSE);
    }
}
