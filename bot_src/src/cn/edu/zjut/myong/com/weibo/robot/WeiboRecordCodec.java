package cn.edu.zjut.myong.com.weibo.robot;

import cn.edu.zjut.myong.com.weibo.User;
import cn.edu.zjut.myong.com.weibo.Weibo;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.Date;

public class WeiboRecordCodec implements Codec<WeiboRecord> {
    @Override
    public WeiboRecord decode(BsonReader bsonReader, DecoderContext decoderContext) {
        WeiboRecord weibo = new WeiboRecord();
        weibo.setGroupName(bsonReader.readString("GroupName"));
        weibo.setId(bsonReader.readString("Id"));
        weibo.setCategory(Weibo.Category.valueOf(bsonReader.readString("Category")));
        weibo.setContent(bsonReader.readString("Content"));
        weibo.setUrl(bsonReader.readString("Url"));
        Date d = new Date();
        d.setTime(bsonReader.readDateTime("Time"));
        weibo.setTime(d);
        weibo.setUserId(bsonReader.readString("UserId"));
        weibo.setUserName(bsonReader.readString("UserName"));
        weibo.setUserType(User.Category.valueOf(bsonReader.readString("UserType")));
        weibo.setTextClass(bsonReader.readString("TextClass"));
        return weibo;
    }

    @Override
    public void encode(BsonWriter bsonWriter, WeiboRecord weibo, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        {
            bsonWriter.writeString("GroupName", weibo.getGroupName());
            bsonWriter.writeString("Id", weibo.getId());
            bsonWriter.writeString("Category", weibo.getCategory().name());
            bsonWriter.writeString("Content", weibo.getContent());
            bsonWriter.writeString("Url", weibo.getUrl());
            bsonWriter.writeDateTime("Time", weibo.getTime().getTime());
            bsonWriter.writeString("UserId", weibo.getUserId());
            bsonWriter.writeString("UserName", weibo.getUserName());
            bsonWriter.writeString("UserType", weibo.getUserType().name());
            bsonWriter.writeString("TextClass", weibo.getTextClass());
        }
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<WeiboRecord> getEncoderClass() {
        return WeiboRecord.class;
    }
}
