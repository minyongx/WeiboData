package cn.edu.zjut.myong.com.weibo;

import java.io.Serializable;

public class Comment implements Serializable, Comparable {

    private static final long serialVersionUID = -6552576960532469972L;

    private String userId;

    private String userName;

    private int childCommentNumber;

    private String content;

    private boolean hasPicture;

    public Comment(String userId, String userName, String content, boolean hasPicture, int childCommentNumber) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.hasPicture = hasPicture;
        this.childCommentNumber = childCommentNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public int getChildCommentNumber() {
        return childCommentNumber;
    }

    public String getContent() {
        return content;
    }

    public boolean isHasPicture() {
        return hasPicture;
    }

    @Override
    public String toString() {
        return userId + "\t" + userName + "\t" + hasPicture + "\t" + childCommentNumber + "\t" + content;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Comment) && this.userId.equals(((Comment) obj).userId);
    }

    @Override
    public int compareTo(Object o) {
        return this.userId.compareTo(((Comment) o).userId);
    }
}
