package cn.edu.zjut.myong.com.weibo;

public class WeiboException extends RuntimeException {

    private static final long serialVersionUID = -8640601615484414441L;

    public WeiboException(Error err, String msg) {
        super("" + err.toString() + ": " + msg);
    }

    public Error getError() {
        String msg = super.getMessage();
        return Error.valueOf(msg.substring(0, msg.indexOf(":")));
    }

    @SuppressWarnings("unused")
    public enum Error {
        Timeout,
        Network,
        Authorization,
        InvalidUser,
        ErrorURL,
        CannotFollow,
        ExcessFollow,
        Other,
        PopUP,
        NoElement
    }
}
