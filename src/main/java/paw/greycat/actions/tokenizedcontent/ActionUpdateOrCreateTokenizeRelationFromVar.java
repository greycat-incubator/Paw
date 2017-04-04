package paw.greycat.actions.tokenizedcontent;

import greycat.Action;
import greycat.Constants;
import greycat.TaskContext;
import greycat.internal.task.TaskHelper;
import greycat.plugin.SchedulerAffinity;
import greycat.struct.Buffer;
import paw.greycat.actions.PawctionNames;
import paw.greycat.tasks.TokenizedRelationTasks;

public class ActionUpdateOrCreateTokenizeRelationFromVar implements Action {
    private String _tokenizerVar;
    private String _nodeVar;
    private String _contentVar;
    private String[] _relationNames;

    public ActionUpdateOrCreateTokenizeRelationFromVar(String p_tokenizerVar, String p_nodeVar, String p_contentVar, String... relationNames) {
        this._tokenizerVar = p_tokenizerVar;
        this._nodeVar = p_nodeVar;
        this._contentVar = p_contentVar;
        this._relationNames = relationNames;

    }

    @Override
    public void eval(TaskContext ctx) {
        TokenizedRelationTasks.updateOrCreateTokenizeRelationFromVar(_tokenizerVar, _nodeVar, _contentVar, _relationNames)
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
        builder.writeString(PawctionNames.UPDATE_OR_CREATE_TOKENIZE_RELATION_FROM_VAR);
        builder.writeChar(Constants.TASK_PARAM_OPEN);
        TaskHelper.serializeString(_tokenizerVar, builder, true);
        builder.writeChar(Constants.TASK_PARAM_SEP);
        TaskHelper.serializeString(_nodeVar, builder, true);
        builder.writeChar(Constants.TASK_PARAM_SEP);
        TaskHelper.serializeString(_contentVar, builder, true);
        builder.writeChar(Constants.TASK_PARAM_SEP);
        if (_relationNames != null && _relationNames.length > 0) {
            TaskHelper.serializeStringParams(_relationNames, builder);
        }
        builder.writeChar(Constants.TASK_PARAM_CLOSE);
    }
}
