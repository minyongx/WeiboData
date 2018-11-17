package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.com.weibo.User;
import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.Weibo;
import cn.edu.zjut.myong.com.weibo.util.RegexTool;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomePageOperator extends PageOperatorProto {

    public static final String CSS_HOME_WEIBO_WRAP = "#v6_pl_content_homefeed > div > div.WB_feed > div.WB_cardwrap[mid]";
    public static final String CSS_HOME_WEIBO_ID = "div.WB_detail > div.WB_from > a[title]";
    public static final String CSS_HOME_WEIBO_POSTER = "div.WB_detail > div.WB_info > a";
    public static final String CSS_HOME_WEIBO_TEXT = "div.WB_detail div.WB_text";
    public static final String CSS_HOME_WEIBO_DATE = "div.WB_detail > div.WB_from > a[date]";
    public final static String CSS_HOME_GROUP_MORE = ".more";
    public final static String CSS_HOME_GROUP_LIST = ".lev[gid]";
    public final static String CSS_HOME_FOLLOWING_LINK = "#v6_pl_rightmod_myinfo > div > div > div.WB_innerwrap > ul.user_atten a:nth-child(1)";

    public static final String URL_REGEX = "weibo.com\\/u\\/[0-9]+\\/home\\?";

    /**
     * 解析一个微博wrap中的文本信息
     * @param weiboWrap 微博wrap节点
     * @return 解析获得的文本
     */
    String parseWeiboText(WebElement weiboWrap) {
        StringBuilder text = new StringBuilder();
        List<WebElement> blocks = weiboWrap.findElements(By.cssSelector(CSS_HOME_WEIBO_TEXT));
        for (WebElement block : blocks) {
            // 解析文本块中的“文本”或“元素”节点
            Object nodes = driver.executeScript(
                    "" +
                            "    var nodes = arguments[0].childNodes;\n" +
                            "    var R = new Array();\n" +
                            "    for (var i = 0, j = 0; i < nodes.length; i++) {\n" +
                            "        if (nodes[i].nodeType == 1) {\n" +
                            "            R[j++] = nodes[i];\n" +
                            "            console.log(nodes[i].tagName);\n" +
                            "        } else if (nodes[i].nodeType == 3) {\n" +
                            "            R[j++] = nodes[i].textContent;\n" +
                            "            console.log(nodes[i].textContent);\n" +
                            "        }\n" +
                            "    }\n" +
                            "    return R;", block);
            // 按照节点的性质进行过滤和处理
            if (nodes instanceof List) {
                List<Object> nodeList = (List) nodes;
                for (Object aNodeList : nodeList) {
                    if (aNodeList instanceof String) {
                        // 文本节点
                        String node = (String) aNodeList;
                        // 去掉空格内容
                        Pattern p = Pattern.compile("(\t|\r|\n|\\s|\u200b|\u00a0)+");
                        Matcher m = p.matcher(node);
                        node = m.replaceAll(" ");
                        // 若果起始为冒号，删除
                        if (node.startsWith(":")) {
                            node = node.substring(1);
                        }
                        // 如果结尾是双斜杠，删除，并记录为转发者
                        if (node.endsWith("//")) {
                            node = node.substring(0, node.length() - 2);
                        }
                        // 如果整个字符串就是...省略号，删除
                        node = node.replace("...", "");
                        // 加入
                        if (!node.trim().isEmpty()) {
                            text.append(node.trim());
                            text.append(" ");
                        }
                    } else if (aNodeList instanceof WebElement) {
                        // 图片或者连接元素节点
                        WebElement node = (WebElement) aNodeList;
                        // 判断元素的类型
                        switch (node.getTagName().toLowerCase()) {
                            case "img":
                                // 图片元素
                                if ("W_img_face".equals(node.getAttribute("class"))) {
                                    // 表情符
                                    // text.append(node.getAttribute("title"));
                                    // text.append(" ");
                                    continue;
                                }
                                break;
                            case "a":
                                // 链接元素
                                String atxt = node.getText().trim();
                                if (node.getAttribute("href").contains("http://t.cn/")) {
                                    // 短链接，现在不加入文本中
                                    // 再这个短链接内部有一个<i>的元素，可以区分短链接的类型：视频、位置等等
                                    // text.append(node.getAttribute("title"));
                                    // text.append(" ");
                                    continue;
                                }
                                if (node.getAttribute("usercard") != null && atxt.startsWith("@")) {
                                    // 用户链接
                                    text.append(atxt.substring(1));
                                    text.append(" ");
                                }
                                if ("a_topic".equals(node.getAttribute("class")) && atxt.startsWith("#") && atxt.endsWith("#")) {
                                    // 话题链接
                                    text.append(atxt, 1, atxt.length() - 1);
                                    text.append(" ");
                                }
                                if (node.getAttribute("href").contains("huati.weibo.com")) {
                                    // 超话链接
                                    text.append(atxt);
                                    text.append(" ");
                                }
                                break;
                            case "br":
                                // 换行元素
                                break;
                        }
                    }
                }
            }
        }
        // 返回结果
        return text.toString().trim();
    }

    /**
     * 获得微博wrap节点
     * @param n 本页微博序号
     * @return 微博Wrap节点元素
     */
    private WebElement getWeiboWrap(int n) {
        checkURL(URL_REGEX);
        List<WebElement> wcs = driver.findElements(By.cssSelector(CSS_HOME_WEIBO_WRAP));
        for (int i = 0, j = 0; i < wcs.size(); i++) {
            WebElement wc = wcs.get(i);
            if (wc.getAttribute("feedtype") == null || wc.getAttribute("data-mark") == null) {
                if (j++ == n) {
                    return wc;
                }
            }
        }
        return null;
    }

    public HomePageOperator(WebDriverConfiguration conf) {
        super(conf);
    }

    /**
     * 当前页面包含的微博的数量
     * @return 显示的微博的数量
     */
    public int getWeiboNumber() {
        checkURL(URL_REGEX);
        List<WebElement> wcs = driver.findElements(By.cssSelector(CSS_HOME_WEIBO_WRAP));
        int n = wcs.size();
        for (WebElement wc : wcs) {
            if (wc.getAttribute("feedtype") != null && wc.getAttribute("data-mark") != null) {
                n--;
            }
        }
        return n;
    }

    /**
     * 获得微博对象
     * @param n 本页微博序号
     * @return 微博对象
     */
    public Weibo getWeibo(int n) {
        WebElement weiboWrap = getWeiboWrap(n);
        if (weiboWrap == null)
            return null;
        new Actions(driver)
                .moveToElement(weiboWrap)
                .pause(1000)
                .build()
                .perform();
        // 微博id TODO 尚未验证修改
        WebElement idAndTime = weiboWrap.findElement(By.cssSelector(CSS_HOME_WEIBO_ID));
        String href = idAndTime.getAttribute("href");
        int s = href.lastIndexOf('/');
        int e = (href.indexOf('?')==-1) ? href.length() : href.indexOf('?');
        String identifier = href.substring(s+1, e);
        // 微博时间
        Date time = null;
        try {
            time = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(idAndTime.getAttribute("title"));
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        // 微博poster信息
        List<WebElement> posterInfo = weiboWrap.findElements(By.cssSelector(CSS_HOME_WEIBO_POSTER));
        if (posterInfo.size() < 1)
            return null;
        /* TODO 尚未验证修改
        String uid = posterInfo.get(0).getAttribute("usercard");
        Pattern ptn = Pattern.compile("id=[0-9]+");
        Matcher mth = ptn.matcher(uid);
        if (mth.find())
            uid = mth.group().substring(3);
        else
            return null;
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
        User poster = new User(uid, username, userType, User.Source.Weibo, 0, 0, 0, "");
        // 微博类型
        Weibo.Category weiboType = Weibo.Category.Normal;
        // 微博内容
        String content = parseWeiboText(weiboWrap);
        return new Weibo(identifier, poster, weiboType, content, href, time);
    }

    /**
     * 获得一个微博中提及的（包括转发路径）所有用户的用户页面的地址
     * @param n 本页微博序号
     * @return 按顺序的网页地址
     */
    public List<String> getMentionedUserUrl(int n) {
        List<String> users = new ArrayList<>();
        WebElement weiboCard = getWeiboWrap(n);
        if (weiboCard == null)
            return users;
        List<WebElement> mentioned = weiboCard.findElements(By.partialLinkText("@"));
        for (WebElement a : mentioned) {
            if (!a.getTagName().equals("a"))
                continue;
            users.add(a.getAttribute("href"));
        }
        return users;
    }

    /**
     * 获得一个微博中提及的（包括转发路径）所有用户的id或者用户名
     * @param n 本页微博序号
     * @return 按顺序的用户id或者名称，id=XXXX代表id，name=XXXX代表用户名
     */
    public List<String> getMentionedUser(int n) {
        List<String> users = new ArrayList<>();
        WebElement weiboCard = getWeiboWrap(n);
        if (weiboCard == null)
            return users;
        List<WebElement> mentioned = weiboCard.findElements(By.partialLinkText("@"));
        for (WebElement a : mentioned) {
            if (!a.getTagName().equals("a") || a.getAttribute("usercard") == null)
                continue;
            /* TODO 尚未验证修改
            Pattern ptn = Pattern.compile("(id=[0-9]+)|(name=.+)");
            Matcher mth = ptn.matcher(a.getAttribute("usercard"));
            if (!mth.find())
                continue;
            */
            users.add(RegexTool.findFirst(
                    a.getAttribute("usercard"),
                    "(id=[0-9]+)|(name=.+)"));
        }
        return users;
    }

    /**
     * 活动微博的发布日期
     * @param n 本页微博序号
     * @return 发布日期
     */
    public Date getWeiboDate(int n) {
        try {
            WebElement weiboCard = getWeiboWrap(n);
            if (weiboCard == null)
                return null;
            WebElement ed = weiboCard.findElement(By.cssSelector(CSS_HOME_WEIBO_DATE));
            long d = Long.parseLong(ed.getAttribute("date"));
            Date r = new Date();
            r.setTime(d);
            return r;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
