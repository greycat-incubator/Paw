/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
