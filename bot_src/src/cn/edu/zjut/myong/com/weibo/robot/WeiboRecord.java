package cn.edu.zjut.myong.com.weibo.robot;

import cn.edu.zjut.myong.com.weibo.User;
import cn.edu.zjut.myong.com.weibo.Weibo;

import java.util.Date;

public class WeiboRecord {

    private String groupName;

    /**
     * 微博识别符，用于在页面中定位该微博，可以是一个id，也可以是对应的css selector
     */
    private String id;

    /**
     * 微博类型，正常微博，推荐微博，广告微博等
     */
    private Weibo.Category category;

    /**
     * 微博内容
     */
    private String content;

    /**
     * 微博地址
     */
    private String url;

    /**
     * 发布时间
     */
    private Date time;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户id号
     */
    private String userId;

    /**
     * 用户类型
     */
    private User.Category userType;

    private String textClass;

    public String getTextClass() {
        return textClass;
    }

    public void setTextClass(String textClass) {
        this.textClass = textClass;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Weibo.Category getCategory() {
        return category;
    }

    public void setCategory(Weibo.Category category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User.Category getUserType() {
        return userType;
    }

    public void setUserType(User.Category userType) {
        this.userType = userType;
    }
}