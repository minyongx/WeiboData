package cn.edu.zjut.myong.com.weibo.operator;

import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.WeiboException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPageOperator extends PageOperatorProto {

    public static final String CSS_LOGIN_PANEL = "div#pl_login_form div.info_header a[node-type$=normal_tab";
    public static final String CSS_LOGIN_USERNAME = "#loginname";
    public static final String CSS_LOGIN_PASSWORD = "#pl_login_form > div > div > div.info_list.password > div > input[type='password']";
    public static final String CSS_LOGIN_SUBMIT = "#pl_login_form > div > div > div.info_list.login_btn > a";

    public LoginPageOperator(WebDriverConfiguration conf) {
        super(conf);
    }

    /**
     * 登陆微博
     * @param username 用户名
     * @param password 密码
     * @throws WeiboException WeiboException.Error.Authorization无法获得微博授权，抛出授权异常
     */
    public void login(String username, String password) throws WeiboException {
        try {
            driver.get("https://weibo.com/");
            new WebDriverWait(driver, 300).until(
                    ExpectedConditions.or(
                            ExpectedConditions.titleContains("我的首页"),
                            ExpectedConditions.titleContains("微博")));

            // 是否已经登陆
            if (driver.getTitle().trim().startsWith("我的首页")) {
                System.out.println("你已经登陆了");
                return;
            }

            // 进入登陆页面，选择用用户名和密码登陆
            driver.get("https://weibo.com/");
            new WebDriverWait(driver, 30).until(ExpectedConditions.elementToBeClickable(By.cssSelector(CSS_LOGIN_PANEL)));
            driver.findElementByCssSelector(CSS_LOGIN_PANEL).click();

            // 填写用户名密码，并回车
            new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(CSS_LOGIN_USERNAME)));
            WebElement lu = driver.findElement(By.cssSelector(CSS_LOGIN_USERNAME));
            WebElement lp = driver.findElement(By.cssSelector(CSS_LOGIN_PASSWORD));
            WebElement lb = driver.findElement(By.cssSelector(CSS_LOGIN_SUBMIT));
            new Actions(driver)
                    .moveToElement(lu).pause(1000).click().sendKeys(lu, username).pause(1000)
                    .moveToElement(lp).pause(1000).click().sendKeys(lp, password).pause(1000)
                    .moveToElement(lb).pause(1000).click(lb)
                    .build().perform();

            // 有限次等待是否进入页面
            for (int i = 5; i > 0; i--) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (driver.getTitle().trim().startsWith("我的首页"))
                    return;
            }

            // 登陆失败
            throw new Exception("登陆失败");
        } catch (Exception e) {
            // e.printStackTrace();
            throw new WeiboException(WeiboException.Error.Authorization, "登陆失败");
        }
    }
}
