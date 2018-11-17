package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.WeiboException;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 所有的颜面切换都集中到这个类，注意，我去掉了原有的接口类，因为那是为了说明你需要提供的功能，现在已经都比较明确了，就不用了。
 * 每个页面对应于一个operator（也可以叫switcher之类的）。每个operator使用的css selector都在本类内部定义，不再单独用一个类。
 */
public class PageSwitcher extends PageOperatorProto {

    public final static String URL_HOMEPAGE = "https://weibo.com/";
    public final static String CSS_MESSAGE_BOX_CLOSE = "div.gn_topmenulist_tips a[action-type='close']";

    public final static int LOCAL_RELATION_COMMON = 0;
    public final static int LOCAL_RELATION_SECOND = 1;

    enum WeiboPageType {
        repost,
        comment,
        like
    }

    public PageSwitcher(WebDriverConfiguration conf) {
        super(conf);
    }

    /**
     * 关闭可能的消息提醒，避免遮挡页面元素
     */
    public void closeMsg() {
        // System.out.println("关闭提醒");
        try {
            new WebDriverWait(driver, 10)
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector(CSS_MESSAGE_BOX_CLOSE)));
            WebElement mClose = driver.findElement(By.cssSelector(CSS_MESSAGE_BOX_CLOSE));
            new Actions(driver)
                    .moveToElement(mClose)
                    .click(mClose)
                    .pause(300)
                    .build()
                    .perform();
        } catch (TimeoutException | NoSuchElementException e) {
            // Nothing
            // validator.abnormal();
        }
    }

    /**
     * 去向首页
     */
    public void gotoHomePage() {
        try {
            driver.get(URL_HOMEPAGE);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(conf.homeTab)) {
                    driver.switchTo().window(handle);
                    driver.close();
                }
            }
            driver.switchTo().window(conf.homeTab);
            new Actions(driver).sendKeys(Keys.HOME).pause(300).build().perform();
            new WebDriverWait(driver, 30).until(ExpectedConditions.titleContains("我的首页"));
        } catch (TimeoutException e) {
            e.printStackTrace();
            abnormal();
        }
    }

    /**
     * 在首页中，切换去向组页面
     * @param name 组名
     */
    public void gotoGroupInHomePage(String name) {
        if (!conf.getDriver().getTitle().startsWith("我的首页")) {
            gotoHomePage();
        } else {
            driver.executeScript("window.scrollTo(0,0)");
        }
        try {
            try {
                new WebDriverWait(driver, 30)
                        .until(ExpectedConditions.elementToBeClickable(By.cssSelector(HomePageOperator.CSS_HOME_GROUP_MORE)));
                WebElement more = driver.findElement(By.cssSelector(HomePageOperator.CSS_HOME_GROUP_MORE));
                new Actions(driver)
                        .moveToElement(more)
                        .click(more)
                        .pause(1000)
                        .build()
                        .perform();
            } catch (TimeoutException e) {
                // 没有more，证明组少
            }
            List<WebElement> grps = driver.findElementsByCssSelector(HomePageOperator.CSS_HOME_GROUP_LIST);
            for (WebElement grp : grps) {
                if (grp.getText().trim().contains(name)) {
                    clickGroup(grp, grp.getAttribute("gid"));
                    return;
                }
            }
            throw new WeiboException(WeiboException.Error.Other, "未找到对应的组");
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
    }

    /**
     * 去向热门微博页面
     */
    public void gotoHotPage() {
        try {
            driver.get("https://d.weibo.com/");
            new WebDriverWait(driver, 30)
                    .until(ExpectedConditions.numberOfElementsToBeMoreThan(
                            By.cssSelector(HotPageOperator.CSS_HOT_TAG_LIST),
                            40));
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
    }

    /**
     * 去向关注者页面
     */
    public void gotoFollowingPage() {
        try {
            if (!conf.getDriver().getTitle().startsWith("我的首页")) {
                gotoHomePage();
            } else {
                // moveToElement不能保证移动到所需要的位置使得元素可见，全部首先移到顶部，确保重新开始
                driver.executeScript("window.scrollTo(0,0)");
            }
            WebElement ele = driver.findElement(By.cssSelector(HomePageOperator.CSS_HOME_FOLLOWING_LINK));
            new Actions(driver)
                    .moveToElement(ele)
                    .click(ele)
                    .pause(3000)
                    .build()
                    .perform();

            new WebDriverWait(driver, 30)
                    .until(ExpectedConditions.textToBe(
                            By.cssSelector("div#plc_main div.WB_frame_c span.W_f14.S_txt1"), "全部关注"));
        } catch (Exception e) {
            abnormal();
        }
    }

    /**
     * 在关注者页面中，切换去向组页面
     * @param name 组名
     */
    public void toGroupPageInFollow(String name) {
        this.gotoFollowingPage();
        try {
            new WebDriverWait(driver, 30)
                    .until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector(FollowingPageOperator.CSS_FOLLOWING_GROUP_MORE)));
            WebElement more = driver.findElement(By.cssSelector(FollowingPageOperator.CSS_FOLLOWING_GROUP_MORE));
            new Actions(driver)
                    .moveToElement(more)
                    .click(more)
                    .pause(1000)
                    .build()
                    .perform();
            List<WebElement> grps = driver.findElements(By.cssSelector(FollowingPageOperator.CSS_FOLLOWING_GROUP_LIST));
            for (WebElement grp : grps) {
                if (grp.getText().contains(name)) {
                    Pattern ptn = Pattern.compile("gid=[0-9]+");
                    Matcher mth = ptn.matcher(grp.getAttribute("href"));
                    if (mth.find()) {
                        String tid = mth.group().substring(4);
                        clickGroup(grp, tid);
                        new WebDriverWait(driver, 30)
                                .until(ExpectedConditions.textToBe(
                                        By.cssSelector("div#plc_main div.WB_frame_c span.W_f14.S_txt1"), name));
                    } else {
                        throw new WeiboException(WeiboException.Error.Other, "未找到合法的组");
                    }
                    return;
                }
            }
            throw new WeiboException(WeiboException.Error.Other, "未找到对应的组");
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
    }

    /**
     * 点击组按键
     * @param grp 组名元素
     * @param gid 组id
     */
    private void clickGroup(WebElement grp, String gid) {
        new WebDriverWait(driver, 30)
                .until(ExpectedConditions.visibilityOfAllElements(grp));
        new Actions(driver)
                .moveToElement(grp)
                .click(grp)
                .pause(3000)
                .build()
                .perform();
        new WebDriverWait(driver, 5)
                .until(ExpectedConditions.urlContains("gid="));
        Pattern ptn = Pattern.compile("gid=[0-9]+");
        Matcher mth = ptn.matcher(driver.getCurrentUrl());
        if (mth.find()) {
            String aid = mth.group().substring(4);
            if (!gid.equals(aid)) {
                throw new WeiboException(WeiboException.Error.Other, "无法进入分组");
            }
        } else
            throw new WeiboException(WeiboException.Error.Other, "无法进入分组");
    }

    /**
     * 去向用户页面
     * @param username 用户名（屏幕名）
     */
    public void gotoUserPageByName(String username) {
        gotoUserPageByURL("https://weibo.com/n/" + username);
    }

    /**
     * 去向用户页面
     * @param userId 用户id（数字串）
     */
    public void gotoUserPageById(String userId) {
        gotoUserPageByURL("https://weibo.com/u/" + userId);
    }

    /**
     * 去向用户页面
     * @param url 用户页面地址
     */
    public void gotoUserPageByURL(String url) {
        try {
            driver.get(url);
            new WebDriverWait(driver, 30)
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".username")));
        } catch (TimeoutException e) {
            if (driver.getTitle().startsWith("我的首页")) {
                throw new WeiboException(WeiboException.Error.InvalidUser, "用户被封");
            } else {
                e.printStackTrace();
                abnormal();
            }
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
    }

    /**
     * 在用户页面中，去向“共同关注”页面
     * @param userId 用户id
     */
    public void gotoCommonFollowingInUserPage(String userId) {
        gotoLocalRelationInUserPage(userId, 0);
    }

    /**
     * 在用户页面中，去向“我关注的人也关注他”页面
     * @param userId 用户id
     */
    public void gotoSecondFollowingInUserPage(String userId) {
        gotoLocalRelationInUserPage(userId, 1);
    }

    /**
     * 在用户页面中，去向“微关系”的两个页面
     * @param userId 用户id
     * @param typ 微关系类型
     */
    private void gotoLocalRelationInUserPage(String userId, int typ) {
        String xpath, chkPoint;
        if (typ == LOCAL_RELATION_COMMON) {
            xpath = UserPageOperator.XPATH_LOCAL_FOLLOWING_COMMON;
            chkPoint = UserPageOperator.CSS_LOCAL_FOLLOWING_COMMON_CHECK;
        } else if (typ == LOCAL_RELATION_SECOND) {
            xpath = UserPageOperator.XPATH_LOCAL_FOLLOWING_SECOND;
            chkPoint = UserPageOperator.CSS_LOCAL_FOLLOWING_SECOND_CHECK;
        } else {
            return;
        }
        // 前往用户页
        try {
            gotoUserPageById(userId);
            new WebDriverWait(driver, 60)
                    .until(ExpectedConditions.elementToBeClickable(By.xpath(UserPageOperator.XPATH_LOCAL_RELATION)));
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
        // 前往关系页
        try {
            WebElement lnk = driver.findElementByXPath(xpath);
            new Actions(driver)
                    .moveToElement(lnk)
                    .pause(500)
                    .click(lnk)
                    .pause(2000)
                    .build()
                    .perform();
            new WebDriverWait(driver, 60)
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(chkPoint)));
        } catch (NoSuchElementException e) {
            throw new WeiboException(WeiboException.Error.NoElement, "没有共同关注或二次关注");
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
    }

    /**
     * 截屏
     * @param file 保存截屏的文件
     */
    public void screenshot(String file) {
        try {
            File source_file = driver.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(source_file, new File(file));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * 去向微博页面
     * @param uid 微博发布者的用户id
     * @param wid 微博的id
     * @param type 需要的weibo信息类型（转发，评论，点赞）
     */
    public void gotoWeiboPage(String uid, String wid, WeiboPageType type) {
        try {
            driver.get("https://weibo.com/" + uid + "/" + wid + "?type=" + type.name());
        } catch (Exception e) {
            abnormal();
        }
    }
}
