package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.com.weibo.User;
import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.Weibo;
import cn.edu.zjut.myong.com.weibo.util.RegexTool;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeiboRepostPageOperator extends WeiboPageOperator {

    public static final String CSS_PAGE_BLOCK = "div.W_pages";
    public static final String CSS_PAGE_NEXT = "div.repeat_list > div.list_box > div.list_ul > div.WB_cardpage.S_line1 > div > a.page.next";
    public static final String CSS_PAGE_NUMBERS = "div.repeat_list > div.list_box > div.list_ul > div.WB_cardpage.S_line1 > div > a.page[action-type]";
    public static final String CSS_PAGE_CURRENT = "div.repeat_list > div.list_box > div.list_ul > div.WB_cardpage.S_line1 > div > a.page.S_txt1.S_bg2";

    public static final String CSS_REPOST_WRAP = "div.repeat_list > div.list_box > div.list_ul > div[mid]";
    public static final String CSS_REPOST_ID = "div.WB_func > div.WB_from > a[title]";
    public static final String CSS_REPOST_USER = "div.list_con > div.WB_text > a:nth-child(1)[usercard]";
    public static final String CSS_REPOST_CONTENT = "div.list_con > div.WB_text > span";

    public static final String URL_REGEX = "weibo.com\\/[0-9]+\\/[0-9 a-z A-Z]+\\?.*type\\=repost";

    public WeiboRepostPageOperator(WebDriverConfiguration conf) {
        super(conf);
    }

    /**
     * 获得所有的转发
     * @return 转发微博的列表
     */
    public List<Weibo> getReposts() {
        checkURL(URL_REGEX);
        List<Weibo> weibos = new ArrayList<>();
        new PageOperatorAttempt() {
            private int done = 1;
            @Override
            public void run() throws Exception {
                // 是否需要多页
                boolean isMultiPage;
                try {
                    new WebDriverWait(driver, 10)
                            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSS_PAGE_BLOCK)));
                    isMultiPage = true;
                } catch (TimeoutException toe) {
                    isMultiPage = false;
                }
                // 循环多页
                while (true) {
                    int curr = -1;
                    if (isMultiPage) {
                        WebElement cp = driver.findElementByCssSelector(CSS_PAGE_CURRENT);
                        curr = Integer.parseInt(cp.getText().trim());
                    }
                    System.out.println("页面 " + curr + "/" + done);
                    if (!isMultiPage || curr == done) {
                        // 执行获取
                        List<WebElement> reposts = driver.findElementsByCssSelector(CSS_REPOST_WRAP);
                        for (WebElement repost : reposts) {
                            // 提取时间和微博id
                            WebElement from = repost.findElement(By.cssSelector(CSS_REPOST_ID));
                            String title = from.getAttribute("title");
                            Date time = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(title);
                            String href = from.getAttribute("href");
                            int s = href.lastIndexOf('/');
                            int e = (href.indexOf('?')==-1) ? href.length() : href.indexOf('?');
                            String wid = href.substring(s+1, e);
                            // 提取用户id和名字
                            WebElement user = repost.findElement(By.cssSelector(CSS_REPOST_USER));
                            String id = RegexTool.findFirstAndSubstring(
                                    user.getAttribute("usercard"),
                                    "id=[0-9]+",
                                    3);
                            String name = user.getText();
                            // 提取内容和转发关系
                            WebElement content = repost.findElement(By.cssSelector(CSS_REPOST_CONTENT));
                            String text = content.getText();
                            String preUserName = RegexTool.findFirstAndSubstring(
                                    text,
                                    "(//@)(.+)(:)",
                                    3,
                                    1);
                            // 构建对象
                            Weibo w = new Weibo(
                                    wid,
                                    new User(id, name, User.Category.Normal, User.Source.Weibo,
                                            -1, -1, -1, ""),
                                    Weibo.Category.Normal,
                                    text,
                                    "",
                                    time,
                                    true,
                                    preUserName,
                                    getUserId(),
                                    null,
                                    getWeiboId());
                            weibos.add(w);
                        }
                    }
                    // 翻页
                    if (isMultiPage) {
                        try {
                            new WebDriverWait(driver, 10)
                                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSS_PAGE_NEXT)));
                            WebElement next = driver.findElementByCssSelector(CSS_PAGE_NEXT);
                            new Actions(driver)
                                    .moveToElement(next)
                                    .sendKeys(Keys.ARROW_DOWN)
                                    .pause(300)
                                    .sendKeys(Keys.ARROW_DOWN)
                                    .pause(300)
                                    .click(next)
                                    .pause(2000)
                                    .build()
                                    .perform();
                            if (curr == done) {
                                done++;
                            }
                        } catch (TimeoutException toe) {
                            List<WebElement> pages = driver.findElementsByCssSelector(CSS_PAGE_NUMBERS);
                            if (pages.size() > 0) {
                                int maxPage = Integer.parseInt(pages.get(pages.size() - 1).getText().trim());
                                if (curr >= maxPage) {
                                    return;
                                } else {
                                    throw toe;
                                }
                            }
                        }
                    } else {
                        return;
                    }
                }
            }

            @Override
            public void update() {
                driver.navigate().refresh();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start(5);

        return weibos;
    }
}
