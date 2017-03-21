package paw.greycat.actions.vocabulary;

import greycat.Action;
import greycat.Constants;
import greycat.TaskContext;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import paw.greycat.actions.PawctionNames;
import paw.greycat.tasks.VocabularyTasks;

public class ActionRetrieveVocabularyNode implements Action {
    @Override
    public void eval(TaskContext ctx) {
        VocabularyTasks.retrieveVocabularyNode()
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
        builder.writeString(PawctionNames.RETRIEVE_VOCABULARY_NODE);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        builder.writeChar(Constants.TASK_PARAM_CLOSE);
    }
}
