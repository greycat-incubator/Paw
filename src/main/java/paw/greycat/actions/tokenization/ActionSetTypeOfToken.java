package paw.greycat.actions.tokenization;

import greycat.Action;
import greycat.Constants;
import greycat.TaskContext;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import paw.greycat.actions.PawctionNames;
import paw.greycat.tasks.TokenizationTasks;

public class ActionSetTypeOfToken implements Action {

    private final String _tokenizer;
    private final String _type;

    public ActionSetTypeOfToken(String p_tokenizer, String p_type) {
        this._tokenizer = p_tokenizer;
        this._type = p_type;
    }

    @Override
    public void eval(TaskContext ctx) {
        TokenizationTasks.setTypeOfToken(_tokenizer, _type)
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
        builder.writeString(PawctionNames.SET_TYPE_OF_TOKEN);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizer, builder, true);
        builder.writeChar(Constants.TASK_PARAM_SEP);
        TaskHelper.serializeString(_type, builder, true);
        builder.writeChar(Constants.TASK_PARAM_CLOSE);
    }
}
