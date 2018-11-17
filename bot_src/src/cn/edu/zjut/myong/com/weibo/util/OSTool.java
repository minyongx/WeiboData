package cn.edu.zjut.myong.com.weibo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OSTool {

    public static boolean isLinux() {
        boolean isLinux = true;
        URL resource = Path.class.getResource("Path.class");
        String classPath = resource.getPath();
        String className = Path.class.getName().replace(".", "/") + ".class";
        String classesPath = classPath.substring(0, classPath.indexOf(className));
        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS") &&
                classesPath.startsWith("/")) {
            isLinux = false;
        }
        return isLinux;
    }

    public static double ping(String url) {
        try {
            String cmd = "ping ";
            if (isLinux())
                cmd += "-c 10 ";
            else
                cmd += "-n 10 ";

            Process process = Runtime.getRuntime().exec(cmd + url);
            InputStreamReader r = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader returnData = new BufferedReader(r);

            StringBuilder returnMsg= new StringBuilder();
            String line;
            while ((line = returnData.readLine()) != null) {
                // System.out.println(line);
                returnMsg.append(line);
            }

            Pattern ptn = Pattern.compile("[0-9]+\\.?[0-9]*%");
            Matcher mth = ptn.matcher(returnMsg.toString());

            if (mth.find()) {
                String v = mth.group();
                // System.out.println(v);
                return 1 - Double.parseDouble(v.substring(0,v.length()-1)) / 100.0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }
}
