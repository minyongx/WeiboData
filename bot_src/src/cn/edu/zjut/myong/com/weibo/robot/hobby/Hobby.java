package cn.edu.zjut.myong.com.weibo.robot.hobby;

import java.io.Serializable;

public enum Hobby implements Serializable {
    Politics,
    Money,
    Regions,
    Tech,
    Entertainment,
    Sport,
    Education,
    Realty,
    Home,
    Null;

    public static Hobby cn2en(String v) {
        switch (v) {
            case "时政": return Politics;
            case "财经": return Money;
            case "社会": return Regions;
            case "科技": return Tech;
            case "娱乐": return Entertainment;
            case "体育": return Sport;
            case "教育": return Education;
            case "房产": return Realty;
            case "家居": return Home;
            default: return Null;
        }
    }

    @SuppressWarnings("unused")
    public static String en2cn(Hobby v) {
        switch (v) {
            case Politics: return "时政";
            case Money: return "财经";
            case Regions: return "社会";
            case Tech: return "科技";
            case Entertainment: return "娱乐";
            case Sport: return "体育";
            case Education: return "教育";
            case Realty: return "房产";
            case Home: return "家居";
            default: return "无非类";
        }
    }
}
