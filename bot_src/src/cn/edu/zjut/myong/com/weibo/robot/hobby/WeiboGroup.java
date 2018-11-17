package cn.edu.zjut.myong.com.weibo.robot.hobby;

import cn.edu.zjut.myong.com.weibo.User;
import cn.edu.zjut.myong.com.weibo.Weibo;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.util.*;

/**
 * 该类代表一个微博用户中的一个分组，并记录了组的成员树、最后更新时间以及兴趣分类等信息。
 * 并且，提供了添加新节点和删除子树等函数
 */
public class WeiboGroup implements Serializable {

    static final long serialVersionUID = 4318811283513404006L;

    @SuppressWarnings("unused")
    public static void printGroups(String wFile) throws Exception {
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream(wFile));
        List<WeiboGroup> grps = (List<WeiboGroup>) reader.readObject();
        reader.close();

        for (WeiboGroup grp : grps) {
            System.out.println("\n\n=====" + grp.name + "=====");
            for (WeiboGroupNode node : grp.nodes) {
                System.out.print(node.user.getId() + "|" + node.user.getUserName());
                if (node.legal)
                    System.out.print(" (" + true + ")");
                else
                    System.out.print(" <-" + false + "->");
                if (node.parent != null) {
                    System.out.print(" <- " + node.parent.user.getId() + "|" + node.parent.user.getUserName());
                } else {
                    System.out.print(" is root!");
                }
                System.out.println();
            }
        }
    }

    /*
    public static void hobbyGroups2WeiboGroups(String hFile, String wFile) throws Exception {
        ObjectInputStream reader = new ObjectInputStream(new FileInputStream(hFile));
        List<HobbyGroup> oldGroups = (List<HobbyGroup>) reader.readObject();
        reader.close();

        List<WeiboGroup> groups = new ArrayList<>(oldGroups.size());
        for (HobbyGroup og : oldGroups) {
            System.out.println("\n\n=================");
            WeiboGroup g = new WeiboGroup(og.getName(), og.getHobby().name(), new ArrayList<>());
            g.lastWeibo = og.getLastWeibo();
            List<String> members = og.getAllUser();
            for (String id : members) {
                HobbyGroupNode hgn = og.seekNode(id);
                WeiboGroupNode wgn = new WeiboGroupNode(hgn.user, null, hgn.weibo);
                wgn.legal = true;
                wgn.checked = hgn.checked;
                wgn.updateTime = hgn.updateTime;
                g.nodes.add(wgn);
            }
            for (String id : members) {
                HobbyGroupNode hgn = og.seekNode(id);
                if (hgn.parent != null) {
                    System.out.println("Hobby > " + hgn.user.getId() + " <- " + hgn.parent.user.getId());
                } else {
                    System.out.println("Hobby > " + hgn.user.getId() + " is root ");
                }
                WeiboGroupNode nd = g.seekNode(id);
                if (hgn.parent != null) {
                    nd.parent = g.seekNode(hgn.parent.getUser().getId());
                    System.out.println("Weibo > " + nd.user.getId() + " <- " + nd.parent.user.getId());
                } else {
                    System.out.println("Weibo > " + nd.user.getId());
                }
                System.out.println("-----------");
            }
            groups.add(g);
        }

        ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(wFile));
        writer.writeObject(groups);
        writer.flush();
        writer.close();
    }
    */

    public String name;
    public Hobby hobby;
    public Date lastUpdate;
    public Weibo lastWeibo;
    public List<WeiboGroupNode> nodes = Collections.synchronizedList(new LinkedList<>());

    public WeiboGroup(String name, String hobby, List<User> iniMembers) {
        this.name = name;
        for (User u : iniMembers) {
            WeiboGroupNode node = new WeiboGroupNode(u, null, null);
            node.checked = true;
            nodes.add(node);
        }
        this.hobby = Hobby.valueOf(hobby);
        this.lastUpdate = new Date();
        this.lastWeibo = null;
    }

    public boolean addUser(User user, String parentId, Weibo weibo) {
        WeiboGroupNode p = seekNode(parentId);
        if (p != null) {
            WeiboGroupNode n = new WeiboGroupNode(user, p, weibo);
            n.parent = p;
            n.checked = false;
            nodes.add(n);
            lastUpdate = n.updateTime;
            return true;
        } else {
            return false;
        }
    }

    public void removeUser(String id) {
        WeiboGroupNode n = seekNode(id);
        if (n == null)
            return;
        Queue<WeiboGroupNode> queue = new LinkedList<>();
        queue.add(n);
        while (!queue.isEmpty()) {
            WeiboGroupNode r = queue.poll();
            nodes.remove(r);
            System.out.println("删除 >> " + r.user.getId() + " - " + r.user.getUserName());
            for (WeiboGroupNode w : nodes) {
                if (w.parent != null && w.parent.equals(r))
                    queue.offer(w);
            }
        }
    }

    public List<String> getAllUser() {
        List<String> result = new ArrayList<>();
        for (WeiboGroupNode r : nodes) {
            result.add(r.getUser().getId());
        }
        return result;
    }

    public void checkUser(String id, boolean passed) {
        WeiboGroupNode n = seekNode(id);
        if (n == null) {
            return;
        }
        if (passed || n.checked) {
            n.checked = true;
        } else {
            removeUser(id);
        }
    }

    public WeiboGroupNode seekNode(String id) {
        User u = new User(id, "", null, null, 0, 0, 0, "");
        WeiboGroupNode node = new WeiboGroupNode(u, null, null);
        int i = nodes.indexOf(node);
        if (i == -1)
            return null;
        else
            return nodes.get(i);
    }

    public WeiboGroupNode seekNodeByName(String screenName) {
        for (WeiboGroupNode node : nodes) {
            if (node.getUser().getUserName().equals(screenName))
                return node;
        }
        return null;
    }

    public boolean isLegal(String id) {
        for (WeiboGroupNode node : nodes) {
            if (node.getUser().getId().equals(id))
                return node.legal;
        }
        return true;
    }

    public void setLegal(String id, boolean legal) {
        for (WeiboGroupNode node : nodes) {
            if (node.getUser().getId().equals(id))
                node.legal = legal;
        }
    }

    public JsonObjectBuilder toJson() {
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (WeiboGroupNode node : nodes) {
            array.add(node.toJson());
        }
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("name", name)
                .add("hobby", hobby.name())
                .add("number", nodes.size())
                .add("lastUpdate", lastUpdate.getTime())
                .add("nodes", array);
        if (lastWeibo != null)
            json.add("lastWeibo", lastWeibo.toJson());
        return json;
    }

    public JsonArrayBuilder getUncheckedNodes() {
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (WeiboGroupNode node : nodes) {
            if (!node.checked)
                array.add(node.toJson());
        }
        return array;
    }
}
