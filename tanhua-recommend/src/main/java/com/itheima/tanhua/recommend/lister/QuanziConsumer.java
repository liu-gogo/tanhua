package com.itheima.tanhua.recommend.lister;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.tanhua.dubbo.server.pojo.Publish;

import com.itheima.tanhua.recommend.pojo.RecommendQuanZi;
import com.itheima.tanhua.recommend.vo.QuanziMsg;
import lombok.extern.slf4j.Slf4j;
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
@RocketMQMessageListener(topic = "tanhua-quanzi",consumerGroup = "tanhua-quanzi-group")
public class QuanziConsumer implements RocketMQListener<String> {

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void onMessage(String message) {
        //消费者监听器 监听topic，一旦队列中有消息，立马就会被onMessage 获取到
        //拿到消息，解析，获取到publishid
        //去查询Publish的信息，根据计分规则 进行计分
        //数据存储到 recommend_quanzi表中

        //         Map<String, Object> msg = new HashMap<>();
        //            msg.put("userId", user.getId());
        //            msg.put("date", System.currentTimeMillis());
        //            msg.put("publishId", publishId);
        //            msg.put("pid", publish.getPid());
        //            msg.put("type", typeEnum.getValue());

        try {
            QuanziMsg quanziMsg = mapper.readValue(message, QuanziMsg.class);

            Integer type = quanziMsg.getType();

            RecommendQuanZi recommendQuanZi = new RecommendQuanZi();
            recommendQuanZi.setUserId(quanziMsg.getUserId());
            recommendQuanZi.setPublishId(quanziMsg.getPid());
            recommendQuanZi.setId(ObjectId.get());
            recommendQuanZi.setDate(quanziMsg.getDate());

            //根据type的不同 进行分数计算
            switch (type){
                case 1: {
                    int score = 0;
                    Publish publish = this.mongoTemplate.findById(new ObjectId(quanziMsg.getPublishId()), Publish.class);
                    if (StringUtils.length(publish.getText()) < 50) {
                        score += 1;
                    } else if (StringUtils.length(publish.getText()) < 100) {
                        score += 2;
                    } else if (StringUtils.length(publish.getText()) >= 100) {
                        score += 3;
                    }

                    if (!CollectionUtils.isEmpty(publish.getMedias())) {
                        score += publish.getMedias().size();
                    }

                    recommendQuanZi.setScore(Double.valueOf(score));

                    break;
                }
                case 2: {
                    recommendQuanZi.setScore(1d);
                    break;
                }
                case 3: {
                    recommendQuanZi.setScore(5d);
                    break;
                }
                case 4: {
                    recommendQuanZi.setScore(8d);
                    break;
                }
                case 5: {
                    recommendQuanZi.setScore(10d);
                    break;
                }
                case 6: {
                    recommendQuanZi.setScore(-5d);
                    break;
                }
                case 7: {
                    recommendQuanZi.setScore(-8d);
                    break;
                }
                default: {
                    recommendQuanZi.setScore(0d);
                    break;
                }
            }

            //mongo表的保存
            this.mongoTemplate.save(recommendQuanZi,"recommend_quanzi_"+new DateTime().toString("yyyyMMdd"));
            log.info("消息消费成功: publishId={}",quanziMsg.getPublishId());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("有错误~~");
        }

    }
}
