package cn.edu.zjut.myong.com.weibo;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;
import java.util.Date;

public class Weibo implements Serializable, Comparable<Weibo> {

    private static final long serialVersionUID = -8213253345094233937L;

    /**
     * 发微博的人
     */
    private User user;
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

    private Date time;

    private boolean isRepost;

    private String preUserName;

    private String sourceUserId;

    private String preWeiboId;

    private String sourceWeiboId;

    public Weibo(String id, User user, Category category, String content, String url) {
        this.id = id;
        this.user = user;
        if (category == null)
            category = Category.Normal;
        this.category = category;
        if (content == null)
            content = "";
        this.content = content;
        if (url == null)
            url = "";
        this.url = url;
    }

    public Weibo(String id, User user, Category category, String content, String url, Date time) {
        this(id, user, category, content, url);
        this.time = time;
    }

    public Weibo(String id, User user, Category category, String content, String url, Date time, boolean isRepost, String preUser, String sourceUserId, String preWeiboId, String sourceWeiboId) {
        this(id, user, category, content, url);
        this.time = time;
        this.isRepost = isRepost;
        this.preUserName = preUser;
        this.preWeiboId = preWeiboId;
        this.sourceUserId = sourceUserId;
        this.sourceWeiboId = sourceWeiboId;
    }

    public User getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Category getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public Date getTime() {
        return time;
    }

    public boolean isRepost() {
        return isRepost;
    }

    public String getPreUserName() {
        return preUserName;
    }

    public String getSourceUserId() {
        return sourceUserId;
    }

    public String getPreWeiboId() {
        return preWeiboId;
    }

    public String getSourceWeiboId() {
        return sourceWeiboId;
    }

    public JsonObjectBuilder toJson() {
        JsonObjectBuilder json = Json.createObjectBuilder().add("id", id)
                .add("category", category.toString())
                .add("content", content)
                .add("url", url);
        if (user != null)
            json.add("user", user.getId());
        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Weibo) && this.id.equals(((Weibo) obj).id);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Weibo o) {
        return this.id.compareTo(o.id);
    }

    @SuppressWarnings("unused")
    public enum Category {
        Normal,
        Recommendation,
        Advertisement
    }
}
