package org.schedule.algorithm;

import java.util.Arrays;
import javafx.util.Pair;

public class Generator {
    private final int n;
    private final int m;
    private final double delta = 0.1;

    private int[] w;
    private int[] dd;
    private int[][] p;

    public Generator(int n, int m) {
        this.n = n;
        this.m = m;
        w = new int[n];
        dd = new int[n];
        p = new int[n][m];
    }

    public Pair<Double, Double> generateTFandRDD() {
        // step 1
        double tfGen = Math.random();
        double tmp = 2.3 - 2 * tfGen;
        double to = Math.min(1, tmp);
        double rddGen = Math.random() * (to - delta);

        return generateTFandRDD(tfGen, rddGen);
    }

    public Pair<Double, Double> generateTFandRDD(double tfGen, double rddGen) {

        while (true) {
            // step 2
            for (int i = 0; i < w.length; i++) {
                w[i] = 1 + (int) Math.round(Math.random() * 9);
            }
            // step 3
            double p_sum = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    p[i][j] = 1 + (int)(Math.random() * 99);
                    p_sum += p[i][j];
                }
            }
            double q = p_sum * (m - 1) / (n * m);
            // step 4
            int[] d = new int[n];
            double from = q + p_sum / m * (1 - tfGen - rddGen / 2);
            double to2 = q + p_sum / m * (1 - tfGen + rddGen / 2);
            for (int i = 0; i < n; i++) {
                d[i] = (int) Math.round(from + Math.random() * (to2 - from));
            }
            // step 5
            for (int i = 0; i < n; i++) {
                int p_sum2 = 0;
                for (int j = 0; j < m; j++) {
                    p_sum2 += p[i][j];
                }
                dd[i] = Math.max(d[i], p_sum2);
            }
            double dd_sum = 0; // need for step 6
            double ddMax = dd[0]; // need for step 6
            double ddMin = dd[0]; // need for step 6
            for (int i = 0; i < n; i++) {
                dd_sum += dd[i];

                ddMin = Math.min(dd[i], ddMin);
                ddMax = Math.max(dd[i], ddMax);
            }
            // step 6
            double tfPr = 1 - (((dd_sum / n) - ((m - 1d) / (n * m)) * p_sum) / (p_sum * 1d / m));
            double rddPr = (ddMax - ddMin) / (p_sum * 1d / m);
//            System.out.println(String.format("TFgen: %s; RDDgen: %s", tfGen, rddGen));
//            System.out.println(String.format("TFpr: %s; RDDpr: %s", tfPr, rddPr));
//            System.out.println();
            if ((tfPr >= tfGen - delta && tfPr <= tfGen + delta) && (rddPr >= rddGen - delta && rddPr <= rddGen + delta)) {
                return new Pair<Double, Double>(tfPr, rddPr);
            }
        }
    }

    public int[] getW() {
        return Arrays.copyOf(w, w.length);
    }
    
    public int[] getD() {
        return Arrays.copyOf(dd, dd.length);
    }

    public int[][] getP() {
        return Arrays.copyOf(p, p.length);
    }
}
