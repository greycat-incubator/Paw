package paw.greycat.actions.tokenization;

import greycat.Action;
import greycat.Constants;
import greycat.TaskContext;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import paw.greycat.actions.PawctionNames;
import paw.greycat.tasks.TokenizationTasks;

public class ActionTokenizeFromStrings implements Action {

    private final String[] _toTokenize;
    private final String _tokenizer;

    public ActionTokenizeFromStrings(String p_tokenizer, String... p_toTokenize) {
        this._tokenizer = p_tokenizer;
        this._toTokenize = p_toTokenize;
    }

    @Override
    public void eval(TaskContext ctx) {
        TokenizationTasks.tokenizeFromStrings(_tokenizer, _toTokenize)
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
        builder.writeString(PawctionNames.TOKENIZE_FROM_STRINGS);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizer, builder, true);
        builder.writeChar(Constants.TASK_PARAM_SEP);
        TaskHelper.serializeStringParams(_toTokenize, builder);
        builder.writeChar(Constants.TASK_PARAM_CLOSE);

    }
}
