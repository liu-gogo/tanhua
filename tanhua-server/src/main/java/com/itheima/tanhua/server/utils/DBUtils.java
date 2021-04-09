package com.itheima.tanhua.server.utils;

import org.bson.types.ObjectId;

import java.util.Date;

public class DBUtils {
    //假设 对publish表 分了10张表
    private static final int PUBLISH_DB_COUNT = 10;

    public static ObjectId getObjectId(Long userId){
        return new ObjectId(new Date(),(int) ((userId%16777215)));
    }
	//如果前端 参数传递userId的话
    public static int shardingPublish(ObjectId objectId){

        return objectId.getCounter() % 10;
    }

    public static int shardingPublish(Long userId){

        return (int) ((userId%16777215) % 10);  //(int) ((userId%16777215) % 10)) = 0-9 
    }

    public static void main(String[] args) {
        int dbCount = DBUtils.shardingPublish(DBUtils.getObjectId(1000L));

        int dbCount1 = DBUtils.shardingPublish(1000L);

        System.out.println(dbCount);
        System.out.println(dbCount1);
    }
}


