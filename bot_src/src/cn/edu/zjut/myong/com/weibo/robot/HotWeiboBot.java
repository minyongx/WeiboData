package cn.edu.zjut.myong.com.weibo.robot;

import cn.edu.zjut.myong.com.weibo.*;
import cn.edu.zjut.myong.com.weibo.operator.*;
import org.apache.log4j.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HotWeiboBot extends BotProto {
    /* 配置文件模板，加入conf/WeiboBot.json
    "tags": [
        "科技",
        "科普",
        "数码",
        "明星",
        "综艺",
        "电视剧",
        "电影",
        "音乐"
    ]
    */

    // 启动主类
    /*
    public static void main(String[] args) throws Exception {
        JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream("./conf/HotWeiboBot.json")));
        JsonObject parameters = jsonReader.readObject();
        HotWeiboBot bot = new HotWeiboBot(parameters);
        new Thread(bot).start();
    }
    */

    /*
    public final static String[] Categories = new String[]{
            "社会", "国际", "科技",
            "科普", "数码", "财经",
            "股市", "明星", "综艺",
            "电视剧", "电影", "音乐",
            "汽车", "体育", "运动健身",
            "健康", "瘦身", "养生",
            "军事", "历史", "美女模特",
            "美图", "情感", "搞笑",
            "辟谣", "正能量", "政务",
            "游戏", "旅游", "育儿",
            "校园", "美食", "房产",
            "家居", "星座", "读书",
            "三农", "设计", "艺术",
            "时尚", "美妆", "动漫",
            "宗教", "萌宠", "婚庆",
            "法律", "舞蹈", "收藏"};
    */

    private PageSwitcher pageOpr;
    private HotPageOperator hotOpr;

    private String[] tags;

    public HotWeiboBot(JsonObject parameters) {
        super(parameters);

        log = Logger.getLogger(HobbyBot.class.getName());

        // 初始化固定参数
        this.username = parameters.getString("username");
        this.password = parameters.getString("password");

        this.headless = parameters.getBoolean("headless");
        this.chromeDir = parameters.getString("chromeDir");
        this.interval = parameters.getInt("interval");
        JsonArray array = parameters.getJsonArray("tags");
        tags = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            tags[i] = array.getString(i).trim();
        }

        // 初始化页面相关操作
        log.info("> STEP1: 微博页面操作初始化");
        initOperators();
    }

    private void initOperators() {
        pageOpr = new PageSwitcher(conf);
        hotOpr = new HotPageOperator(conf);
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                // 逐个标签操作
                for (String tag : tags) {
                    // 进入热门微博页面，并关闭消息提醒，避免遮挡标签选择
                    pageOpr.gotoHotPage();
                    pageOpr.closeMsg();

                    // 进入标签
                    Date curr = new Date();
                    hotOpr.chooseTag(tag);
                    log.info("> 进入标签: " + tag);

                    // 获得微博
                    List<Weibo> weibos = hotOpr.getWeibos();
                    log.info("> 获得微博: " + weibos.size());

                    // 储存
                    log.info("> 记录");
                    List<WeiboRecord> records = new ArrayList<>();
                    for (Weibo weibo : weibos) {
                        WeiboRecord record = new WeiboRecord();
                        record.setGroupName("");
                        record.setId(weibo.getId());
                        record.setCategory(weibo.getCategory());
                        record.setContent(weibo.getContent());
                        record.setUrl(weibo.getUrl());
                        record.setTime(curr);
                        record.setUserId(weibo.getUser().getId());
                        record.setUserName(weibo.getUser().getUserName());
                        record.setUserType(weibo.getUser().getType());
                        record.setTextClass(tag);
                        records.add(record);
                    }
                    reservoir.getCollection().insertMany(records);
                }

                // 暂停interval小时
                log.info("> 等待" + interval + "小时，始自" + new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()));
                Thread.sleep(interval * 60 * 60 * 1000);
            } catch (Exception e) {
                log.error(e);
                // 重启driver
                initDriver();
                initOperators();
            }
        }
    }
}
