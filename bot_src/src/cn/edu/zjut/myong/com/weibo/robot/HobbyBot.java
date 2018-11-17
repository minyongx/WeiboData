package cn.edu.zjut.myong.com.weibo.robot;

import cn.edu.zjut.myong.com.weibo.*;
import cn.edu.zjut.myong.com.weibo.operator.*;
import cn.edu.zjut.myong.com.weibo.robot.hobby.Hobby;
import cn.edu.zjut.myong.com.weibo.robot.hobby.WeiboGroup;
import cn.edu.zjut.myong.com.weibo.robot.hobby.WeiboGroupNode;
import cn.edu.zjut.myong.com.weibo.util.JsonTool;
import cn.edu.zjut.myong.nlp.FastTextClassification;
import cn.edu.zjut.myong.nlp.TextClass;
import edu.stanford.nlp.util.Pair;
import org.apache.log4j.Logger;

import javax.json.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 继承了Java线程的Runnable，每个机器人都是运行于一个单独的线程。run方法是机器人的主线方法。
 * HobbyBot是指基于兴趣爱好的实验的机器人。
 *
 * BOT3
 * socalbot@sina.com
 * 1209love1026
 *
 * BOT2
 * 17135041821
 * wckrpj3024
 * ====>
 * 二期
 * 17816876485
 * 690807
 *
 * BOT1
 * 13666619281
 * 01hua231223
 *
 * BOT0 —— 测试用账号
 * myong1981@sina.com
 * 1209love1026
 *
 * BOT-X
 * minjun625@sina.com
 * 1209love1026
 *
 * BOT-Y
 * dyc773912355@163.com
 * ZTTwoaini1314
 */
public class HobbyBot extends BotProto {
    /* 配置文件模板，加入conf/WeiboBot.json
    "botName": "BOT3",
    "botPort": 8890,
    "groupNum": 20,
    "groupName": [
        "科技21组",
        "科技22组",
        "科技23组",
        "科技24组",
        "科技25组",
        "科技26组",
        "科技27组",
        "科技28组",
        "科技29组",
        "科技30组",
        "娱乐21组",
        "娱乐22组",
        "娱乐23组",
        "娱乐24组",
        "娱乐25组",
        "娱乐26组",
        "娱乐27组",
        "娱乐28组",
        "娱乐29组",
        "娱乐30组"
    ],
    "groupIni": [
        ["2481600572","5665911552","1812175903"],
        ["1220626621","2262061943","2194035935"],
        ["1617248032","1649586835","23998163"],
        ["2780826007","1107560787","1600243842"],
        ["1107560787","1600243842","1812175903"],
        ["1808449333","2481600572","1231317854"],
        ["5097174964","2194035935","1231317854"],
        ["1729014640","2194035935","23998163"],
        ["1730177302","5256824744","1200933465"],
        ["1600243842","2262061943","5097174964"],
        ["6289212058","2440085024"],
        ["5439664430","5544410300"],
        ["719201567","5544410300"],
        ["5761190132","3811242885"],
        ["6197131982","6028854720"],
        ["5544410300","1796495097"],
        ["1953014583","6028854720"],
        ["5792290340","1849285897"],
        ["5439664430","1787581612"],
        ["5911813820","2104108013"]
    ],
    "groupFile": "./HobbyBotGroups.obj",
    "tempDir": "./temp",
    "webDir": "./web",
    "groupHobby": [
        "Tech",
        "Tech",
        "Tech",
        "Tech",
        "Tech",
        "Tech",
        "Tech",
        "Tech",
        "Tech",
        "Tech",
        "Entertainment",
        "Entertainment",
        "Entertainment",
        "Entertainment",
        "Entertainment",
        "Entertainment",
        "Entertainment",
        "Entertainment",
        "Entertainment",
        "Entertainment"
    ],
    "groupTarget": 120,
    "remoteAlterable": true,
    "fastExe": "/home/min/fastText/build2/fasttext",
    "fastModel": "/home/min/fastText/build2/exp1/6class.bin",
    "threshold": 0.7
    */

    // 启动主类
    /*
    public static void main(String[] args) throws Exception {
        JsonReader jsonReader = Json.createReader(new InputStreamReader(new FileInputStream("./conf/HobbyBot.json")));
        JsonObject parameters = jsonReader.readObject();
        HobbyBot bot = new HobbyBot(parameters);

        for (int i = 4; i < 20; i++) {
            try {
                bot.checkUsers(i, true);
            } catch (Exception e) {
                bot.log.info("出现异常，继续该组");
                i--;
            } finally {
                bot.log.info("休息一下");
                Thread.sleep(5 * 60 * 1000);
            }
        }

        // new Thread(bot).start();
        // new Thread(new HobbyBotWebApproval(bot)).start();
        // new Thread(new HobbyBotEmailApproval(bot)).start();
    }
    */

    private PageSwitcher pageOpr;
    private HomePageOperator homeOpr;
    private UserPageOperator userOpr;
    private UserFollowingPageOperator userFollowOpr;
    private FollowingPageOperator followOpr;

    private String botName;
    private int botPort;
    private int grpN;
    private List<String[]> grpIniUser;
    private String[] grpName;
    private String[] grpHobby;
    private String grpFile;
    private String tempDir;
    private String webDir;
    private int grpTarget;
    private double threshold;
    private boolean remoteAlterable;
    private FastTextClassification classifier;

    private List<WeiboGroup> groups;
    public boolean approvable;
    private int errorCounter;

    public HobbyBot(JsonObject parameters) {
        super(parameters);

        log = Logger.getLogger(HobbyBot.class.getName());

        // 初始化固定参数
        this.botName = parameters.getString("botName");
        this.botPort = parameters.getInt("botPort");
        this.grpN = parameters.getInt("groupNum");
        this.grpName = new String[grpN];
        for (int i = 0; i < parameters.getJsonArray("groupName").size(); i++) {
            grpName[i] = parameters.getJsonArray("groupName").getString(i).trim();
        }
        this.grpIniUser = new ArrayList<>();
        for (int i = 0; i < parameters.getJsonArray("groupIni").size(); i++) {
            JsonArray ius = parameters.getJsonArray("groupIni").getJsonArray(i);
            String[] iu = new String[ius.size()];
            for (int j = 0; j < ius.size(); j++) {
                iu[j] = ius.getString(j);
            }
            grpIniUser.add(iu);
        }
        this.grpHobby = new String[grpN];
        for (int i = 0; i < parameters.getJsonArray("groupHobby").size(); i++) {
            grpHobby[i] = parameters.getJsonArray("groupHobby").getString(i);
        }
        this.grpFile = parameters.getString("groupFile");
        this.tempDir = parameters.getString("tempDir");
        this.webDir = parameters.getString("webDir");
        this.grpTarget = parameters.getInt("groupTarget");
        this.remoteAlterable = parameters.getBoolean("remoteAlterable");
        this.classifier = new FastTextClassification(
                parameters.getString("fastExe"),
                parameters.getString("fastModel"),
                this.tempDir + "/fasttext.txt");
        this.threshold = parameters.getJsonNumber("threshold").doubleValue();

        approvable = false;

        // 初始化页面相关操作
        log.info("> STEP1: 微博页面操作初始化");
        initOperators();

        // 初始化组信息
        log.info("> STEP2: 实验分组初始化");
        initGroup();

        // 一致性检验
        log.info("> STEP3: 校验远程和本地组信息一致性");
        checkGroup();

        approvable = true;
    }

    void initOperators() {
        pageOpr = new PageSwitcher(conf);
        followOpr = new FollowingPageOperator(conf);
        userOpr = new UserPageOperator(conf);
        userFollowOpr = new UserFollowingPageOperator(conf);
        homeOpr = new HomePageOperator(conf);
    }

    void initGroup() {
        // 首先查看是否有本机的分组分析保存，如果没有新建，否则读取
        File gFile = new File(grpFile);
        try {
            ObjectInputStream reader = new ObjectInputStream(new FileInputStream(gFile));
            groups = (List<WeiboGroup>) reader.readObject();
            reader.close();
        } catch (Exception e) {
            // e.printStackTrace();
            log.info(">> 无本地分组信息！");
            groups = new LinkedList<>();
        }

        // 如果分组信息为空，则依据配置文件初始化分组
        if (!groups.isEmpty()) {
            return;
        }

        // 远程组是否可写
        if (!remoteAlterable) {
            log.error(">> 远程组不可写！");
            System.exit(-1);
        }

        log.info(">> 创建远程组");
        // 清除远程组
        while (true) {
            try {
                pageOpr.gotoFollowingPage();
                // 删除所有组
                List<String> groupNames = followOpr.getGroupNames();
                for (String name : groupNames) {
                    log.info(">>> 删除组: " + name);
                    followOpr.deleteGroup(name);
                }
                // groupOpr.deleteAllGroup();
                break;
            } catch (WeiboException e) {
                e.printStackTrace();
            }
        }
        // 创建组
        int i = 0;
        while (i < grpN) {
            try {
                pageOpr.gotoFollowingPage();
                log.info(">>> 创建组: " + grpName[i]);
                followOpr.createGroup(grpName[i]);
                i++;
            } catch (WeiboException e) {
                e.printStackTrace();
            }
        }

        // 每个组添加初始用户
        List<User>[] iUser = new ArrayList[grpN];
        for (int j = 0; j < grpN; j++) {
            try {
                log.info(">> 组: " + grpName[j]);
                iUser[j] = new ArrayList<>();
                for (String uid : grpIniUser.get(j)) {
                    log.info(">>> 添加初始用户: " + uid);
                    pageOpr.gotoUserPageById(uid);
                    if (!userOpr.isFollowed())
                        userOpr.follow();
                    userOpr.setGroup(grpName[j], true);
                    User u = new User(
                            userOpr.getUserId(),
                            userOpr.getUserName(),
                            User.identifyUserCategory(userOpr.getUserIcon()),
                            User.Source.None,
                            userOpr.getFollowNum(),
                            userOpr.getFunNum(),
                            userOpr.getWeiboNum(),
                            "");
                    iUser[j].add(u);
                }
            } catch (WeiboException e) {
                // 该组重做
                j--;
                log.error(e);
            }
        }

        // 构建本地组
        log.info(">> 创建本地组");
        for (int k = 0; k < grpN; k++) {
            WeiboGroup grp = new WeiboGroup(grpName[k], grpHobby[k], iUser[k]);
            groups.add(grp);
        }
        saveProgress();
    }

    /**
     * 检查远程组是否与本地组数量和名称一致，不一致则重新创建
     */
    private void checkGroup() {
        // 组一致性
        List<String> remoteGroup;
        while (true) {
            try {
                pageOpr.gotoFollowingPage();
                remoteGroup = followOpr.getGroupNames();
                log.info(">> 远程组数量：" + remoteGroup.size());
                break;
            } catch (WeiboException e) {
                e.printStackTrace();
            }
        }

        boolean chk = true;
        log.info(">> 本地组数量：" + groups.size());
        if (groups.size() != remoteGroup.size()) {
            chk = false;
        } else {
            log.info(">> 组名称检验：" + groups.size());
            for (int i = 0; i < grpName.length; i++) {
                if (!grpName[i].equals(remoteGroup.get(i))) {
                    log.info(">>> " + grpName[i] + " <> " + remoteGroup.get(i));
                    chk = false;
                    break;
                } else {
                    log.info(">>> " + grpName[i] + " == " + remoteGroup.get(i));
                }
            }
        }

        // 是否一致？
        if (!chk) {
            log.fatal(">> 远程与本地组信息不一致！");
            System.exit(-1);
        }
    }

    /**
     * 本地没有，而远程有的用户，需要移除
     */
    void checkUsers(int grpInx, boolean isBatch) throws Exception {
        log.info(">> 统一组：" + grpName[grpInx]);
        WeiboGroup grp = groups.get(grpInx);
        pageOpr.toGroupPageInFollow(grpName[grpInx]);

        Set<String> remoteFollows = followOpr.getFollows();
        int m = remoteFollows.size();
        List<String> localFollows = grp.getAllUser();
        Set<String> localSubRemote = new HashSet<>();

        // 打印local和remote的用户列表For debug
        log.info(">>> ----- Remote ∩ Local ------");
        for (String localFollow : localFollows) {
            if (remoteFollows.contains(localFollow)) {
                log.info(">>> " + localFollow);
                remoteFollows.remove(localFollow);
            } else {
                localSubRemote.add(localFollow);
            }
        }

        log.info(">>> ----- Remote - Local ------");
        if (remoteFollows.size() > m * 0.85) {
            log.error(">>> 过量删除");
            throw new Exception("过量删除");
        } else{
            for (String remoteFollow : remoteFollows) {
                log.info(">>> " + remoteFollow);
                pageOpr.gotoUserPageById(remoteFollow);
                userOpr.setGroup(grpName[grpInx], false);
                log.info(">>> 已删除");
            }
        }

        log.info(">>> ----- Local - Remote ------");
        Random rnd = new Random();
        for (String localFollow : localSubRemote) {
            log.info(">>> " + localFollow);
            try {
                if (grp.isLegal(localFollow)) {
                    pageOpr.gotoUserPageById(localFollow);
                    if (!userOpr.isFollowed())
                        userOpr.follow();
                    userOpr.setGroup(grpName[grpInx], true);
                    log.info(">>> 已添加");
                } else {
                    log.info(">>> 非法用户");
                }
            } catch (WeiboException e) {
                if (e.getError() == WeiboException.Error.CannotFollow || e.getError() == WeiboException.Error.InvalidUser) {
                    log.info(">>> 无法关注该用户");
                    grp.setLegal(localFollow, false);
                    saveProgress();
                } else if (e.getError() == WeiboException.Error.ExcessFollow) {
                    log.info(">>> 每日关注/取消关注用户超过上限，机器人暂时结束");
                    saveProgress();
                    conf.getDriver().quit();
                    System.exit(-1);
                } else {
                    throw e;
                }
            } finally {
                if (isBatch) {
                    Thread.sleep((rnd.nextInt(60) + 90) * 1000);
                    pageOpr.gotoHomePage();
                    homeOpr.getWeibo(rnd.nextInt(12));
                }
            }
        }
    }

    List<WeiboGroupNode> getGrpMembers(int grpInx) {
        return groups.get(grpInx).nodes;
    }

    /**
     * TODO 这里使用了stanford nlp库中的pair对象，可能会由于snlp版本的变化导致序列化的问题，后面要改成自己的可控类
     * @param userId 用户id
     * @return 用户的为关系
     */
    Pair<Set<String>, Set<String>> collectRelations(String userId) {
        Pair<Set<String>, Set<String>> pair = new Pair<>();
        try {
            log.info(">> " + userId + ":");

            // 关系1
            try {
                pageOpr.gotoCommonFollowingInUserPage(userId);
                Set<String> sameFollow = userFollowOpr.getFollows();
                if (sameFollow != null) {
                    log.info(">>> 共同关注: " + sameFollow.size());
                    pair.setFirst(sameFollow);
                } else {
                    return null;
                }
            } catch (WeiboException e) {
                if (e.getError() == WeiboException.Error.NoElement) {
                    log.info(">>> 共同关注: 0");
                    pair.setFirst(new HashSet<>());
                } else {
                    throw e;
                }
            }

            // 关系2
            try {
                pageOpr.gotoSecondFollowingInUserPage(userId);
                Set<String> secondFollow = userFollowOpr.getFollows();
                if (secondFollow != null) {
                    log.info(">>> 二次关注: " + secondFollow.size());
                    pair.setSecond(secondFollow);
                } else {
                    return null;
                }
            } catch (WeiboException e) {
                if (e.getError().equals(WeiboException.Error.NoElement)) {
                    log.info(">>> 二次关注: 0");
                    pair.setSecond(new HashSet<>());
                } else {
                    throw e;
                }
            }

            return pair;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run() {
        Random rand = new Random();
        while (true) {
            try {
                // 开始一个新过程，关闭approval功能
                approvable = false;
                log.info("> ");
                log.info("> Bot激活");
                log.info("> ");

                // 随机选择一个分组，查看当页微博。
                // 按照距离目标用户个数的差距作为权重进行随机选择。差距越大，概率越高。
                int selectedGrp = rand.nextInt(grpN);
                int[] dist = new int[grpN];
                for (int i = 0; i < dist.length; i++) {
                    dist[i] = Math.max(0, grpTarget - groups.get(i).getAllUser().size());
                }
                // 计算累计差距量
                int[] cumsum = new int[grpN];
                for (int i = 0; i < grpN; i++) {
                    if (i <= 0)
                        cumsum[i] = dist[i];
                    else
                        cumsum[i] = cumsum[i-1] + dist[i];
                }
                // 都已经达成目标，则无需继续进行互联网络操作，可等待审核删除
                if (cumsum[grpN-1] <= 0) {
                    log.info("> 所有组都已经达到目标数量，暂停" + interval + "分钟！");
                    approvable = true;
                    Thread.sleep(1000 * 60 * this.interval);
                    continue;
                }
                // 按权重分布进选择
                double v = rand.nextDouble();
                for (int i = 0; i < grpN; i++) {
                    double ub = (double) cumsum[i] / cumsum[grpN-1];
                    if (v <= ub) {
                        selectedGrp = i;
                        break;
                    }
                }

                WeiboGroup grp = groups.get(selectedGrp);
                log.info("> 选择组: " + grp.name + " (" + grp.getAllUser().size() +  "/" + grpTarget + ")");

                // 对所选择的组进行用户一致性检验
                checkUsers(selectedGrp, false);

                // 进入所选择的组的页面
                try {
                    pageOpr.gotoHomePage();
                    pageOpr.gotoGroupInHomePage(grp.name);
                } catch (WeiboException e) {
                    log.error(e);
                    log.error("> 无法进入选定的组");
                    continue;
                }

                // 初始化微博索引和微博数量，以及存放可关注候选者的列表
                int weiboIndex = 0;
                int weiboNum = homeOpr.getWeiboNumber();
                List<Candidate> candidates = new ArrayList<>();

                // 记录第一条微博，作为最新微博予以保留，可以防止后面不断读取大量微博
                Weibo firstWeibo = null;
                if (weiboNum > 0)
                    firstWeibo = homeOpr.getWeibo(0);

                // 遍历所有微博，寻找候选者，但不翻页
                while (weiboIndex < weiboNum) {
                    // 当前位置
                    log.info("> " + weiboIndex + "/" + weiboNum);

                    // 微博基本合法性检验
                    Weibo weibo = homeOpr.getWeibo(weiboIndex);
                    Date weiboDate = homeOpr.getWeiboDate(weiboIndex);
                    weiboNum = homeOpr.getWeiboNumber();
                    if (weibo == null) {
                        throw new Exception("无法获取微博");
                    }
                    if (weibo.getUser().getId() == null) {
                        throw new Exception("无法获取微博发布者信息");
                    }

                    // 上一条用于添加用户的微博，不再继续遍历
                    if (grp.lastWeibo != null && grp.lastWeibo.getId().equals(weibo.getId())) {
                        log.info("> 本组没有新微博");
                        break;
                    }

                    // 非本组用户所发微博，跳过
                    if (grp.seekNode(weibo.getUser().getId()) == null) {
                        log.info("> 非本组用户微博");
                        weiboIndex++;
                        continue;
                    }

                    // 保存微博
                    try {
                        WeiboRecord record = new WeiboRecord();
                        record.setGroupName(grp.name);
                        record.setId(weibo.getId());
                        record.setCategory(weibo.getCategory());
                        record.setContent(weibo.getContent());
                        record.setUrl(weibo.getUrl());
                        record.setTime(weiboDate);
                        record.setUserId(weibo.getUser().getId());
                        record.setUserName(weibo.getUser().getUserName());
                        record.setUserType(weibo.getUser().getType());
                        record.setTextClass("");
                        reservoir.getCollection().insertOne(record);
                    } catch (Exception e) {
                        log.error(e);
                        log.error("保存微博失败");
                    }

                    // 微博中是否有合法用户
                    List<String> uLinks = homeOpr.getMentionedUserUrl(weiboIndex);
                    if (uLinks == null || uLinks.size() <= 0) {
                        log.info("> 无被提及用户");
                        weiboIndex++;
                        continue;
                    }

                    // 微博的类型合法性
                    if (!grp.hobby.equals(Hobby.Null)) {
                        TextClass[] textClass = classifier.classify(weibo.getContent());
                        log.info("> [微博] " + weibo.getContent());
                        log.info("> [分类] " + textClass[0].getTextClass());
                        log.info("> [置信度] " + textClass[0].getConfidence());
                        if (textClass[0].getConfidence() < threshold
                                || !Hobby.cn2en(textClass[0].getTextClass()).equals(grp.hobby)) {
                            log.info("> 非对应偏好");
                            weiboIndex++;
                            continue;
                        }
                    }

                    // 查找合法微博中的候选者
                    List<String> users = homeOpr.getMentionedUser(weiboIndex);
                    for (String u : users) {
                        if (u.startsWith("id=")) {
                            String id = u.substring(3);
                            if (grp.seekNode(id) == null) {
                                Candidate t = new Candidate();
                                t.user = id;
                                t.idOrName = Candidate.ID;
                                t.weibo = weibo;
                                candidates.add(t);
                                log.info("> 候选者: " + t.user);
                            }
                        } else if (u.startsWith("name=")) {
                            String name = u.substring(5);
                            if (grp.seekNodeByName(name) == null) {
                                Candidate t = new Candidate();
                                t.user = name;
                                t.idOrName = Candidate.NAME;
                                t.weibo = weibo;
                                candidates.add(t);
                                log.info("> 候选者: " + t.user);
                            }
                        }
                    }
                    log.info("> 候选者数量: " + candidates.size());
                    weiboIndex++;
                }

                // 在所有目标中选择一个目标进行关注
                if (candidates.size() > 0) {
                    // 选择用户，并去向该用户页面
                    int s = rand.nextInt(candidates.size());
                    Candidate t = candidates.get(s);
                    if (t.idOrName == Candidate.ID)
                        pageOpr.gotoUserPageById(t.user);
                    else if (t.idOrName == Candidate.NAME)
                        pageOpr.gotoUserPageByName(t.user);
                    else
                        throw new Exception("无法去向目标候选者");
                    log.info("> 选中用户 [" + t.user + "]");

                    // 建立用户
                    User u = new User(
                            userOpr.getUserId(),
                            userOpr.getUserName(),
                            User.identifyUserCategory(userOpr.getUserIcon()),
                            User.Source.None,
                            userOpr.getFollowNum(),
                            userOpr.getFunNum(),
                            userOpr.getWeiboNum(),
                            ""
                    );

                    // 检查是否可加入本组关注树
                    if (grp.seekNode(u.getId()) == null) {
                        if (grp.addUser(u, t.weibo.getUser().getId(), t.weibo)) {
                            // 如果成功添加了用户到本地
                            try {
                                if (!userOpr.isFollowed())
                                    userOpr.follow();
                                userOpr.setGroup(grp.name, true);
                                log.info("> 添加用户 [" + u.getUserName() + "]");
                                grp.lastWeibo = t.weibo;
                            } catch (WeiboException e) {
                                grp.removeUser(u.getId());
                                log.error(e);
                                log.error("> 添加用户失败");
                            } finally {
                                saveProgress();
                            }
                        } else {
                            log.fatal("> 无法找到父节点");
                            System.exit(-1);
                        }
                    }

                } else {
                    // 如果没有可用的候选者，则将本次第一条微博设为最新微博
                    grp.lastWeibo = firstWeibo;
                }

                // 一次正常运行后，错误计数清零
                errorCounter = 0;

            } catch (Exception e) {
                // 出错信息
                log.error(e);
                log.error("> [错误] " + e.getMessage());

                // 是否无法继续关注
                if (e instanceof WeiboException
                        && ((WeiboException) e).getError() == WeiboException.Error.ExcessFollow) {
                    log.info("> 每日关注/取消关注用户超过上限，机器人暂时结束");
                    saveProgress();
                    return;
                }

                // 保存出错时的网页截图
                try {
                    String figName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                    pageOpr.screenshot("./log/" + figName + ".png");
                    // File source_file = conf.getDriver().getScreenshotAs(OutputType.FILE);
                    // FileUtils.copyFile(source_file, new File(figName + ".png"));
                    log.info("> 保存错误网页截图");
                } catch(Exception ee) {
                    log.error(ee);
                    log.error("> 无法保存错误网页截图");
                }

                // 连续错误计数增加
                errorCounter++;

                // 3次以上连续错误，重启WebDriver
                if (errorCounter >= 3 || e.getMessage().contains("session") || e.getMessage().contains("crash")) {
                    log.error("> 连续3次未正常运行或WebDriver出错");
                    errorCounter = 0;
                    initDriver();
                    initOperators();
                }

            } finally {
                // 每次运行后，等待间隔后再操作
                approvable = true;
                try {
                    int m = interval * 60 + rand.nextInt(120) - 60;
                    log.info("> 暂停" + m + "秒");
                    Thread.sleep(m * 1000);
                } catch (InterruptedException e) {
                    log.error(e);
                }
            }
        }
    }

    public String getBotName() {
        return botName;
    }

    public int getBotPort() {
        return botPort;
    }

    public int getGrpN() {
        return grpN;
    }

    /**
     * 返回20个组的信息
     *
     * @return nothing
     */
    public JsonArrayBuilder getGroupsInfo() {
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (WeiboGroup group : groups) {
            array.add(group.toJson());
        }
        return array;
    }

    /**
     * 返回没有验证过的用户节点信息
     *
     * @return nothing
     */
    public JsonArrayBuilder getAllUncheckedUsers() {
        JsonArrayBuilder all = Json.createArrayBuilder();
        // for (WeiboGroup group : groups)
        //    all.add(group.getUncheckedNodes());
        for (WeiboGroup group : groups) {
            all.add(group.getUncheckedNodes());
        }
        return all;
    }

    /**
     * 用户验证
     *
     * @param gid x
     * @param uid x
     * @param passes x
     */
    public void approve(int gid, String uid, boolean passes) {
        if (approvable)
            groups.get(gid).checkUser(uid, passes);
        saveProgress();
    }

    /**
     * 当有新的关注或者用户验证时，需要保存进度
     */
    public void saveProgress() {
        try {
            ObjectOutputStream oWriter = new ObjectOutputStream(new FileOutputStream(new File(grpFile)));
            oWriter.writeObject(groups);
            oWriter.flush();
            oWriter.close();
            //
            BufferedWriter jWriter = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(
                                    new File(grpFile + ".json"))));
            JsonArrayBuilder array = Json.createArrayBuilder();
            for (WeiboGroup grp : groups) {
                array.add(grp.toJson());
            }
            jWriter.write(JsonTool.formatJson(array.build().toString()));
            jWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Candidate {
        public static final int NAME = 0;
        public static final int ID = 1;
        String user;
        int idOrName;
        Weibo weibo;
    }

    public String getTempDir() {
        return tempDir;
    }

    public String getWebDir() {
        return webDir;
    }
}
