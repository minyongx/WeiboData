package cn.edu.zjut.myong.com.weibo.robot;

import cn.edu.zjut.myong.com.weibo.robot.hobby.HobbyBotEmailApproval;
import cn.edu.zjut.myong.com.weibo.robot.hobby.HobbyBotWebApproval;
import cn.edu.zjut.myong.com.weibo.robot.hobby.WeiboGroupNode;
import edu.stanford.nlp.util.Pair;
import org.apache.log4j.PropertyConfigurator;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BotTrigger {
    @SuppressWarnings("ConstantConditions")
    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "-h":
                case "--help": {
                    System.out.println("-hobby <config file>");
                    System.out.println("-recovery <config file> [starting group index]");
                    System.out.println("-relation <config file> [network file] [starting group index] [need to check]");
                    System.out.println("-relation-append <config file> <network file> [user 1] [user 2] ...");
                    System.out.println("-hot <config file>");
                    System.out.println("参数必须按顺序，可少不可多！");
                    break;
                }
            }
            return;
        }

        if (args.length < 2) {
            System.out.println("Please tell me the name of bot and configuration file!");
            return;
        }

        JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(args[1])));
        JsonObject parameters = jsonReader.readObject();

        PropertyConfigurator.configure(parameters.getString("logConf"));

        switch (args[0].toLowerCase()) {
            case "-test": {
                HobbyBot tBot = new HobbyBot(parameters);
                tBot.collectRelations("1120663884");
                break;
            }

            case "-hobby": {
                HobbyBot hBot = new HobbyBot(parameters);
                new Thread(hBot).start();
                new Thread(new HobbyBotWebApproval(hBot)).start();
                new Thread(new HobbyBotEmailApproval(hBot)).start();
                break;
            }

            case "-recovery": {
                HobbyBot rBot = new HobbyBot(parameters);
                int s = 0;
                if (args.length > 2) {
                    s = Integer.parseInt(args[2]);
                }
                for (int i = s; i < rBot.getGrpN(); i++) {
                    try {
                        rBot.checkUsers(i, true);
                    } catch (Exception e) {
                        rBot.log.info("出现异常，继续该组");
                        rBot.initDriver();
                        rBot.initOperators();
                        i--;
                    } finally {
                        rBot.log.info("休息一下");
                        Thread.sleep(5 * 60 * 1000);
                    }
                }
                break;
            }

            case "-relation": {
                // 储存已获得的局部网络
                int g = 0;
                String netFile = parameters.getString("tempDir") + "/network.obj";
                Map<String, Pair<Set<String>, Set<String>>> network = new HashMap<>();
                boolean needCheck = false;

                // 处理输入参数
                if (args.length > 2) {
                    // 读入已经储存的局部网络
                    netFile = args[2];
                    if (new File(netFile).exists()) {
                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(netFile));
                        network = (Map<String, Pair<Set<String>, Set<String>>>) ois.readObject();
                    }
                    if (args.length > 3) {
                        g = Integer.parseInt(args[3]);
                    }
                    if (args.length > 4) {
                        needCheck = Boolean.parseBoolean(args[4]);
                    }
                }

                // 获取局部网络
                HobbyBot fBot = new HobbyBot(parameters);
                try {
                    // 对所有组做统一
                    if (needCheck) {
                        for (int i = 0; i < fBot.getGrpN(); i++) {
                            fBot.checkUsers(i, false);
                        }
                        fBot.log.info("> 完成组统一");
                    }
                } catch (Exception e) {
                    fBot.log.error("> 未能完成组统一");
                    return;
                }

                // 对所有组做获取
                for (int i = g; i < fBot.getGrpN(); i++) {
                    fBot.log.info("> 组" + i);
                    List<WeiboGroupNode> members = fBot.getGrpMembers(i);
                    int errorCount = 0;
                    for (int j = 0; j < members.size(); j++) {
                        String mid = members.get(j).user.getId();
                        // 非法用户
                        if (!members.get(j).legal) {
                            fBot.log.info(">> 非法 " + mid);
                            continue;
                        }
                        // 如果已经获得过这个用户，则可跳过
                        if (network.containsKey(mid)) {
                            fBot.log.info(">> 已获得 " + mid);
                            continue;
                        }
                        // 获取用户mid的局部网络
                        Pair<Set<String>, Set<String>> lr = fBot.collectRelations(mid);
                        if (lr == null) {
                            fBot.log.error("> 用户 " + mid + " 错误");
                            errorCount++;
                            if (errorCount >= 3) {
                                fBot.log.error("> 用户 " + mid + " 连续错误多次，跳过");
                                fBot.initDriver();
                                fBot.initOperators();
                            } else {
                                j--;
                            }
                        } else {
                            network.put(mid, lr);
                            // 保存现有进度，首先复制一下当前文件为同名.bak，而后写入.obj，这样无论市.bak出错还是.obj时出错，都有一个备份.
                            if (new File(netFile).exists()) {
                                Files.copy(Paths.get(netFile), Paths.get(netFile + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                            }
                            //
                            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(netFile));
                            oos.writeObject(network);
                            oos.flush();
                            oos.close();
                            // 错误重置
                            errorCount = 0;
                        }
                    }
                }
                break;
            }

            case "-relation-append" : {
                Map<String, Pair<Set<String>, Set<String>>> network = new HashMap<>();
                HobbyBot aBot = new HobbyBot(parameters);

                String netFile = args[2];
                if (new File(netFile).exists()) {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(netFile));
                    network = (Map<String, Pair<Set<String>, Set<String>>>) ois.readObject();
                } else {
                    System.err.println("请提供待补充的网络文件");
                    System.exit(-1);
                }

                int errorCount = 0;
                for (int i = 3; i < args.length; i++) {
                    // 获取用户mid的局部网络
                    if (network.containsKey(args[i])) {
                        System.out.println("已存在" + args[i] + "，跳过！");
                        continue;
                    }
                    Pair<Set<String>, Set<String>> lr = aBot.collectRelations(args[i]);
                    if (lr == null) {
                        aBot.log.error("> 用户 " + args[i] + " 错误");
                        errorCount++;
                        if (errorCount >= 5) {
                            aBot.log.error("> 用户 " + args[i] + " 连续错误多次，跳过");
                            aBot.initDriver();
                            aBot.initOperators();
                        } else {
                            i--;
                        }
                    } else {
                        network.put(args[i], lr);
                        // 保存现有进度，首先复制一下当前文件为同名.bak，而后写入.obj，这样无论市.bak出错还是.obj时出错，都有一个备份.
                        if (new File(netFile).exists()) {
                            Files.copy(Paths.get(netFile), Paths.get(netFile + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                        }
                        //
                        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(netFile));
                        oos.writeObject(network);
                        oos.flush();
                        oos.close();
                        // 错误重置
                        errorCount = 0;
                    }
                }

                break;
            }

            case "-hot": {
                HotWeiboBot hwBot = new HotWeiboBot(parameters);
                new Thread(hwBot).start();
                break;
            }
        }
    }
}
