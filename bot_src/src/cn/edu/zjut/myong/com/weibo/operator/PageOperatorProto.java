package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.WeiboException;
import cn.edu.zjut.myong.com.weibo.util.OSTool;
import cn.edu.zjut.myong.com.weibo.util.RegexTool;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Set;

public abstract class PageOperatorProto {

    protected RemoteWebDriver driver;
    protected WebDriverConfiguration conf;

    /**
     * 利用WebDriverConfiguration进行初始化
     * @param conf 对应的WebDriver的封装
     */
    public PageOperatorProto(WebDriverConfiguration conf) {
        this.conf = conf;
        this.driver = conf.getDriver();
    }

    /**
     * 当WebDriver运行报错时，调用该函数，分析错误内容，并抛出对应异常
     * @throws WeiboException 所有operator的public方法都只能通过该函数抛出微博异常
     */
    public void abnormal() throws WeiboException {
        // 是否是弹出了新的浏览器tab，如果是，证明是有广告
        Set<String> handles = driver.getWindowHandles();
        if (handles.size() > 1) {
            for (String h : handles) {
                if (!h.equals(conf.homeTab)) {
                    driver.switchTo().window(h);
                    driver.close();
                }
            }
            driver.switchTo().window(conf.homeTab);
            driver.navigate().refresh();
            new Actions(driver)
                    .sendKeys(Keys.HOME).pause(300)
                    .build().perform();
            throw new WeiboException(WeiboException.Error.PopUP, "弹出广告");
        }

        if (!validate()) {
            // 利用validate()函数检验是否被封了
            throw new WeiboException(WeiboException.Error.Authorization, "被封了");
        } else if (OSTool.ping("www.weibo.com") < 0.5) {
            // 利用ping检查网络问题
            throw new WeiboException(WeiboException.Error.Network, "网络问题");
        } else {
            // 其他未知问题
            throw new WeiboException(WeiboException.Error.Other, "不明错误");
        }
    }

    /**
     * 检查当前地址是否是operator对应的网页
     */
    public void checkURL(String urlRegex) {
        String url = RegexTool.findFirst("", urlRegex);
        if (url == null || url.isEmpty()) {
            throw new WeiboException(WeiboException.Error.ErrorURL, "请进入自身的关注者页面");
        }
    }

    /**
     * 检查当前账号是否被微博封号或者限制权力
     * @return true or false
     */
    private boolean validate() {
        return true;
    }
}
