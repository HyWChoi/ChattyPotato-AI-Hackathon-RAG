// Replace package with your project's base package if needed
package gladhee.ruby.util;

import java.util.*;

/**
 * Maximal Marginal Relevance (MMR) simple implementation.
 * Assumes we have initial relevance scores and (optional) normalized cosine similarities.
 */
public final class MMR {
    private MMR() {}

    public static List<Integer> select(int k,
                                       double lambda,
                                       double[] relevance, // size N
                                       double[][] sim      // NxN, cosine sim between docs
    ) {
        int n = relevance.length;
        if (k <= 0) return List.of();
        k = Math.min(k, n);

        boolean[] selected = new boolean[n];
        List<Integer> order = new ArrayList<>();

        // pick best relevance first
        int first = argmax(relevance);
        selected[first] = true;
        order.add(first);

        while (order.size() < k) {
            double bestScore = -1e9;
            int bestIdx = -1;
            for (int i = 0; i < n; i++) {
                if (selected[i]) continue;
                double maxSimToChosen = 0.0;
                for (int j : order) {
                    if (sim != null && sim.length > i && sim[i] != null && sim[i].length > j) {
                        maxSimToChosen = Math.max(maxSimToChosen, sim[i][j]);
                    }
                }
                double score = lambda * relevance[i] - (1 - lambda) * maxSimToChosen;
                if (score > bestScore) {
                    bestScore = score;
                    bestIdx = i;
                }
            }
            if (bestIdx < 0) break;
            selected[bestIdx] = true;
            order.add(bestIdx);
        }
        return order;
    }

    private static int argmax(double[] arr) {
        int idx = 0;
        double best = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > best) {
                best = arr[i];
                idx = i;
            }
        }
        return idx;
    }
}
