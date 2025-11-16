// EmbeddingUtils.java
package com.sd.facultyfacialrecognition;

import java.util.ArrayList;
import java.util.List;

public class EmbeddingUtils {
    // L2-normalize embedding
    public static float[] normalize(float[] emb) {
        double sum = 0.0;
        for (float v : emb) sum += v * v;
        double norm = Math.sqrt(sum) + 1e-10;
        float[] out = new float[emb.length];
        for (int i = 0; i < emb.length; i++) out[i] = (float) (emb[i] / norm);
        return out;
    }

    // compute centroid for a list of embeddings
    public static float[] centroid(List<float[]> embs) {
        if (embs == null || embs.size() == 0) return null;
        int n = embs.get(0).length;
        float[] c = new float[n];
        for (float[] e : embs) {
            for (int i = 0; i < n; i++) c[i] += e[i];
        }
        for (int i = 0; i < n; i++) c[i] /= embs.size();
        return normalize(c);
    }

    // cosine similarity
    public static double cosine(float[] a, float[] b) {
        double dot = 0;
        double na = 0;
        double nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-10);
    }

    // merge: normalize each and store centroid per faculty before writing to json
    public static List<float[]> normalizeList(List<float[]> list) {
        List<float[]> out = new ArrayList<>();
        for (float[] e : list) out.add(normalize(e));
        return out;
    }
}
