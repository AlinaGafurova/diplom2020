package org.schedule.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
    private static final boolean SHOW_EACH_STEP_IN_CONSOLE = false;

    static int n = 12; // кол-во работ
    static int m = 3; // кол-во машин

    static double alpha = 2d;
    static Generator generator = new Generator(n, m);
    static int k = 0;

    static int x = 0; // параметр целевой функции
    private static final boolean IS_USE_OPTIMAL_INSERT = false;  // использовать оптимальную вставку
    static int generation_count = 40; // кол-во генерируемых задач

    private static int[] d;
    private static int[][] p;
    private static int[] w;

    // for aud
    static List<Integer> I_aud = new ArrayList<>();
    static List<Integer> p_koptej_aud = new ArrayList<>();

    // for wmdd
    static List<Integer> I_wmdd = new ArrayList<>();
    static List<Integer> p_koptej_wmdd = new ArrayList<>();

    // for wspt
    static List<Integer> I_wspt = new ArrayList<>();
    static List<Integer> p_koptej_wspt = new ArrayList<>();

    public static void main(String[] args) {
        int sum_A_wmdd = 0;
        double sum_B_wmdd = 0;
        double sum_C_wmdd = 0;

        int sum_A_aud = 0;
        double sum_B_aud = 0;
        double sum_C_aud = 0;

        int sum_A_wspt = 0;
        double sum_B_wspt = 0;
        double sum_C_wspt = 0;

        int counter = 0;

        List<String> headersList = new ArrayList<>();
        headersList.add("TF");
        headersList.add("RDD");
        headersList.add("WMDD");
        headersList.add("AU-d");
        headersList.add("WSPT-WR-d");

        List<List<String>> rowsList = new ArrayList<>();

        double rddMax = 0.9;

        for (double tf = 0.1; tf <= 0.9; tf+=0.2) {
            for (double rdd = 0.1; rdd <= rddMax; rdd+=0.2) {

                if(tf == 0.7){
                    rddMax = 0.7;
                } else if(tf > 0.7){
                    rddMax = 0.4;
                }

                int A_wmdd = 0;
                double B_wmdd = 0;
                double C_wmdd = 0;

                int A_aud = 0;
                double B_aud = 0;
                double C_aud = 0;

                int A_wspt = 0;
                double B_wspt = 0;
                double C_wspt = 0;

                for (int i = 0; i < generation_count; i++) {
                    generator.generateTFandRDD(tf, rdd);
                    d = generator.getD();
                    p = generator.getP();
                    w = generator.getW();

                    dispathingMethod();

                    double effect_wmdd = countEffect(p_koptej_wmdd);
                    double effect_aud = countEffect(p_koptej_aud);
                    double effect_wspt = countEffect(p_koptej_wspt);

                    if (effect_wmdd > effect_aud) {
                        A_wmdd++;
                        B_wmdd = Math.max(effect_wmdd, B_wmdd);
                        C_wmdd +=effect_wmdd;
                    } else {
                        if (effect_wspt > effect_aud) {
                            A_wspt++;
                            B_wspt = Math.max(effect_wspt, B_wspt);
                            C_wspt +=effect_wspt;
                        } else {
                            A_aud++;
                            B_aud = Math.max(effect_aud, B_aud);
                            C_aud +=effect_aud;
                        }
                    }
                }

                C_wmdd /= generation_count;
                C_aud /= generation_count;
                C_wspt /= generation_count;

                if (SHOW_EACH_STEP_IN_CONSOLE) {
                    System.out.println(String.format("TF: %.1f RDD: %.1f", tf, rdd));
                    System.out.println(String.format("WMDD: %s/%.2f", A_wmdd, C_wmdd));
                    System.out.println(String.format("AU-d: %s/%.2f", A_aud, C_aud));
                    System.out.println(String.format("WSPT: %s/%.2f", A_wspt, C_wspt));
                    System.out.println();
                }

                List<String> row = new ArrayList<>();
                row.add(String.format("%.1f", tf));
                row.add(String.format("%.1f", rdd));
                row.add(String.format("%s/%.2f", A_wmdd, C_wmdd));
                row.add(String.format("%s/%.2f", A_aud, C_aud));
                row.add(String.format("%s/%.2f", A_wspt, C_wspt));
                rowsList.add(row);

                sum_A_wmdd += A_wmdd;
                sum_B_wmdd = Math.max(sum_B_wmdd, B_wmdd);
                sum_C_wmdd += C_wmdd;

                sum_A_aud += A_aud;
                sum_B_aud = Math.max(sum_B_aud, B_aud);
                sum_C_aud += C_aud;

                sum_A_wspt += A_wspt;
                sum_B_wspt = Math.max(sum_B_wspt, B_wspt);
                sum_C_wspt += C_wspt;

                counter++;
            }
            rowsList.add(Arrays.asList("-","-","-","-","-"));
        }

        List<String> row = new ArrayList<>();
        row.add("");
        row.add("");
        row.add(String.format("%s/%.2f", sum_A_wmdd, sum_C_wmdd / counter));
        row.add(String.format("%s/%.2f", sum_A_aud, sum_C_aud / counter));
        row.add(String.format("%s/%.2f", sum_A_wspt, sum_C_wspt / counter));
        rowsList.add(row);

        TableGenerator tableGenerator = new TableGenerator();

        System.out.println(tableGenerator.generateTable(headersList, rowsList));
        System.out.println(IS_USE_OPTIMAL_INSERT ? "Сравнение комбинированных алгоритмов" : "Сравнение алгоритмов диспетчеризаций");
    }

    private static void dispathingMethod() {
        // Метод диспетчеризации
        // step 0
        for (int i = 0; i < n; i++) {
            I_wmdd.add(i + 1);
            I_aud.add(i + 1);
            I_wspt.add(i + 1);
        }

        while (true) {
            // step 1
            int j_wmdd = wmdd();
            int j_aud = auD();
            int j_wspt = wspt();

            // step 2
            insert(j_wmdd, j_aud, j_wspt, IS_USE_OPTIMAL_INSERT);

            I_wmdd.remove((Object)j_wmdd);
            I_aud.remove((Object)j_aud);
            I_wspt.remove((Object)j_wspt);

            k++;

            // step 3
            if (k >= n) {
                break;
            }
        }
    }

    private static void insert(int j_wmdd, int j_aud, int j_wspt, boolean useOptimalInsert) {
        if (!useOptimalInsert) {
            p_koptej_aud.add(j_aud);
            p_koptej_wmdd.add(j_wmdd);
            p_koptej_wspt.add(j_wspt);
        } else {
            p_koptej_wmdd = optimalInsertMethod(j_wmdd, p_koptej_wmdd);
            p_koptej_aud = optimalInsertMethod(j_aud, p_koptej_aud);
            p_koptej_wspt = optimalInsertMethod(j_wspt, p_koptej_wspt);
        }
    }

    // Правило оптимальной вставки
    private static List<Integer> optimalInsertMethod(int j, final List<Integer> p_kortej) {
        int i = 1;
        double M = Integer.MAX_VALUE;

        List<Integer> result_kortej = new ArrayList<>();
        while (true) {
            List<Integer> p_k = new ArrayList<>(p_kortej);
            Collections.copy(p_k, p_kortej);

            int t = p_k.isEmpty() ? 0 : (Math.min(p_k.size(), i - 1));
            p_k.add(t, j);

            double F = f(p_k);
            if (M > F) {
                result_kortej = new ArrayList<>(p_k);
                M = F;
            }

            if (i <= k) {
                i++;
            } else {
                return result_kortej;
            }
        }
    }

    private static double f(List<Integer> kortej){
        double F = 0;
        double c_i = 0;
        for (int I_item : kortej) {
            for (int j = 0; j < m; j++) {
                c_i += p[I_item - 1][j];
            }
        }

        for (int I_item : kortej) {
            double t_i = Math.max(0, c_i - d[I_item - 1]);
            F += w[I_item - 1] * t_i;
        }

        return F;
    }

    //алгоритм AU-d
    private static int auD() {
        int c = 0;

        for (int item : p_koptej_aud) {
            for (int j = 0; j < m; j++) {
                c += p[item-1][j];
            }
        }

        double sum_p = 0;
        for (int I_item : I_aud) {
            sum_p += p[I_item - 1][m-1];
        }
        double p_ = (1d/I_aud.size()) * sum_p;

        double max = -Integer.MAX_VALUE;
        int arg = 0;
        for (int I_item : I_aud) {

            int c_i = c;
            for (int j = 0; j < m; j++) {
                c_i += p[I_item-1][j];
            }

            double arg_for_exp = - (Math.max(0, d[I_item-1] - c_i) * 1d / (alpha * p_));
            double exp = Math.exp(arg_for_exp);
            double kef = w[I_item-1] * 1d / (c_i - c);
            double counted = kef * exp;
            if (counted > max) {
                max = counted;
                arg = I_item;
            }
        }

        return arg;
    }

    //алгоритм WMDD
    private static int wmdd() {
        int c = 0;

        for (int item : p_koptej_wmdd) {
            for (int j = 0; j < m; j++) {
                c += p[item-1][j];
            }
        }

        double min = Integer.MAX_VALUE;
        int arg = 0;
        for (int I_item : I_wmdd) {

            int c_i = c;
            for (int j = 0; j < m; j++) {
                c_i += p[I_item-1][j];
            }

            double from = (c_i - c) / (w[I_item - 1] * 1d);
            double to = (d[I_item - 1] - c_i + p[I_item - 1][m - 1]) / (w[I_item - 1] * 1d);

            double max = Math.max(from, to);
            if (min > max) {
                min = max;
                arg = I_item;
            }
        }

        return arg;
    }

    //алгоритм WSPT-WR-d
    private static int wspt() {

        double min = Integer.MAX_VALUE;
        int arg = 0;
        for (int I_item : I_wspt) {
            int c_i = 0;
            for (int j = 0; j < m; j++) {
                c_i += p[I_item-1][j];
            }

            double res = Math.max(0, d[I_item - 1] - c_i);
            if (res == 0) { // правило WSPT
                double sum_p = 0;
                for (int j = 0; j < m; j++) {
                    sum_p += p[I_item - 1][j];
                }

                double counted = sum_p / w[I_item - 1];
                if (min > counted) {
                    min = counted;
                    arg = I_item;
                }

            } else { //правило WR
                double counted = res / w[I_item - 1];
                if (min > counted) {
                    min = counted;
                    arg = I_item;
                }
            }


        }

        return arg;
    }

    private static double countEffect(List<Integer> p_kortej) {
        double result = 0;
        double a = 0;

        double c_i = 0;
        for (int I_item : p_kortej) {
            for (int j = 0; j < m; j++) {
                c_i += p[I_item - 1][j];
            }

            double t_i = Math.max(0, c_i - d[I_item - 1]);
            a += w[I_item - 1] * t_i;
        }


        double sum_p = 0;
        double sum_w = 0;
        for (int I_item : p_kortej) {
            for (int j = 0; j < m; j++) {
                sum_p += p[I_item - 1][j];
            }

            sum_w += w[I_item - 1];
        }

        double WP = (1d/m) * sum_w * sum_p;
        result = (a - x) / WP;

        return result;
    }
}
