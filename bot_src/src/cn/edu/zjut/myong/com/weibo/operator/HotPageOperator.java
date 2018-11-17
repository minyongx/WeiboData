package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.com.weibo.User;
import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.Weibo;
import cn.edu.zjut.myong.com.weibo.util.RegexTool;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.util.*;

public class HotPageOperator extends PageOperatorProto {

    public static final String CSS_HOT_PAGE_TOP = "#pl_common_top > div > div > div.gn_logo > a";
    public final static String CSS_HOT_TAG_LIST = "ul.ul_text li";
    public static final String CSS_HOT_TAG_ACTIVE = "li > a.S_link1";
    public static final String CSS_HOT_MORE = "#Pl_Core_NewMixFeed__3 > div > div.WB_feed.WB_feed_v3.WB_feed_v4 > div.WB_cardwrap > a.WB_cardmore";
    public static final String CSS_HOT_RELOAD = "#Pl_Core_NewMixFeed__3 > div > div.WB_feed.WB_feed_v3.WB_feed_v4 > div.WB_cardwrap > div.WB_empty a";
    public static final String CSS_HOT_WEIBO_WRAP = "#Pl_Core_NewMixFeed__3 > div > div.WB_feed.WB_feed_v3.WB_feed_v4 > div.WB_cardwrap[mid]";
    public static final String CSS_HOT_WEIBO_ID = "div.WB_detail > div.WB_from > a[title]";
    public static final String CSS_HOT_WEIBO_POSTER = "div.WB_detail > div.WB_info > a";

    public static final String URL_REGEX = "d.weibo.com\\/";
    public static final int MAX_WEIBO = 500;


    private HomePageOperator homePageOpr;

    public HotPageOperator(WebDriverConfiguration conf) {
        super(conf);
        // 需要用里面的微博块的解析函数
        this.homePageOpr = new HomePageOperator(conf);
    }

    /**
     * 选择热门标签
     * @param key 标签名称
     */
    public void chooseTag(String key) {
        checkURL(URL_REGEX);
        // 查找标签
        List<WebElement> elements = driver.findElementsByCssSelector(CSS_HOT_TAG_LIST);
        WebElement tag = null;
        for (WebElement e : elements) {
            if (e.getText().trim().equals(key)) {
                tag = e;
                break;
            }
        }
        // 是否有正确的标签
        if (tag == null) {
            abnormal();
        } else {
            try {
                new WebDriverWait(driver, 30)
                        .until(ExpectedConditions.elementToBeClickable(tag));
            } catch (TimeoutException e) {
                abnormal();
            }
            // 进入标签
            new Actions(driver)
                    .moveToElement(tag)
                    .sendKeys(Keys.ARROW_DOWN)
                    .pause(300)
                    .click(tag)
                    .pause(2000)
                    .moveToElement(driver.findElement(By.cssSelector(CSS_HOT_PAGE_TOP)))
                    .pause(1000)
                    .build()
                    .perform();
            try {
                new WebDriverWait(driver, 30)
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSS_HOT_TAG_ACTIVE)));
            } catch (TimeoutException e) {
                abnormal();
            }
        }
    }

    /**
     * 获得热门微博
     * @return 热门微博列表
     */
    public List<Weibo> getWeibos() {
        // 读取热门微博
        List<WebElement> hws = driver.findElementsByCssSelector(CSS_HOT_WEIBO_WRAP);
        int n = hws.size();
        while (n < MAX_WEIBO) {
            // 等待
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 下滚
            new Actions(driver)
                    .moveToElement(hws.get(n - 1))
                    .build()
                    .perform();
            // TRY1 下滚获得更多
            try {
                new WebDriverWait(driver, 30)
                        .until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(CSS_HOT_WEIBO_WRAP), n));
            } catch (TimeoutException toe1) {
                // TRY2 是否出现点击加载更多
                try {
                    new WebDriverWait(driver, 30)
                            .until(ExpectedConditions.elementToBeClickable(By.cssSelector(CSS_HOT_MORE)));
                    driver.findElement(By.cssSelector(CSS_HOT_MORE)).click();
                    new WebDriverWait(driver, 30)
                            .until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(CSS_HOT_WEIBO_WRAP), n));
                } catch (TimeoutException toe2) {
                    // TRY3 是否出现重载加载更多
                    try {
                        new WebDriverWait(driver, 30)
                                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(CSS_HOT_RELOAD)));
                        driver.findElement(By.cssSelector(CSS_HOT_RELOAD)).click();
                        new WebDriverWait(driver, 30)
                                .until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(CSS_HOT_WEIBO_WRAP), n));
                    } catch (TimeoutException toe3) {
                        break;
                    }
                }
            }
            // 新的数量
            hws = driver.findElementsByCssSelector(CSS_HOT_WEIBO_WRAP);
            n = hws.size();
        }
        // 获得所有热门微博
        System.out.println("热门微博数量为：" + hws.size());
        List<Weibo> hots = new ArrayList<>();
        try {
            for (WebElement weiboCard : hws) {
                // 微博id TODO 尚未验证修改
                WebElement idAndTime = weiboCard.findElement(By.cssSelector(CSS_HOT_WEIBO_ID));
                String href = idAndTime.getAttribute("href");
                int s = href.lastIndexOf('/');
                int e = (href.indexOf('?')==-1) ? href.length() : href.indexOf('?');
                String identifier = href.substring(s+1, e);
                // 微博时间
                Date time = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(idAndTime.getAttribute("title"));
                // 微博poster信息
                List<WebElement> posterInfo = weiboCard.findElements(By.cssSelector(CSS_HOT_WEIBO_POSTER));
                if (posterInfo.size() < 1)
                    continue;
                /* TODO 尚未验证修改
                String uid = posterInfo.get(0).getAttribute("usercard");
                Pattern ptn = Pattern.compile("id=[0-9]+");
                Matcher mth = ptn.matcher(uid);
                if (mth.find())
                    uid = mth.group().substring(3);
                else
                    continue;
                */
                String uid = RegexTool.findFirstAndSubstring(
                        posterInfo.get(0).getAttribute("usercard"),
                        "id=[0-9]+",
                        3);
                String username = posterInfo.get(0).getText();
                User.Category userType = User.Category.Normal;
                for (int i = 1; i < posterInfo.size(); i++) {
                    try {
                        WebElement it = posterInfo.get(i).findElement(By.cssSelector("i[title][class]"));
                        userType = User.identifyUserCategory(it.getAttribute("class"));
                    } catch (NoSuchElementException ne) {
                        // Nothing
                    }
                }
                User poster = new User(uid, username, userType, User.Source.Weibo,
                        0, 0, 0, "");
                // 微博类型
                Weibo.Category weiboType = Weibo.Category.Normal;
                // 微博内容
                String content = homePageOpr.parseWeiboText(weiboCard);
                // 创建
                hots.add(new Weibo(identifier, poster, weiboType, content, href, time));
                System.out.println(hots.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hots;
    }
}
