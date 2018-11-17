package cn.edu.zjut.myong.com.weibo.analysis;

import cn.edu.zjut.myong.com.weibo.robot.hobby.WeiboGroup;
import cn.edu.zjut.myong.com.weibo.robot.hobby.WeiboGroupNode;
import cn.edu.zjut.myong.nlp.Corpus;
import cn.edu.zjut.myong.nlp.Dictionary;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReadManualAnnotatedData {

    // 科技组预设的初始比例
    public static final double[] initPCR = new double[] {
            0.7875,
            0.6452,
            0.5000,
            0.9048,
            0.6538,
            0.6842,
            0.6000,
            0.2292,
            0.3614,
            0.8222,
            0.3333,
            1.0000,
            0.1587,
            0.5000,
            0.2619,
            0.6563,
            0.5882,
            0.6000,
            0.3030,
            0.1818,
            0.1522,
            0.8111,
            1.0000,
            0.4000,
            0.3043,
            0.3303,
            0.2903,
            0.2857,
            0.1704,
            0.7791
    };
    // 科技预设漏标率
    public static final String[] annotator = new String[] {
            "黄嵩戈",
            "刘爱竹",
            "刘子瑜",
            "王恒基",
            "周钰颖",
            "季秋雅",
            "张若楠",
            "许婷",
            "张佳叶",
            "王锦梦",
            "姚任远",
            "方晨怡",
            "陆龙忠",
            "候皓晨",
            "吴浩",
            "周建",
            "冯蓥杰",
            "韩世聪",
            "程银婷",
            "李樊",
            "王怡静",
            "高尚",
            "李昊",
            "宁熠奇",
            "程璐",
            "鲍煜杰",
            "徐俊豪",
            "张陆晨",
            "袁聪儿",
            "张蔚",
            "李哲宇",
            "郑海秋",
            "方伟",
            "张幸帆",
            "金凌剑"
    };
    public static final double[] missing = new double[] {
            0.0980,
            0.0640,
            0.0840,
            0.0420,
            0.0280,
            0.0860,
            0.0100,
            0.0280,
            -0.0780,
            0.0420,
            0.0400,
            0.0040,
            0.0560,
            0.0260,
            0.1180,
            0.0320,
            -0.0260,
            0.0320,
            0.0200,
            0.1040,
            0.0720,
            0.0300,
            0.0600,
            -0.0200,
            0.0520,
            0.0220,
            0.0500,
            0.0220,
            0.0120,
            0.0040,
            0.0980,
            0.0900,
            0.0400,
            0.0140,
            0.0120
    };

    public static final Map<String, Double> missingRatio = new HashMap<>();
    static {
        for (int i = 0; i < 30; i++) {
            missingRatio.put(annotator[i], missing[i]);
        }
    }

    public static void main(String[] args) throws Exception {
        String[] tags = new String[]{"娱乐", "科技"};


        for (int i = 1; i <= 3; i++) {
            for (int j = (i-1)*10+1; j <= (i-1)*10+10; j++) {
                for (int k = 0; k < tags.length; k++) {
                    String grpFile = "C:\\Workspace\\[A]www\\exp1\\实验原始数据BOT" + i + "\\BOT" + i + "-HobbyBotGroups.obj";
                    String datFile = "C:\\Workspace\\[A]www\\exp1\\人工分类数据结果BOT" + i + "\\BOT" + i + "-" + tags[k] + (j<10?"0":"") + j + "组.xls";
                    String grpName = tags[k] + (j<10?"0":"") + j + "组";
                    List<Datum> data = parseAnnotation(grpName, grpFile, datFile, k + 4);
                    /*
                    int[][] ratio;
                    if (k == 0) {
                        ratio = calculatePreferredContentRatio(20, data);
                    } else {
                        ratio = calculatePreferredContentRatioWithAdjust(20, data, j-1);
                    }

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(grpName + ".pcr")));
                    for (int x = 0; x < ratio.length; x++) {
                        for (int y = 0; y < ratio[x].length; y++) {
                            writer.write(ratio[x][y] + " ");
                        }
                        writer.newLine();
                    }
                    writer.close();
                    */

                    List<Term> frequency = calculateWordFrequency(data);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(grpName + ".frq")));
                    for (Term t : frequency) {
                        writer.write("" + t.frequency);
                        writer.newLine();
                    }
                    writer.close();

                }
            }
        }
    }

    public static List<Term> calculateWordFrequency(List<Datum> data) throws Exception {
        Set<String> text = new HashSet<>();
        for (Datum d : data) {
            if (d.content == null || d.content.trim().length() < 5) {
                continue;
            }
            text.add(d.content.trim());
        }

        Dictionary dict = new Dictionary(new Corpus(new ArrayList<>(text)), 0);
        List<Term> frequency = new ArrayList<>();
        for (int i = 0; i < dict.size(); i++) {
            Term term = new Term();
            term.text = dict.getTerm(i);
            term.frequency = (double) dict.termFrequency.get(i) / text.size();
            frequency.add(term);
            // System.out.println(frequency.get(dict.getTerm(i)));
        }

        Collections.sort(frequency);
        return frequency;
    }

    public static int[][] calculatePreferredContentRatio(int n, List<Datum> data) throws Exception {
        // T0比例由人工预设


        // 初始用户的初始微博比例，也就是T0时的比例
        int h = 0, initPreferred = 0, initTotal = 0;
        for (; h < data.size(); h++) {
            if (!data.get(h).isInitial) {
                break;
            } else {
                initTotal++;
                if (data.get(h).isPreferred) {
                    initPreferred++;
                }
            }
        }

        // 初始数据记录
        int[][] ratio = new int[n+1][2];
        ratio[0][0] = initPreferred;
        ratio[0][1] = initTotal;

        // 分n个steps的记录
        long start = data.get(h).time.getTime();
        long end = data.get(data.size()-1).time.getTime();
        for (int i = 1; i <= n; i++) {
            int t = 0, s = 0;
            for (Datum d : data) {
                if (d.time.getTime() > start+(i*(end-start)/n*1.0)) {
                    break;
                } else {
                    t++;
                    if (d.isPreferred) {
                        s++;
                    }
                }
            }
            System.out.println(i + ": " + s + " / " + t + " = " + ((double)s/t));
            ratio[i][0] = s;
            ratio[i][1] = t;
        }

        return ratio;
    }

    public static int[][] calculatePreferredContentRatioWithAdjust(int n, List<Datum> data, int grp) throws Exception {
        // 初始用户的初始微博比例，也就是T0时的比例
        int h = 0, initPreferred = 0, initTotal = 0;
        for (; h < data.size(); h++) {
            if (!data.get(h).isInitial) {
                break;
            } else {
                initTotal++;
            }
        }

        // 初始数据记录
        int[][] ratio = new int[n+1][2];
        ratio[0][0] = initPreferred = (int) (initTotal * initPCR[grp]);
        ratio[0][1] = initTotal;

        // 分n个steps的记录
        long start = data.get(h).time.getTime();
        long end = data.get(data.size()-1).time.getTime();
        for (int i = 1; i <= n; i++) {
            int t = 0, s = 0;
            Map<String, Integer> worker = new HashMap<>();
            for (Datum d : data) {
                if (d.time.getTime() <= start) {
                    continue;
                } else if (d.time.getTime() > start+(i*(end-start)/n*1.0)) {
                    break;
                } else {
                    t++;
                    if (d.isPreferred) {
                        s++;
                    }
                    // 检查是谁标注的
                    if (!worker.containsKey(d.annotator)) {
                        worker.put(d.annotator, 0);
                    }
                    worker.put(d.annotator, worker.get(d.annotator) + 1);
                }
            }

            ratio[i][0] = s + initPreferred;
            ratio[i][1] = t + initTotal;

            // 处理漏标
            int delta = 0;
            for (String a : worker.keySet()) {
                if (missingRatio.containsKey(a)) {
                    delta += (int) (missingRatio.get(a) * worker.get(a));
                } else {
                    System.out.println("What?");
                }

            }
            System.out.println(delta);
            ratio[i][0] = ratio[i][0] + delta;

            System.out.println(i + ": " + ratio[i][0] + " / " + ratio[i][1] + " = " + ((double)s/t));
        }

        return ratio;
    }

    public static List<Datum> parseAnnotation(String grpName, String grpFile, String datFile, int category) throws IOException, ParseException {
        // 读取组信息
        File gFile = new File(grpFile);
        List<WeiboGroup> groups;
        try {
            ObjectInputStream reader = new ObjectInputStream(new FileInputStream(gFile));
            groups = (List<WeiboGroup>) reader.readObject();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        WeiboGroup group = null;
        for (WeiboGroup grp : groups) {
            if (grp.name.equals(grpName)) {
                group = grp;
                break;
            }
        }
        if (group == null) {
            System.err.println("未找到对应组");
            System.exit(-1);
        }

        //
        List<Datum> data = new ArrayList<>();

        // 读取excel数据文件
        InputStream ipt = new FileInputStream(datFile);
        HSSFWorkbook book = new HSSFWorkbook(ipt);
        HSSFSheet sheet = book.getSheetAt(0);
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            HSSFRow row = sheet.getRow(rowNum);
            if (row == null) {
                continue;
            }
            Datum datum = new Datum();
            // 确认用户id
            HSSFCell uid = row.getCell(7);
            if (uid == null
                    || group.seekNode(uid.getStringCellValue().trim()) == null) {
                System.err.println("Missing id: " + uid);
                continue;
            }
            WeiboGroupNode node = group.seekNode(uid.getStringCellValue().trim());
            if (!node.legal) {
                continue;
            }
            datum.user = node.user.getId();
            datum.isInitial = (node.parent == null);
            // 确认时间
            HSSFCell t = row.getCell(3);
            if (t == null) {
                System.err.println("无时间戳");
                continue;
            }
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'").parse(t.getStringCellValue());
            datum.time = d;
            // 确认标注者
            HSSFCell a = row.getCell(6);
            if (a == null) {
                System.err.println("无标注者");
                continue;
            }
            datum.annotator = a.getStringCellValue().trim();
            // 确认类型
            HSSFCell isP = row.getCell(category);
            System.out.println(grpName + ": " + rowNum);
            if (isP != null &&
                    ((isP.getCellTypeEnum() == CellType.NUMERIC && isP.getNumericCellValue() == 1.0) ||
                    (isP.getCellTypeEnum() == CellType.STRING && (isP.getStringCellValue().trim().equals(",") || Integer.parseInt(isP.getStringCellValue().trim()) == 1)))) {
                datum.isPreferred = true;
            }
            // 记录内容
            HSSFCell c = row.getCell(8);
            if (c == null) {
                System.err.println("无内容");
                datum.content = "";
                continue;
            }
            datum.content = c.getStringCellValue().trim();

            data.add(datum);
        }
        book.close();

        // 数据排序
        Collections.sort(data);

        return data;
    }
}

class Datum implements Comparable {
    public Date time;
    public boolean isPreferred;
    public String annotator;
    public String user;
    public boolean isInitial;
    public String content;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Datum) {
            return this.time.equals(((Datum) obj).time);
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Object o) {
        return this.time.compareTo(((Datum) o).time);
    }
}

class Term implements Comparable {
    public String text;
    public Double frequency;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Datum) {
            return this.frequency.equals(((Term) obj).frequency);
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Object o) {
        return this.frequency.compareTo(((Term) o).frequency);
    }
}