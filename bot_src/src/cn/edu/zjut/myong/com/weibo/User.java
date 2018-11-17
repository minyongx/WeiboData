package cn.edu.zjut.myong.com.weibo;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;

/**
 * 微博用户类，添加了一个toJson方法，用户将用户信息转化为json格式与前端监控页面通讯
 */
public class User implements Serializable, Comparable<User> {

    static final long serialVersionUID = -4055909868476248689L;

    public enum Category {
        Normal,
        RedV,
        BlueV,
        YellowV,
    }

    public enum Source {
        Weibo,
        RecFriend,
        RecMobile,
        RecLocation,
        RecInterest,
        RecHot,
        None,
    }

    public static Category identifyUserCategory(String userIcons) {
        if (userIcons == null) {
            return Category.Normal;
        }
        String[] icons = userIcons.split(" ");
        for (String icon : icons) {
            switch (icon) {
                case "icon_approve_co":
                case "icon_pf_approve_co":
                    return Category.BlueV;
                case "icon_approve_gold":
                case "icon_pf_approve_gold":
                    return Category.RedV;
                case "icon_approve":
                case "icon_pf_approve":
                    return Category.YellowV;
            }
        }
        return Category.Normal;
    }

    // 用户标识符，用户在页面或者微博中定位该用户
    private String id;

    private String userName;

    // 用户类型，大V，企业用户等等
    private User.Category type;

    // 用户来源，微博提取，推荐系统等等
    private User.Source source;

    // 该用户的关注数
    private int followNum;

    // 该用户的粉丝数
    private int funNum;

    // 该用户的微博数
    private int weiboNum;

    // 该用户的地址
    private String location;

    public String getId() {
        return id;
    }

    public Category getType() {
        return type;
    }

    public String getUserName() {
        return userName;
    }

    public int getFollowNum() {
        return followNum;
    }

    public int getFunNum() {
        return funNum;
    }

    public int getWeiboNum() {
        return weiboNum;
    }

    public User(String id, String userName, Category type, Source source, int followNum, int funNum, int weiboNum, String location) {
        this.id = id;
        this.userName = userName;
        if (type == null)
            type = Category.Normal;
        this.type = type;
        if (source == null)
            source = Source.None;
        this.source = source;
        this.followNum = followNum;
        this.funNum = funNum;
        this.weiboNum = weiboNum;
        if (location == null)
            location = "";
        this.location = location;
    }

    public JsonObjectBuilder toJson() {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("userName", userName)
                .add("type", type.toString())
                .add("source", source.toString())
                .add("followNum", followNum)
                .add("funNum", funNum)
                .add("weiboNum", weiboNum)
                .add("location", location);
    }

    @Override
    public String toString() {
        return toJson().build().toString();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof User) && this.id.equals(((User) obj).id);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(User o) {
        return this.id.compareTo(o.id);
    }
}
