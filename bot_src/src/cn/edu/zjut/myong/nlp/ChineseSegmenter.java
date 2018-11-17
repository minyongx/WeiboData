package cn.edu.zjut.myong.nlp;

import com.huaban.analysis.jieba.JiebaSegmenter;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChineseSegmenter {

    public enum Segmenter {
        JieBa,
        Stanford
    }
    private static JiebaSegmenter jieba;
    private static CRFClassifier<CoreLabel> stanford;
    private static Set<String> stopWords;

    private static Pattern spacing = Pattern.compile("[\t\r\n]");
    private static Pattern spacing2 = Pattern.compile("\\s{2,}");
    private static Pattern numbers = Pattern.compile("[0-9]+");

    static {
        // Jieba分词初始化
        // jieba = new JiebaSegmenter();

        // Stanford分词初始化
        String basedir = "./resource/stanford";
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", basedir);
        props.setProperty("NormalizationTable", basedir + "/norm.simp.utf8");
        props.setProperty("normTableEncoding", "UTF-8");
        props.setProperty("serDictionary", basedir + "/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");
        stanford = new CRFClassifier<>(props);
        stanford.loadClassifierNoExceptions(basedir + "/ctb.gz", props);

        // 停用词
        try {
            File file = new File("./resource/StopWords.txt");
            // 判断文件是否存在
            if (file.isFile() && file.exists()) {
                //考虑到编码格式
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(read);
                stopWords = new HashSet<>();
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    stopWords.add(lineTxt.trim());
                }
                read.close();
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String preProcess(String str) {
        // 去掉换行等
        Matcher m = spacing.matcher(str);
        str = m.replaceAll("");

        // 去掉多余空格
        Matcher m2 = spacing2.matcher(str);
        str = m2.replaceAll(" ");

        return str;
    }

    private static List<String> sufProcess(List<String> words) {
        List<String> result = new ArrayList<>();
        for (String word : words) {
            if (word.trim().isEmpty()) {
                continue;
            }
            if (stopWords.contains(word)) {
                continue;
            }
            if (!isMeaningful(word)) {
                continue;
            }
            Matcher mch = numbers.matcher(word);
            if (mch.find() && mch.group().length() == word.length())
                continue;
            result.add(word.toLowerCase());
        }
        return result;
    }

    public static List<String> parse(String str, Segmenter seg) {
        str = preProcess(str);
        if (seg == Segmenter.JieBa)
            return sufProcess(jieba.sentenceProcess(str));
        else if (seg == Segmenter.Stanford)
            return sufProcess(stanford.segmentString(str));
        else
            return new ArrayList<>();
    }

    private static boolean isMeaningful(char c) {
        Character.UnicodeScript sc = Character.UnicodeScript.of(c);
        return (c >= 'a' && c <= 'z') || sc == Character.UnicodeScript.HAN;
    }

    private static boolean isMeaningful(String str) {
        str = str.toLowerCase();
        if (str.length() == 1 && str.charAt(0) >= 'a' && str.charAt(0) <= 'z')
            return false;
        for (char c : str.toCharArray()) {
            if (isMeaningful(c)) {
                return true;
            }
        }
        return false;
    }
}
