package cn.edu.zjut.myong.nlp;

import java.io.*;

public class FastTextOptimizer {
     public static void main(String[] args) throws Exception {
         double[] learningRates = new double[]{0.1, 0.3, 0.5, 0.7, 0.9};
         int[] epoch = new int[]{5, 15, 25, 35, 45};
         int[] minCounts = new int[]{1, 2, 3, 4, 5};
         int[] ngrams = new int[]{1, 2, 3, 4, 5};

         BufferedWriter result = new BufferedWriter(
                 new OutputStreamWriter(
                         new FileOutputStream("result.txt"), "UTF-8"));

         for (double lr : learningRates) {
             for (int ep : epoch) {
                 for (int mc : minCounts) {
                     for (int ng : ngrams) {
                         // 训练
                         Process train = Runtime.getRuntime().exec(
                                 "../fasttext supervised -input training-sf.txt -output model-sf -lr " + lr
                                         + " -epoch " + ep + " -minCount " + mc + " -wordNgrams " + ng);
                         train.waitFor();
                         // 测试
                         System.out.print("-lr " + lr + " -epoch " + ep + " -minCount " + mc + " -wordNgrams " + ng + " -> ");
                         result.write(lr + ", " + ep + ", " + mc + ", " + ng + ", ");
                         Process test = Runtime.getRuntime().exec("../fasttext test model-sf.bin testing-sf.txt 0.3 0.7");
                         BufferedReader br = new BufferedReader(new InputStreamReader(test.getInputStream()));
                         String line;
                         while ((line = br.readLine()) != null) {
                             if (line.trim().startsWith("P@1")) {
                                 System.out.print(line.substring(3).trim() + " ");
                                 result.write(line.substring(3).trim() + ", ");
                             }
                             if (line.trim().startsWith("R@1")) {
                                 System.out.print(line.substring(3).trim());
                                 result.write(line.substring(3).trim());
                             }
                         }
                         train.destroy();
                         test.destroy();
                         System.out.println();
                         result.newLine();
                         result.flush();
                     }
                 }
             }
         }
         result.close();
     }
}
