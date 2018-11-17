package cn.edu.zjut.myong.com.weibo.robot.hobby;

import cn.edu.zjut.myong.com.weibo.User;
import cn.edu.zjut.myong.com.weibo.Weibo;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * 一个组内关注树的节点，每个节点代表一个用户，有父节点和子节点结合的信息，以及节点是否通过了人工审核。
 */
public class WeiboGroupNode implements Serializable, Comparable<WeiboGroupNode> {

    static final long serialVersionUID = 2120486758312872346L;

    public User user;
    public Weibo weibo;
    public WeiboGroupNode parent;
    public boolean checked = false;
    public boolean legal = true;
    public Date updateTime;

    public WeiboGroupNode(User user, WeiboGroupNode parent, Weibo weibo) {
        this.user = user;
        this.parent = parent;
        this.weibo = weibo;
        updateTime = Calendar.getInstance().getTime();
    }

    public User getUser() {
        return user;
    }

    public Weibo getWeibo() {
        return weibo;
    }

    public JsonObjectBuilder toJson() {
        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("user", user.toJson())
                .add("updateTime", updateTime.getTime())
                .add("legal", legal)
                .add("checked", checked);
        if (parent != null)
            json.add("parent", parent.toJson());
        if (weibo != null)
            json.add("weibo", weibo.toJson());
        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WeiboGroupNode && user.equals(((WeiboGroupNode) obj).user);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(WeiboGroupNode o) {
        return user.compareTo(o.user);
    }
}
