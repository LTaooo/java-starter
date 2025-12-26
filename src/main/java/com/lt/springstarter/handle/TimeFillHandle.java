package com.lt.springstarter.handle;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lt.springstarter.util.Datetime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * 时间填充处理器
 */
@Component
public class TimeFillHandle implements MetaObjectHandler {
    private static final String CREATE_TIME = "createdAt";
    private static final String UPDATE_TIME = "updatedAt";

    @Override
    public void insertFill(MetaObject metaObject) {
        long now = Datetime.timestamp();
        if (metaObject.hasGetter(CREATE_TIME)) {
            strictInsertFill(metaObject, CREATE_TIME, Long.class, now);
        }
        if (metaObject.hasGetter(UPDATE_TIME)) {
            strictInsertFill(metaObject, UPDATE_TIME, Long.class, now);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasGetter(UPDATE_TIME)) {
            strictUpdateFill(metaObject, UPDATE_TIME, Long.class, Datetime.timestamp());
        }
    }
}
