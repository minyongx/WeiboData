package cn.edu.zjut.myong.webdriver;

import cn.edu.zjut.myong.com.weibo.util.OSTool;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.*;
import java.util.*;

/**
 * 机器人和Webdriver的接口，一个configuration包括了一个driver，并提供了获得wait和actions的方法。
 * Cookies的操作我也修改了，直接用类保存。
 * <p>
 * 这个configuration包括了firefox和chrome，只需要你选择好浏览器以及需要额参数list就可以启动不同浏览器。
 * <p>
 * 后面operator你需要都要改成将这个configuration作为参数的，类似我弄的PageSwitcher
 */
public class WebDriverConfiguration {

    public enum Browser {
        Chrome,
        Firefox,
    }

    private RemoteWebDriver driver;
    private Browser browser;
    private List<String> arguments;
    public String homeTab;

    public WebDriverConfiguration(Browser browser, List<String> arguments) {
        this.browser = browser;
        this.arguments = arguments;
        initDriver();
    }

    private void initDriver() {
        // 检测是否有chromedirver
        File current;
        File[] chromedrivers;
        if (!OSTool.isLinux()) {
            current = new File(".\\resource\\");
            chromedrivers = current.listFiles((dir, name) -> name.startsWith("chromedriver") & name.endsWith(".exe"));
        } else {
            current = new File("./resource/");
            chromedrivers = current.listFiles((dir, name) -> name.startsWith("chromedriver") & !name.endsWith(".exe"));
        }
        String chromedriver = null;

        if (chromedrivers != null && chromedrivers.length > 0) {
            chromedriver = chromedrivers[0].getAbsolutePath();
        }

        // 检测是否有geckodriver
        File[] geckodrivers;
        if (!OSTool.isLinux()) {
            geckodrivers = current.listFiles((dir, name) -> name.startsWith("geckodriver") & name.endsWith(".exe"));
        } else {
            geckodrivers = current.listFiles((dir, name) -> name.startsWith("geckodriver") & !name.endsWith(".exe"));
        }
        String geckodriver = null;
        if (geckodrivers != null && geckodrivers.length > 0) {
            geckodriver = geckodrivers[0].getAbsolutePath();
        }

        // 构建driver
        switch (browser) {
            case Chrome:
                if (chromedriver == null)
                    return;
                System.setProperty("webdriver.chrome.driver", chromedriver);
                ChromeOptions chromeOptions = new ChromeOptions();
                // chromeOptions.setBinary("C:\\Program Files (x86)\\Google\\Chrome Beta\\Application\\chrome.exe");
                chromeOptions.addArguments(arguments);
                driver = new ChromeDriver(chromeOptions);
                driver.manage().window().maximize();
                homeTab = driver.getWindowHandle();
                break;

            case Firefox:
                if (geckodriver == null)
                    return;
                System.setProperty("webdriver.gecko.driver", geckodriver);
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments(arguments);
                driver = new FirefoxDriver(firefoxOptions);
                driver.manage().window().maximize();
                homeTab = driver.getWindowHandle();
                break;

            default:
                driver = null;
        }
    }

    public void exit() {
        if (driver != null)
            try {
                driver.quit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        homeTab = "";
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }
}
