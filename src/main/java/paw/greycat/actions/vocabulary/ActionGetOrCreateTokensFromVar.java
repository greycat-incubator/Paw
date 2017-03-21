package paw.greycat.actions.vocabulary;

import greycat.*;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import paw.greycat.actions.PawctionNames;
import paw.greycat.tasks.VocabularyTasks;

public class ActionGetOrCreateTokensFromVar implements Action {

    private final String _variable;

    public ActionGetOrCreateTokensFromVar(final String p_variable) {
        this._variable = p_variable;
    }

    @Override
    public void eval(TaskContext ctx) {
        VocabularyTasks.getOrCreateTokensFromVar(_variable)
                .executeFrom(ctx, ctx.result(), SchedulerAffinity.SAME_THREAD,
                        new Callback<TaskResult>() {
                            public void on(TaskResult res) {
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
                            }
                        });
    }

    @Override
    public void serialize(Buffer builder) {
        builder.writeString(PawctionNames.GET_OR_CREATE_TOKENS_FROM_STRINGS);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        if (_variable != null) {
            TaskHelper.serializeString(_variable, builder, true);
        }
        builder.writeChar(Constants.TASK_PARAM_CLOSE);
    }
}
