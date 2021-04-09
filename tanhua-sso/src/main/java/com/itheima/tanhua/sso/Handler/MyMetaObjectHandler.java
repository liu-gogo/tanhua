package com.itheima.tanhua.sso.Handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sun.media.jfxmedia.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("insert fill invoke...");
        Object created = getFieldValByName("created", metaObject);
        if(created == null){
            setFieldValByName("created", new Date(), metaObject);
        }

        Object updated = getFieldValByName("updated", metaObject);
        if(updated == null){
            setFieldValByName("updated", new Date(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("update fill invoke...");

        setFieldValByName("updated", new Date(), metaObject);



    }
}
