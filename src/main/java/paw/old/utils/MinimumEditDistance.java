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
package paw.old.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinimumEditDistance {

    public static final int SUBSTITUTION = 1;

    public static final int INSERTION = 2;

    public static final int DELETION = 3;

    public static final int KEEP = 4;

    private final int[] _newer;
    private final int[] _older;
    private int[][] minEditDistanceMatrix;
    private int[][][] backtraceMatrix;

    public MinimumEditDistance(int[] older, int[] newer) {
        this._older = older;
        this._newer = newer;
        minEditDistanceMatrix = new int[newer.length + 1][older.length + 1];
        backtraceMatrix = new int[newer.length + 1][older.length + 1][3];
        computeEditDistance();
    }

    private void computeEditDistance() {
        for (int i = 0; i <= _newer.length; i++) {
            minEditDistanceMatrix[i][0] = i;
        }
        for (int j = 0; j <= _older.length; j++) {
            minEditDistanceMatrix[0][j] = j;
        }

        for (int i = 1; i <= _newer.length; i++) {
            for (int j = 1; j <= _older.length; j++) {
                int insert = minEditDistanceMatrix[i - 1][j] + 1;
                int delet = minEditDistanceMatrix[i][j - 1] + 1;
                int subsame;
                int mod;

                if (_older[j - 1] == (_newer[i - 1])) {
                    subsame = minEditDistanceMatrix[i - 1][j - 1];
                    mod = KEEP;
                } else {
                    subsame = minEditDistanceMatrix[i - 1][j - 1] + 2;
                    mod = SUBSTITUTION;
                }

                if (insert <= delet && insert <= subsame) {
                    minEditDistanceMatrix[i][j] = insert;
                    backtraceMatrix[i][j][0] = INSERTION;
                }
                if (delet <= insert && delet <= subsame) {
                    minEditDistanceMatrix[i][j] = delet;
                    backtraceMatrix[i][j][1] = DELETION;
                }
                if (subsame <= insert && subsame <= delet) {
                    minEditDistanceMatrix[i][j] = subsame;
                    backtraceMatrix[i][j][2] = mod;
                }
            }
        }
    }

    public int editDistance() {
        return minEditDistanceMatrix[_newer.length][_older.length];
    }

    public List<int[]> path() {
        int i = _newer.length;
        int j = _older.length;
        List<int[]> listAction = new ArrayList<>();
        while (backtraceMatrix[i][j][0] != 0 || backtraceMatrix[i][j][1] != 0 || backtraceMatrix[i][j][2] != 0) {
            int[] actions = backtraceMatrix[i][j];
            if (actions[2] == KEEP) {
                listAction.add(new int[]{_older[j - 1], KEEP});
                i -= 1;
                j -= 1;
            } else if (actions[2] == SUBSTITUTION) {
                listAction.add(new int[]{_older[j - 1], DELETION});
                listAction.add(new int[]{_newer[i - 1], INSERTION});
                i -= 1;
                j -= 1;
            } else if (actions[0] == INSERTION) {
                listAction.add(new int[]{_newer[i - 1], INSERTION});
                i -= 1;
            } else {//Suppression
                listAction.add(new int[]{_older[j - 1], DELETION});
                j -= 1;
            }
        }
        if (i != 0 && j != 0) throw new RuntimeException("error in edit distance");
        if (i != 0) {
            while (i != 0) {
                listAction.add(new int[]{_newer[i - 1], INSERTION});
                i -= 1;
            }
        } else {
            while (j != 0) {
                listAction.add(new int[]{_older[j - 1], DELETION});
                j -= 1;
            }
        }
        Collections.reverse(listAction);

        return listAction;
    }
}
