package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.com.weibo.Comment;
import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.util.RegexTool;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class WeiboCommentPageOperator extends WeiboPageOperator {

    public static final String CSS_MORE = "a.WB_cardmore";
    public static final String CSS_COMMENT_WRAP = "div.list_li[comment_id]";
    public static final String CSS_COMMENT_TEXT = "div.WB_text";
    public static final String CSS_COMMENT_USER = "div.WB_text > a[usercard]:nth-child(1)";
    public static final String CSS_COMMENT_CHILD = "div.list_li_v2[node-type='more_child_comment'] a[action-type='click_more_child_comment_big']";

    public static final String URL_REGEX = "weibo.com\\/[0-9]+\\/[0-9 a-z A-Z]+\\?.*type\\=comment";

    public WeiboCommentPageOperator(WebDriverConfiguration conf) {
        super(conf);
    }

    public List<Comment> getComments() {
        checkURL(URL_REGEX);
        List<Comment> comments = new ArrayList<>();
        new PageOperatorAttempt() {
            private boolean needRefresh = false;
            @Override
            public void run() {
                // 翻页展示所有评论
                // 下滚到最下方，直到出现“更多”
                int n = 0;
                while (true) {
                    List<WebElement> coms = driver.findElementsByCssSelector(CSS_COMMENT_WRAP);
                    if (coms.size() > 0) {
                        new Actions(driver)
                                .moveToElement(coms.get(coms.size()-1))
                                .pause(2000)
                                .build()
                                .perform();
                    }
                    if (coms.size() <= n) {
                        break;
                    } else {
                        n = coms.size();
                    }
                }
                // 扫描到最后
                int i = 0;
                while (true) {
                    // 是否出现下一页
                    try {
                        new WebDriverWait(driver,10)
                                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSS_MORE)));
                    } catch (TimeoutException toe) {
                        if (driver.findElementsByCssSelector(CSS_COMMENT_WRAP).size() <= 0) {
                            needRefresh = true;
                            throw toe;
                        } else {
                            break;
                        }
                    }
                    // 执行下一页
                    WebElement more = driver.findElementByCssSelector(CSS_MORE);
                    new Actions(driver)
                            .moveToElement(more)
                            .sendKeys(Keys.ARROW_DOWN)
                            .pause(300)
                            .sendKeys(Keys.ARROW_DOWN)
                            .pause(300)
                            .click(more)
                            .pause(2000)
                            .build()
                            .perform();
                    System.out.println("More " + ++i);
                }
                // 获得所有评论
                List<WebElement> cWraps = driver.findElementsByCssSelector(CSS_COMMENT_WRAP);
                for (WebElement cWrap : cWraps) {
                    String content = cWrap.findElement(By.cssSelector(CSS_COMMENT_TEXT)).getText();
                    boolean hasPicture = false;
                    if (content.contains("评论配图")) {
                        hasPicture = true;
                    }
                    WebElement user = cWrap.findElement(By.cssSelector(CSS_COMMENT_USER));
                    String id = RegexTool.findFirstAndSubstring(
                            user.getAttribute("usercard"),
                            "id=[0-9]+",
                            3);
                    String name = user.getText();
                    String child = "0";
                    List<WebElement> children = cWrap.findElements(By.cssSelector(CSS_COMMENT_CHILD));
                    if (children.size() > 0) {
                        child = RegexTool.findFirstAndSubstring(
                                children.get(0).getText(), "共[0-9]+条回复", 1, 3);
                        child = (child==null)?"0":child;
                    }
                    Comment c = new Comment(id, name, content, hasPicture, Integer.parseInt(child));
                    // System.out.println(c);
                    comments.add(c);
                }
            }

            @Override
            public void update() {
                // nothing
                if (needRefresh) {
                    driver.navigate().refresh();
                    needRefresh = false;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start(5);

        return comments;
    }
}
