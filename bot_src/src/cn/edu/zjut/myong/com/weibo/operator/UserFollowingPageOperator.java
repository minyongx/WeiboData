package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class UserFollowingPageOperator extends PageOperatorProto {

    public static final String CSS_USER_FOLLOWING_WRAP = "ul.follow_list > li.follow_item[action-data]";
    public static final String CSS_USER_FOLLOWING_PAGE = "div.WB_cardpage > div.W_pages";
    public static final String CSS_USER_FOLLOWING_PAGE_CURRENT = "div.WB_cardpage > div.W_pages > a.page.S_txt1.S_bg1";
    public static final String CSS_USER_FOLLOWING_PAGE_NEXT = "div.WB_cardpage > div.W_pages > a.page.next";
    public static final String CSS_USER_FOLLOWING_PAGE_LIMITED = "div.W_layer p.S_txt1";

    public static final String URL_REGEX = "weibo.com\\/p\\/[0-9]+\\/follow";

    public UserFollowingPageOperator(WebDriverConfiguration conf) {
        super(conf);
    }

    /**
     * 获取本页面，包括后继翻页中的所有能够获的关注者信息
     * @return 所有用户的user id的集合
     */
    public Set<String> getFollows() {
        checkURL(URL_REGEX);
        // 读取用户
        Set<String> follows = new HashSet<>();
        try {
            while (true) {
                // 读取本页的用户
                List<WebElement> users = driver.findElements(By.cssSelector(CSS_USER_FOLLOWING_WRAP));
                for (WebElement u : users) {
                    String[] data = u.getAttribute("action-data").split("&");
                    for (String datum : data) {
                        if (datum.startsWith("uid=")) {
                            follows.add(datum.substring(4));
                        }
                    }
                }
                System.out.println(follows.size());
                // 有没有下一页？
                try {
                    driver.findElementByCssSelector(CSS_USER_FOLLOWING_PAGE);
                } catch (NoSuchElementException e) {
                    return follows;
                }
                // 等待出现下一页元素
                new WebDriverWait(driver, 3)
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSS_USER_FOLLOWING_PAGE_NEXT)));
                // 获得当前页码
                WebElement cp = driver.findElementByCssSelector(CSS_USER_FOLLOWING_PAGE_CURRENT);
                int c = Integer.parseInt(cp.getText());
                // 去下一页
                WebElement next = driver.findElement(By.cssSelector(CSS_USER_FOLLOWING_PAGE_NEXT));
                if (next.getAttribute("href") != null) {
                    new Actions(driver)
                            .moveToElement(next)
                            .sendKeys(Keys.ARROW_DOWN)
                            .pause(500)
                            .sendKeys(Keys.ARROW_DOWN)
                            .pause(500)
                            .moveToElement(next)
                            .click(next)
                            .build()
                            .perform();
                    // 检查是否查出限制，TODO 尚未验证修改
                    try {
                        new WebDriverWait(driver, 60)
                                .until(ExpectedConditions.textMatches(
                                        By.cssSelector(CSS_USER_FOLLOWING_PAGE_LIMITED),
                                        Pattern.compile("由于系统限制，你无法查看所有关注")));
                        break;
                    } catch (TimeoutException ignored) { }
                    // 检查是否进入下一页
                    new WebDriverWait(driver, 60)
                            .until(ExpectedConditions.textToBe(
                                    By.cssSelector(CSS_USER_FOLLOWING_PAGE_CURRENT),
                                    Integer.toString(c + 1)));
                } else {
                    // 抓取结束，返回首页
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            abnormal();
        }
        return follows;
    }
}
