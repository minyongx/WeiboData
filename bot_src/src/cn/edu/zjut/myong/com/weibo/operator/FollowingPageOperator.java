package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.util.RegexTool;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FollowingPageOperator extends PageOperatorProto {

    public static final String CSS_FOLLOWING_PAGE = "div.W_pages";
    public static final String CSS_FOLLOWING_PAGE_First = "div.W_pages > a[href*='page=1#']";
    public static final String CSS_FOLLOWING_PAGE_NEXT = "div.W_pages > a.page.next";
    public static final String CSS_FOLLOWING_WRAP = "div.member_box li.member_li div.title > a[usercard*='id=']";
    public static final String CSS_FOLLOWING_GROUP_CREATE = "a[action-type='addGroup']";
    public static final String CSS_FOLLOWING_GROUP_CREATE_NAME = "input[name='name']";
    public static final String CSS_FOLLOWING_GROUP_CREATE_BUTTON = "a[node-type='submit']";
    public static final String CSS_FOLLOWING_GROUP_CREATE_LAYER = "div.W_layer div[node-type='inner']";
    public final static String CSS_FOLLOWING_GROUP_MORE = "#plc_main div.WB_frame_b div.WB_cardwrap.S_bg2 a[action-type='expend']";
    public final static String CSS_FOLLOWING_GROUP_LIST = "li.extend li.lev3 > a";
    public static final String CSS_FOLLOWING_GROUP_CURRENT = "#plc_main > div.WB_frame_c li.tab_li > span > span";
    public static final String CSS_FOLLOWING_GROUP_DELETE = "a.edit_box[action-type='delete_group']";
    public static final String CSS_FOLLOWING_GROUP_DELETE_BUTTON = "a[action-type='ok']";

    public static final String URL_REGEX = "weibo.com\\/[0-9]+\\/follow\\?";

    private PageSwitcher switcher;

    public FollowingPageOperator(WebDriverConfiguration conf) {
        super(conf);
        switcher = new PageSwitcher(conf);
    }

    /**
     * 获得自身的所有关注者
     * @return 关注者集合
     */
    public Set<String> getFollows() {
        checkURL(URL_REGEX);
        Set<String> follows = new HashSet<>();
        try {
            while (true) {
                // 获得本页follows
                List<WebElement> cards = driver.findElements(By.cssSelector(CSS_FOLLOWING_WRAP));
                for (WebElement m : cards) {
                    /* TODO 尚未验证修改
                    String info = m.getAttribute("usercard");
                    Pattern ptn = Pattern.compile("id=[0-9]+");
                    Matcher mth = ptn.matcher(info);
                    if (mth.find()) {
                        String uid = mth.group().substring(3);
                        follows.add(uid);
                    }
                    */
                    String uid = RegexTool.findFirstAndSubstring(
                            m.getAttribute("usercard"), "id=[0-9]+", 3);
                    if (uid != null) {
                        follows.add(uid);
                    }
                }
                // 是否多页
                try {
                    driver.findElement(By.cssSelector(CSS_FOLLOWING_PAGE));
                } catch (NoSuchElementException e) {
                    // e.printStackTrace();
                    break;
                }
                // TODO 如果翻页速度慢的化，可能出问题，参考UserFollowPageOperator可以修正
                // 是否有下一页
                new WebDriverWait(driver, 3)
                        .until(ExpectedConditions.elementToBeClickable(By.cssSelector(CSS_FOLLOWING_PAGE_NEXT)));
                WebElement next = driver.findElement(By.cssSelector(CSS_FOLLOWING_PAGE_NEXT));
                if (next.getAttribute("href") == null) {
                    // 返回首页
                    WebElement first = driver.findElement(By.cssSelector(CSS_FOLLOWING_PAGE_First));
                    new Actions(driver)
                            .moveToElement(first)
                            .sendKeys(Keys.ARROW_DOWN)
                            .pause(500)
                            .sendKeys(Keys.ARROW_DOWN)
                            .pause(500)
                            .moveToElement(first)
                            .click(first)
                            .pause(3000)
                            .build()
                            .perform();
                    break;
                }
                // 去下一页
                new Actions(driver)
                        .moveToElement(next)
                        .sendKeys(Keys.ARROW_DOWN)
                        .pause(500)
                        .sendKeys(Keys.ARROW_DOWN)
                        .pause(500)
                        .moveToElement(next)
                        .click(next)
                        .pause(3000)
                        .build()
                        .perform();
            }
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
        return follows;
    }

    /**
     * 返回现有组组名
     * @return 组名列表
     */
    public List<String> getGroupNames() {
        checkURL(URL_REGEX);
        try {
            new WebDriverWait(driver, 30)
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector(CSS_FOLLOWING_GROUP_MORE)));
            WebElement more = driver.findElement(By.cssSelector(CSS_FOLLOWING_GROUP_MORE));
            new Actions(driver)
                    .moveToElement(more)
                    .click(more)
                    .build()
                    .perform();
            List<String> grpName = new ArrayList<>();
            List<WebElement> grps = driver.findElements(By.cssSelector(CSS_FOLLOWING_GROUP_LIST));
            for (WebElement grp : grps) {
                String name = grp.findElement(By.tagName("span")).getText();
                if (!name.equals("悄悄关注") && !name.equals("我的推荐") && !name.equals("服务号") && !name.equals("收起"))
                    grpName.add(name);
            }
            return grpName;
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
            return new ArrayList<>();
        }
    }

    /**
     * 创建组
     * @param name 组名
     */
    public void createGroup(String name) {
        checkURL(URL_REGEX);
        try {
            WebElement create = driver.findElement(By.cssSelector(CSS_FOLLOWING_GROUP_CREATE));
            new Actions(driver)
                    .moveToElement(create)
                    .click(create)
                    .pause(3000)
                    .build()
                    .perform();
            WebElement layer = driver.findElement(By.cssSelector(CSS_FOLLOWING_GROUP_CREATE_LAYER));
            new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOf(layer));
            WebElement grpName = layer.findElement(By.cssSelector(CSS_FOLLOWING_GROUP_CREATE_NAME));
            WebElement grpButton = layer.findElement(By.cssSelector(CSS_FOLLOWING_GROUP_CREATE_BUTTON));
            new Actions(driver)
                    .moveToElement(grpName)
                    .sendKeys(grpName, name)
                    .pause(1000)
                    .moveToElement(grpButton)
                    .click(grpButton)
                    .pause(3000)
                    .build()
                    .perform();
            WebElement grpTitle = driver.findElement(By.cssSelector(CSS_FOLLOWING_GROUP_CURRENT));
            if (!grpTitle.getText().equals(name)) {
                throw new Exception("创建失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
    }

    /**
     * 删除组
     * @param name 组名
     */
    public void deleteGroup(String name) {
        checkURL(URL_REGEX);
        switcher.toGroupPageInFollow(name);
        try {
            WebElement delete = driver.findElement(By.cssSelector(CSS_FOLLOWING_GROUP_DELETE));
            new Actions(driver)
                    .moveToElement(delete)
                    .click(delete)
                    .pause(3000)
                    .build()
                    .perform();
            new WebDriverWait(driver, 10)
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector(CSS_FOLLOWING_GROUP_DELETE_BUTTON)));
            WebElement dBtn = driver.findElement(By.cssSelector(CSS_FOLLOWING_GROUP_DELETE_BUTTON));
            new Actions(driver)
                    .moveToElement(dBtn)
                    .click(dBtn)
                    .pause(3000)
                    .build()
                    .perform();
            WebElement grpTitle = driver.findElement(By.cssSelector(CSS_FOLLOWING_GROUP_CURRENT));
            if (grpTitle.getText().equals(name)) {
                throw new Exception("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
    }

    /**
     * 删除所有组
     */
    @SuppressWarnings("unused")
    public void deleteAllGroup() {
        List<String> groupNames = this.getGroupNames();
        for (String name : groupNames) {
            System.out.println("删除组：" + name);
            deleteGroup(name);
        }
    }
}
