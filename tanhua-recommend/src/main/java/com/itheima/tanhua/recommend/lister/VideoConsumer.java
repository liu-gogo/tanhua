package com.itheima.tanhua.recommend.lister;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.itheima.tanhua.recommend.pojo.RecommendVideo;
import com.itheima.tanhua.recommend.vo.VideoMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "tanhua-video",consumerGroup = "tanhua-video-group")
public class VideoConsumer implements RocketMQListener<String> {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void onMessage(String message) {
        //消费者监听器 监听topic，一旦队列中有消息，立马就会被onMessage 获取到
        //拿到消息，解析，获取到videoid
        //去查询Video的信息，根据计分规则 进行计分
        //数据存储到 recommend_video表中

        //       Map<String, Object> msg = new HashMap<>();
        //            msg.put("userId", userId);
        //            msg.put("date", System.currentTimeMillis());
        //            msg.put("videoId", videoId);
        //            msg.put("vid", video.getVid());
        //            msg.put("type", typeEnum.getValue());

        try {
            VideoMsg videoMsg = mapper.readValue(message, VideoMsg.class);

            Integer type = videoMsg.getType();

            RecommendVideo recommendVideo = new RecommendVideo();
            recommendVideo.setUserId(videoMsg.getUserId());
            recommendVideo.setVideoId(videoMsg.getVid());
            recommendVideo.setId(ObjectId.get());
            recommendVideo.setDate(videoMsg.getDate());

            //根据type的不同 进行分数计算
            switch (type){
                case 1: {
                    recommendVideo.setScore(2d);
                    break;
                }
                case 2: {
                    recommendVideo.setScore(5d);
                    break;
                }
                case 3: {
                    recommendVideo.setScore(-5d);
                    break;
                }
                case 4: {
                    recommendVideo.setScore(10d);
                    break;
                }
                default: {
                    recommendVideo.setScore(0d);
                    break;
                }
            }

            //mongo表的保存
            this.mongoTemplate.save(recommendVideo,"recommend_video_"+new DateTime().toString("yyyyMMdd"));
            log.info("消息消费成功: publishId={}",videoMsg.getVid());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("有错误~~");
        }

    }
}
