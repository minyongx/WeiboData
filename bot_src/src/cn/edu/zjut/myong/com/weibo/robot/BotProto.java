package cn.edu.zjut.myong.com.weibo.robot;

import cn.edu.zjut.myong.webdriver.WebDriverConfiguration;
import cn.edu.zjut.myong.com.weibo.WeiboException;
import cn.edu.zjut.myong.com.weibo.operator.LoginPageOperator;
import org.apache.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public abstract class BotProto implements Runnable {

    String username;
    String password;
    int interval;
    boolean headless;
    String chromeDir;

    String storageHost;
    int storagePort;
    String storageDB;
    String storageTable;

    Logger log = Logger.getLogger(BotProto.class.getName());
    WeiboRecordReservoir reservoir;
    WebDriverConfiguration conf;

    public BotProto(JsonObject parameters) {
        this.username = parameters.getString("username");
        this.password = parameters.getString("password");
        this.interval = parameters.getInt("interval");
        this.headless = parameters.getBoolean("headless");
        this.chromeDir = parameters.getString("chromeDir");

        this.storageHost = parameters.getString("storageHost");
        this.storagePort = parameters.getInt("storagePort");
        this.storageDB = parameters.getString("storageDB");
        this.storageTable = parameters.getString("storageTable");

        log.info("> INIT1: 数据库初始化");
        initDatabase();

        log.info("> INIT2: 浏览引擎初始化");
        initDriver();

        log.info("> INIT3: 微博登录");
        initLogin();
    }

    void initDatabase() {
        try {
            reservoir = new WeiboRecordReservoir(storageHost, storagePort, storageDB, storageTable);
        } catch (Exception e) {
            log.error(e);
            log.fatal("数据库初始化失败");
            System.exit(-1);
        }
    }

    void initDriver() {
        try {
            List<String> arguments = new ArrayList<>();
            arguments.add("user-data-dir=" + chromeDir);
            arguments.add("start-maximized"); // open Browser in maximized mode
            arguments.add("disable-gpu"); // applicable to windows os only
            if (headless)
                arguments.add("headless");
            if (conf != null)
                conf.exit();
            conf = new WebDriverConfiguration(WebDriverConfiguration.Browser.Chrome, arguments);
            RemoteWebDriver driver = conf.getDriver();
            driver.manage().window().setSize(new Dimension(2560,2560));
        } catch (Exception e) {
            log.error(e);
            log.fatal("浏览引擎初始化失败");
            System.exit(-1);
        }

    }

    void initLogin() {
        try {
            LoginPageOperator loginOpr = new LoginPageOperator(conf);
            loginOpr.login(username, password);
        } catch (WeiboException e) {
            log.error(e);
            log.info("微博登录失败");
            System.exit(-1);
        }
    }
}
