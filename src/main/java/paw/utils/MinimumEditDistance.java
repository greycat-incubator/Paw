package paw.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinimumEditDistance {

    public static final long SUBSTITUTION = 1;

    public static final long INSERTION = 2;

    public static final long DELETION = 3;

    public static final long KEEP = 4;

    private final long[] _newer;
    private final long[] _older;
    private int[][] minEditDistanceMatrix;
    private long[][][] backtraceMatrix;

    public MinimumEditDistance(long[] older, long[] newer) {
        this._older = older;
        this._newer = newer;
        minEditDistanceMatrix = new int[newer.length + 1][older.length + 1];
        backtraceMatrix = new long[newer.length + 1][older.length + 1][3];
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
                long mod;

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

    public List<long[]> path() {
        int i = _newer.length;
        int j = _older.length;
        List<long[]> listAction = new ArrayList<>();
        while (backtraceMatrix[i][j][0] != 0 || backtraceMatrix[i][j][1] != 0 || backtraceMatrix[i][j][2] != 0) {
            long[] actions = backtraceMatrix[i][j];
            if (actions[2] == KEEP) {
                listAction.add(new long[]{_older[j - 1], KEEP});
                i -= 1;
                j -= 1;
            } else if (actions[2] == SUBSTITUTION) {
                listAction.add(new long[]{_older[j - 1], DELETION});
                listAction.add(new long[]{_newer[i - 1], INSERTION});
                i -= 1;
                j -= 1;
            } else if (actions[0] == INSERTION) {
                listAction.add(new long[]{_newer[i - 1], INSERTION});
                i -= 1;
            } else {//Suppression
                listAction.add(new long[]{_older[j - 1], DELETION});
                j -= 1;
            }
        }
        if (i != 0 && j != 0) throw new RuntimeException("error in edit distance");
        if (i != 0) {
            while (i != 0) {
                listAction.add(new long[]{_newer[i - 1], INSERTION});
                i -= 1;
            }
        } else {
            while (j != 0) {
                listAction.add(new long[]{_older[j - 1], DELETION});
                j -= 1;
            }
        }
        Collections.reverse(listAction);

        return listAction;
    }
}
