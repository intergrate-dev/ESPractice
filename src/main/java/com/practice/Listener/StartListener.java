package com.practice.Listener;

import com.practice.util.DateParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by yuan-pc on 2018/3/26.
 */
@Component
public class StartListener implements ApplicationListener<ApplicationReadyEvent> {
    private static Logger logger = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            Thread.sleep(3000);
            long time_1 = System.currentTimeMillis();
            // TODO 初始化所有索引及配置
            logger.info("=================== starter execute time: {}, take times: {} s ===============",
                    DateParseUtil.dateToString(new Date()), (System.currentTimeMillis() - time_1)/1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
