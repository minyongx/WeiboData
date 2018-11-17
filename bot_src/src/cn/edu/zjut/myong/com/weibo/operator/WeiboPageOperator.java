package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.util.RegexTool;
import org.openqa.selenium.WebElement;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class WeiboPageOperator extends PageOperatorProto {

    public static final String CSS_WEIBO_POSTER = "div.WB_feed_detail > div.WB_detail > div.WB_info > a[usercard]";
    public static final String CSS_WEIBO_ID = "div.WB_detail > div.WB_from > a[title]";

    public WeiboPageOperator(WebDriverConfiguration conf) {
        super(conf);
    }

    /**
     * 获得微博的id
     * @return 微博id，可以用于访问微博，不是mid
     */
    public String getWeiboId() {
        WebElement ele = driver.findElementByCssSelector(CSS_WEIBO_ID);
        String href = ele.getAttribute("href");
        int s = href.lastIndexOf('/');
        int e = (href.indexOf('?')==-1) ? href.length() : href.indexOf('?');
        return href.substring(s+1, e);
    }

    /**
     * 微博的发布时间
     * @return 发布时间
     */
    public Date getWeiboTime() {
        WebElement ele = driver.findElementByCssSelector(CSS_WEIBO_ID);
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(ele.getAttribute("title"));
        } catch (Exception e) {
            abnormal();
            return null;
        }
    }

    /**
     * 获得微博发布者的用户id
     * @return 用户id
     */
    public String getUserId() {
        WebElement ele = driver.findElementByCssSelector(CSS_WEIBO_POSTER);
        String usercard = ele.getAttribute("usercard");
        String id = RegexTool.findFirstAndSubstring(usercard, "id=[0-9]+", 3);
        return (id==null) ? usercard : id;
    }

    /**
     * 获得用户屏幕名
     * @return 屏幕名
     */
    public String getUserName() {
        WebElement ele = driver.findElementByCssSelector(CSS_WEIBO_POSTER);
        return ele.getText();
    }
}
