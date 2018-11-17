package cn.edu.zjut.myong.com.weibo.robot;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

public class WeiboRecordReservoir {
    private MongoClient mClient;
    private MongoCollection<WeiboRecord> mCollection;

    /**
     * 私有化构造函数，用于确保一个系统中只有一个CrawlerMongoDB实例。依据系统配置文件的储存参数
     * 连接数据库。
     */
    public WeiboRecordReservoir(String host, int port, String db, String collection) {
        mClient = new MongoClient(host, port);
        MongoDatabase mDB = mClient.getDatabase(db);
        CodecRegistry registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new WeiboRecordCodec()));
        mCollection = mDB.getCollection(collection, WeiboRecord.class).withCodecRegistry(registry);
    }

    public MongoCollection<WeiboRecord> getCollection() {
        return mCollection;
    }

    /**
     * 关闭数据库。
     */
    public void close() {
        try {
            mClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
