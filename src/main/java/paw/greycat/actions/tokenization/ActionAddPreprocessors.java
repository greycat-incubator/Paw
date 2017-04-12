/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package paw.greycat.actions.tokenization;

import greycat.Action;
import greycat.Constants;
import greycat.TaskContext;
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
        for(int i = 0; i<_preprocessor.length; i++ ){
            builder.writeChar(Constants.TASK_PARAM_SEP);
            builder.writeString(PreprocessorType.typeName(_preprocessor[i]));
        }
        builder.writeChar(Constants.TASK_PARAM_CLOSE);
    }
}
