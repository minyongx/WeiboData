package cn.edu.zjut.myong.nlp;

import cn.edu.zjut.myong.com.weibo.util.OSTool;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;

public class FastTextClassification {
    private String tmpFilePath;
    private String command;

    /*
    public static void main(String[] args) throws Exception {
        FastTextClassification c = new FastTextClassification(
                "/home/min/fastText/build/fasttext",
                "model.ft.bin");
        TextClass[] r = c.classify("总共被引用了137次 被引用次数最多的版本是1.0 ，其被引用次数为110 ，查看引用次数柱状图 。");
        for (TextClass aR : r)
            System.out.println(aR.getTextClass() + ": " + aR.getConfidence());
    }
    */

    public FastTextClassification(String fastExe, String fastModel, String tmpFilePath) {
        this.tmpFilePath = tmpFilePath;
        command = fastExe + " predict-prob " + fastModel + " " + tmpFilePath + " 3";
    }

    public TextClass[] classify(String text) {
        // 如果在windows平台，则无fasttext，返回一个虚拟结果
        if (!OSTool.isLinux()) {
            return getDefaultTextClass();
        }

        // 分词，去停用词
        List<String> sentence = ChineseSegmenter.parse(text, ChineseSegmenter.Segmenter.Stanford);

        // 创建临时文件，供外部调用
        File file = new File(tmpFilePath);
        try {
            FileWriter fw =  new FileWriter(file);
            fw.write(StringUtils.join(sentence, " "));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 执行分类
        try {
            Process pro = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            StringBuilder msg=  new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                msg.append(line);
            }

            // 解析结果
            String[] elements = msg.toString().trim().split(" ");
            if (elements.length != 6) {
                return getDefaultTextClass();
            } else {
                TextClass[] result = new TextClass[3];
                for (int i = 0; i < result.length; i++) {
                    result[i] = new TextClass();
                    result[i].setTextClass(elements[i*2].substring(9));
                    result[i].setConfidence(Double.parseDouble(elements[i*2+1]));
                }
                return result;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return getDefaultTextClass();
    }

    private TextClass[] getDefaultTextClass() {
        TextClass[] textClass = new TextClass[1];
        textClass[0] = new TextClass();
        textClass[0].setTextClass("科技");
        textClass[0].setConfidence(0.9);
        return textClass;
    }
}