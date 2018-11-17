package cn.edu.zjut.myong.com.weibo.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileTool {

    public static List<String> readByLines(String file, Charset charset) {
        List<String> lines = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(file), charset));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    public static void writeByLines(String file, List<String> lines, Charset charset) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file), charset));
            if (lines != null) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveObject(String file, Object obj) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(obj);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object loadObject(String file) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
