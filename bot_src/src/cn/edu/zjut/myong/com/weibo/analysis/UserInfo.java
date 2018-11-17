package cn.edu.zjut.myong.com.weibo.analysis;

import cn.edu.zjut.myong.com.weibo.robot.hobby.WeiboGroup;
import cn.edu.zjut.myong.com.weibo.robot.hobby.WeiboGroupNode;

import java.io.*;
import java.util.*;

public class UserInfo {

    public static final String[] EG05 = new String[] {
            "1087770692",
            "1195381367",
            "1210101913",
            "1246130430",
            "1247022055",
            "1252036043",
            "1257818405",
            "1295560007",
            "1303977362",
            "1346710264",
            "1412151723",
            "1425930027",
            "1628616933",
            "1639052883",
            "1642592432",
            "1642605821",
            "1645211065",
            "1646239802",
            "1646255843",
            "1646272724",
            "1646318871",
            "1646324894",
            "1646356982",
            "1646514243",
            "1650641401",
            "1661493522",
            "1665103091",
            "1665755171",
            "1674615553",
            "1704091601",
            "1716032452",
            "1717675430",
            "1722647874",
            "1729562517",
            "1739046981",
            "1751035982",
            "1757953773",
            "1779409587",
            "1779850265",
            "1802324580",
            "1822796164",
            "1827683445",
            "1834834840",
            "1849285897",
            "1850101627",
            "1862036640",
            "1862105874",
            "1887094534",
            "1904671130",
            "2002890060",
            "2006455031",
            "2014034660",
            "2080579601",
            "2101994372",
            "2102739291",
            "2272669450",
            "2321588694",
            "2336573415",
            "2412421034",
            "2432854134",
            "2482557597",
            "2495494187",
            "2497052030",
            "2507157603",
            "2533607591",
            "2591595652",
            "2658735821",
            "2689280541",
            "2706896955",
            "2723838314",
            "2731728324",
            "2814059080",
            "2907514197",
            "2948536215",
            "2953833595",
            "2996806992",
            "3118861807",
            "3262382641",
            "3365597502",
            "3547332173",
            "3560003601",
            "3594001605",
            "3623205353",
            "3699172790",
            "3758512144",
            "45819349",
            "5132146757",
            "5255814135",
            "5300636312",
            "5350911311",
            "5387377467",
            "5396404131",
            "5511008040",
            "5532072579",
            "5533547170",
            "5541182601",
            "5615627418",
            "5616921290",
            "5628495345",
            "5786272905",
            "5833842342",
            "5851229890",
            "5863008460",
            "5982398082",
            "6029875729",
            "6050539019",
            "6068315751",
            "6085605492",
            "6283155721",
            "6288204676",
            "6321604984",
            "6326658550",
            "6373339671",
            "6423838632",
            "6469158167",
            "6499729665",
            "6501611329",
            "6573404886",
            "6601926578"
    };

    public static final String[] STG02 = new String[]{
            "1044173452",
            "1046485715",
            "1100856704",
            "1107560787",
            "1111681197",
            "1120663884",
            "1203003525",
            "1231317854",
            "1260422202",
            "1340724027",
            "1367009482",
            "1396927662",
            "1402400261",
            "1477342667",
            "1495357574",
            "1565193811",
            "1618051664",
            "1641561812",
            "1642632622",
            "1642720480",
            "1644395354",
            "1649155730",
            "1649383554",
            "1652811601",
            "1676652780",
            "1684430384",
            "1711479641",
            "1715118170",
            "1720489883",
            "1722583785",
            "1722883092",
            "1738690784",
            "1746408033",
            "1748548681",
            "1748930024",
            "1783631265",
            "1805250771",
            "1806732505",
            "1807436544",
            "1808449333",
            "1812175903",
            "1849980831",
            "1887312183",
            "1906698021",
            "1951919037",
            "1956700750",
            "1998336453",
            "2081558227",
            "2135348954",
            "2136342863",
            "2231230902",
            "2297255040",
            "2312103501",
            "2393831515",
            "2432441407",
            "2473476713",
            "2868676035",
            "3097251601",
            "3172142827",
            "3176804502",
            "3569314904",
            "3710258141",
            "3898260405",
            "3921730119",
            "5034901460",
            "5198011111",
            "5231699489",
            "5242649983",
            "5243619735",
            "5256824744",
            "5287003755",
            "5324875209",
            "5375583682",
            "5501429448",
            "5518421579",
            "5566876030",
            "5590704704",
            "5623180299",
            "5665911552",
            "5724173799",
            "5799721772",
            "5894736946",
            "5972893517",
            "5976424681",
            "5999840266",
            "6027333782",
            "6041830477",
            "6087767848",
            "6092740159",
            "6094846964",
            "6176882343",
            "6183026864",
            "6226268544",
            "6307202194",
            "6375760521",
            "6417385970",
            "6476809047",
            "6486985643",
            "6501723193",
            "6516579468",
            "6543766562"
    };

    public static void printUserSequence() throws Exception {
        String grpFile = "C:\\Workspace\\[A]www\\exp1\\实验原始数据BOT1\\BOT1-HobbyBotGroups.obj";
        String grpName = "科技02组";

        File gFile = new File(grpFile);
        List<WeiboGroup> groups;
        try {
            ObjectInputStream reader = new ObjectInputStream(new FileInputStream(gFile));
            groups = (List<WeiboGroup>) reader.readObject();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        WeiboGroup group = null;
        for (WeiboGroup grp : groups) {
            if (grp.name.equals(grpName)) {
                group = grp;
                break;
            }
        }

        List<Datum> users = new ArrayList<>();
        if (group != null) {
            for (String uid : group.getAllUser()) {
                WeiboGroupNode node = group.seekNode(uid);
                if (node != null && node.legal) {
                    Datum d = new Datum();
                    d.user = node.user.getId();
                    d.time = node.updateTime;
                    users.add(d);
                }
            }
        }
        Collections.sort(users);
        for (Datum d : users) {
            for (int i = 0; i < STG02.length; i++) {
                if (STG02[i].equals(d.user)) {
                    System.out.println(i+1);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        printUserSequence();

        String[] tags = new String[]{"娱乐", "科技"};

        for (int k = 0; k < tags.length; k++) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tags[k] + ".user")));
            Set<String> visited = new HashSet<>();
            for (int i = 1; i <= 3; i++) {
                for (int j = (i - 1) * 10 + 1; j <= (i - 1) * 10 + 10; j++) {
                    String grpFile = "C:\\Workspace\\[A]www\\exp1\\实验原始数据BOT" + i + "\\BOT" + i + "-HobbyBotGroups.obj";
                    String grpName = tags[k] + (j < 10 ? "0" : "") + j + "组";

                    File gFile = new File(grpFile);
                    List<WeiboGroup> groups;
                    try {
                        ObjectInputStream reader = new ObjectInputStream(new FileInputStream(gFile));
                        groups = (List<WeiboGroup>) reader.readObject();
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    WeiboGroup group = null;
                    for (WeiboGroup grp : groups) {
                        if (grp.name.equals(grpName)) {
                            group = grp;
                            break;
                        }
                    }

                    if (group != null) {
                        for (String uid : group.getAllUser()) {
                            WeiboGroupNode node = group.seekNode(uid);
                            if (!visited.contains(uid) && node != null && node.legal) {
                                writer.write(node.user.getFollowNum() + " " + node.user.getFunNum() + " " + node.user.getWeiboNum());
                                writer.newLine();
                                visited.add(uid);
                            }
                        }
                    } else {
                        System.err.println("no group!");
                        return;
                    }
                }
            }
            writer.close();
        }
    }
}
