package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.WeiboException;
import cn.edu.zjut.myong.com.weibo.util.RegexTool;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class UserPageOperator extends PageOperatorProto {

    public static final String CSS_USER_ID = "div.PCD_header div.pf_opt div.btn_bed[action-data]";
    public static final String CSS_USER_NAME = "div.PCD_header div.pf_username > h1.username";
    public static final String CSS_USER_NUMBER_FOLLOWING = "table.tb_counter td:nth-child(1) strong";
    public static final String CSS_USER_NUMBER_FOLLOWER = "table.tb_counter td:nth-child(2) strong";
    public static final String CSS_USER_NUMBER_WEIBO = "table.tb_counter td:nth-child(3) strong";
    public static final String CSS_USER_ICON_CLASS = "div.PCD_header div.pf_photo > a.icon_bed > em[title]";
    public static final String CSS_USER_FOLLOW_BUTTON = "div.PCD_header div.pf_opt div.btn_bed > a[action-type$='ollow']";
    public static final String CSS_USER_GROUP_SET = "li[action-type='setGroup'] > a";
    public static final String CSS_USER_GROUP_LAYER = ".W_layer div.layer_set_group";
    public static final String CSS_USER_GROUP_LAYER_CLOSE = "div.W_layer_close > a[node-type='close']";
    public static final String CSS_USER_GROUP_LIST = "li.group_li";
    public static final String CSS_USER_GROUP_NAME = "span.group_name > span";
    public static final String CSS_USER_GROUP_SELECT = "input.W_checkbox";
    public static final String CSS_USER_GROUP_SET_BUTTON = "a[action-type='submit']";

    public static final String URL_REGEX = "(weibo.com\\/u\\/[0-9]+\\?)|(weibo.com\\/[0-9 a-z]+\\?)";

    public static final String XPATH_LOCAL_RELATION = "//h2[starts-with(@class, 'main_title') and text()='微关系']";
    public static final String XPATH_LOCAL_FOLLOWING_COMMON = "//a[@class='S_txt1' and starts-with(text(),'共同关注')]";
    public static final String XPATH_LOCAL_FOLLOWING_SECOND = "//a[@class='S_txt1' and starts-with(text(),'我关注的人也关注他')]";
    public static final String CSS_LOCAL_FOLLOWING_COMMON_CHECK = "a.tab_item[href*='relate=same_follow']";
    public static final String CSS_LOCAL_FOLLOWING_SECOND_CHECK = "a.tab_item[href*='relate=second_follow']";

    public UserPageOperator(WebDriverConfiguration conf) {
        super(conf);
    }

    /**
     * 获得用户ID
     * @return 字符串，用户ID
     * @throws WeiboException 微博异常
     */
    public String getUserId() throws WeiboException {
        checkURL(URL_REGEX);
        try {
            WebElement info = driver.findElement(By.cssSelector(CSS_USER_ID));
            String data = info.getAttribute("action-data");
            String id = RegexTool.findFirst(data, "uid=[0-9]+");
            if (id != null) {
                id = id.substring(4);
            }
            return id;
        } catch (Exception e) {
            abnormal();
            return null;
        }
    }

    /**
     * 获得用户的屏幕名
     * @return 屏幕名
     * @throws WeiboException 微博异常
     */
    public String getUserName() throws WeiboException {
        checkURL(URL_REGEX);
        try {
            WebElement info = driver.findElement(By.cssSelector(CSS_USER_NAME));
            return info.getText();
        } catch (Exception e) {
            abnormal();
            return null;
        }
    }

    /**
     * 获得用户关注的人数
     * @return 关注的人数
     * @throws WeiboException 微博异常
     */
    public int getFollowNum() throws WeiboException {
        checkURL(URL_REGEX);
        try {
            WebElement a = driver.findElement(By.cssSelector(CSS_USER_NUMBER_FOLLOWING));
            return Integer.parseInt(a.getText());
        } catch (Exception e) {
            abnormal();
            return -1;
        }
    }

    /**
     * 获得用户的粉丝数量
     * @return 粉丝的人数
     * @throws WeiboException 微博异常
     */
    public int getFunNum() throws WeiboException {
        checkURL(URL_REGEX);
        try {
            WebElement b = driver.findElement(By.cssSelector(CSS_USER_NUMBER_FOLLOWER));
            return Integer.parseInt(b.getText());
        } catch (Exception e) {
            abnormal();
            return -1;
        }
    }

    /**
     * 用户发表的微博的数量
     * @return 微博的数量
     * @throws WeiboException 微博异常
     */
    public int getWeiboNum() throws WeiboException {
        checkURL(URL_REGEX);
        try {
            WebElement c = driver.findElement(By.cssSelector(CSS_USER_NUMBER_WEIBO));
            return Integer.parseInt(c.getText());
        } catch (Exception e) {
            abnormal();
            return -1;
        }
    }

    /**
     * 获得用户标签图标的tag，进而可通过User类中的identifyUserCategory方法判别用户类型
     * @return 图标的tag
     * @throws WeiboException 微博异常
     */
    public String getUserIcon() throws WeiboException {
        checkURL(URL_REGEX);
        try {
            WebElement icon = driver.findElement(By.cssSelector(CSS_USER_ICON_CLASS));
            return icon.getAttribute("class");
        } catch (NoSuchElementException e) {
            return "";
        } catch (Exception e) {
            abnormal();
            return "";
        }
    }

    /**
     * 是否已经关注该用户
     * @return true for 已经关注；false for 尚未关注或者无法关注
     * @throws WeiboException 微博异常
     */
    public boolean isFollowed() throws WeiboException {
        checkURL(URL_REGEX);
        try {
            WebElement btn = driver.findElement(By.cssSelector(CSS_USER_FOLLOW_BUTTON));
            String typ = btn.getAttribute("action-type");
            return !typ.equals("follow");
        } catch (Exception e) {
            abnormal();
            return false;
        }
    }

    /**
     * 关注本用户
     * @throws WeiboException 微博异常
     */
    public void follow() throws WeiboException {
        checkURL(URL_REGEX);
        try {
            WebElement btn = driver.findElement(By.cssSelector(CSS_USER_FOLLOW_BUTTON));
            new Actions(driver)
                    .moveToElement(btn)
                    .click(btn)
                    .pause(3000)
                    .build()
                    .perform();
            // 执行关注
            try {
                new WebDriverWait(driver, 30)
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div[node-type='inner'] p.S_txt1")));
                WebElement ele = driver.findElement(By.cssSelector("div[node-type='inner'] p.S_txt1"));
                if (ele.getText().trim().contains("根据对方设置，你不能进行该操作。")) {
                    throw new WeiboException(WeiboException.Error.CannotFollow, "无法关注");
                }
                if (ele.getText().trim().contains("您今天已经关注（或取消关注）太多人啦")) {
                    throw new WeiboException(WeiboException.Error.ExcessFollow, "每日关注超标");
                }
            } catch (TimeoutException toe) {
                // Do nothing
            }
            // 是否关注成功
            new WebDriverWait(driver, 30)
                    .until(ExpectedConditions.attributeToBe(
                            By.cssSelector(CSS_USER_FOLLOW_BUTTON),
                            "action-type",
                            "unFollow"));
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof WeiboException)
                throw (WeiboException) e;
            else
                abnormal();
        }
    }

    /**
     * 在关注后设置该用户所属的用户组（加入或者退出一个组）
     * @param groupName 组名（显示在微博中的）
     * @param in true for 加入一个组，false for 退出一个组
     * @throws WeiboException 微博异常
     */
    public void setGroup(String groupName, boolean in) throws WeiboException {
        checkURL(URL_REGEX);
        if (!isFollowed()) {
            return;
        }
        try {
            toGroupSettingLayer();
            // 选择分组
            List<WebElement> lis = driver.findElements(By.cssSelector(CSS_USER_GROUP_LIST));
            for (WebElement li : lis) {
                WebElement groupTitle = li.findElement(By.cssSelector(CSS_USER_GROUP_NAME));
                WebElement groupCheck = li.findElement(By.cssSelector(CSS_USER_GROUP_SELECT));
                WebElement groupSet = driver.findElement(By.cssSelector(CSS_USER_GROUP_SET_BUTTON));
                if (groupTitle.getText().equals(groupName) && (in ^ groupCheck.isSelected())) {
                    new Actions(driver)
                            .moveToElement(groupCheck)
                            .click(groupCheck)
                            .pause(3000)
                            .moveToElement(groupSet)
                            .click(groupSet)
                            .pause(3000)
                            .build()
                            .perform();
                    break;
                }
            }
            try {
                new WebDriverWait(driver, 1)
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSS_USER_GROUP_LAYER)));
                WebElement groupClose = driver.findElement(By.cssSelector(CSS_USER_GROUP_LAYER_CLOSE));
                new Actions(driver)
                        .moveToElement(groupClose)
                        .click(groupClose)
                        .pause(3000)
                        .build()
                        .perform();
            } catch (TimeoutException ignored) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
    }

    /**
     * 获得本用户所属的组
     * @return 所属组组名的列表
     * @throws WeiboException 微博异常
     */
    @SuppressWarnings("unused")
    public List<String> getGroups() throws WeiboException {
        checkURL(URL_REGEX);
        if (!isFollowed()) {
            return null;
        }
        List<String> grps = new ArrayList<>();
        try {
            toGroupSettingLayer();
            // 选择分组
            List<WebElement> lis = driver.findElements(By.cssSelector(CSS_USER_GROUP_LIST));
            for (WebElement li : lis) {
                WebElement groupTitle = li.findElement(By.cssSelector(CSS_USER_GROUP_NAME));
                WebElement groupCheck = li.findElement(By.cssSelector(CSS_USER_GROUP_SELECT));
                if (groupCheck.isSelected()) {
                    grps.add(groupTitle.getText());
                }
            }
            WebElement groupClose = driver.findElement(By.cssSelector(CSS_USER_GROUP_LAYER_CLOSE));
            new Actions(driver)
                    .moveToElement(groupClose)
                    .click(groupClose)
                    .pause(3000)
                    .build()
                    .perform();
            return grps;
        } catch (Exception e) {
            // e.printStackTrace();
            abnormal();
            return null;
        }
    }

    /**
     * 在关注关注按键后，弹出组设置面板
     * @throws WeiboException 微博异常
     */
    private void toGroupSettingLayer() throws WeiboException {
        if (!isFollowed()) {
            return;
        }
        try {
            // 弹出设置按键
            WebElement btn = driver.findElement(By.cssSelector(CSS_USER_FOLLOW_BUTTON));
            new Actions(driver)
                    .moveToElement(btn)
                    .build()
                    .perform();
            new WebDriverWait(driver, 30)
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSS_USER_GROUP_SET)));
            // 点击设置按键
            WebElement set = driver.findElement(By.cssSelector(CSS_USER_GROUP_SET));
            new Actions(driver)
                    .moveToElement(set)
                    .click(set)
                    .pause(3000)
                    .build()
                    .perform();
            new WebDriverWait(driver, 30)
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSS_USER_GROUP_LAYER)));
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
    }
}
