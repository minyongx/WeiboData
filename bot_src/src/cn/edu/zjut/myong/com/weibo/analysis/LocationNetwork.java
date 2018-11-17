package cn.edu.zjut.myong.com.weibo.analysis;

import cn.edu.zjut.myong.com.weibo.robot.hobby.WeiboGroup;
import cn.edu.zjut.myong.com.weibo.robot.hobby.WeiboGroupNode;
import cn.edu.zjut.myong.com.weibo.util.FileTool;
import edu.stanford.nlp.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.*;

public class LocationNetwork {

    public static void main(String[] args) {
        export2PajekNet("bot3/HobbyBot3Groups.obj", "bot3/HobbyBot3Network.obj");
    }

    public static void export2PajekNet(String grpFile, String netFile) {
        // 读取组信息
        File gFile = new File(grpFile);
        List<WeiboGroup> groups;
        try {
            ObjectInputStream reader = new ObjectInputStream(new FileInputStream(gFile));
            groups = (List<WeiboGroup>) reader.readObject();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 读取局部网络连接信息
        Map<String, Pair<Set<String>, Set<String>>> network;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(netFile));
            network = (Map<String, Pair<Set<String>, Set<String>>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // 按组操作
        for (WeiboGroup grp : groups) {
            // 用户集合
            List<String> users = new LinkedList<>();
            for (String uid : grp.getAllUser()) {
                uid = uid.trim();
                WeiboGroupNode node = grp.seekNode(uid);
                if (node != null) {
                    if (users.contains(uid)) {
                        System.err.println("Fatal Error 1!");
                        return;
                    }
                    if (node.legal) {
                        users.add(uid);
                    }
                }
            }

            // 排序
            Collections.sort(users);
            Map<String, Integer> vertices = new HashMap<>();
            for (int i = 0; i < users.size(); i++) {
                vertices.put(users.get(i), i + 1);
            }

            // 建立Arcs
            Set<Pair<Integer, Integer>> arcs = new HashSet<>();
            for (String uid : users) {
                // TODO 加入关注树的边
                WeiboGroupNode parent = grp.seekNode(uid).parent;
                if (parent != null && parent.legal) {
                    Pair<Integer, Integer> arc = new Pair<>(vertices.get(uid), vertices.get(parent.user.getId()));
                    arcs.add(arc);
                }

                if (network.containsKey(uid)) {
                    Pair<Set<String>, Set<String>> relations = network.get(uid);

                    // 共同关注 uid关注了局部网络中的谁？
                    if (relations.first != null) {
                        for (String cid : relations.first) {
                            if (vertices.containsKey(cid)) {
                                Pair<Integer, Integer> arc = new Pair<>(vertices.get(uid), vertices.get(cid));
                                arcs.add(arc);
                            }
                        }
                    }

                    // 我关注的人也关注她 局部网路中谁关注了uid
                    if (relations.second != null) {
                        for (String cid : relations.second) {
                            if (vertices.containsKey(cid)) {
                                Pair<Integer, Integer> arc = new Pair<>(vertices.get(cid), vertices.get(uid));
                                arcs.add(arc);
                            }
                        }
                    }

                } else {
                    System.err.println("数据缺失！" + uid);
                }
            }

            // 排序
            List<Pair<Integer, Integer>> arcList = new LinkedList<>(arcs);
            Collections.sort(arcList);

            // 网络输出
            List<String> paj = new ArrayList<>();
            paj.add("*Vertices " + vertices.size());
            for (int i = 0; i < users.size(); i++) {
                paj.add((i+1) + " \"" + users.get(i) + "\"");
            }
            paj.add("*Arcs");
            for (Pair<Integer, Integer> arc : arcList) {
                paj.add(arc.first + " " + arc.second);
            }

            // 网络写入
            FileTool.writeByLines(grp.name + ".paj", paj, Charset.forName("UTF-8"));
        }
    }
}
