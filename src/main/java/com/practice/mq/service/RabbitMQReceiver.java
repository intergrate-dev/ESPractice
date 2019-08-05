package com.practice.mq.service;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.practice.bus.bean.SiteMonitorEntity;
import com.practice.util.DateParseUtil;
import com.practice.util.FastJsonConvertUtil;
import com.rabbitmq.client.impl.AMQImpl;
import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.practice.bus.bean.DocInfo;
import com.practice.bus.bean.EnumOperation;
import com.practice.config.RabbitMQConfig;
import com.practice.es.service.ESDocumentTemplate;
import com.practice.es.service.ESService;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

//从指定队列中接收消息
@Component
public class RabbitMQReceiver {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ESService indexService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleMessage(JSONObject content) throws InterruptedException {

        RabbitMessage rabbitMessage = JSONObject.toJavaObject(content, RabbitMessage.class);
        EnumOperation operation = rabbitMessage.getOperation();
        SiteMonitorEntity siteMonitor = rabbitMessage.getSiteMonitor();
        logger.info("==================== recieve handleMessage, siteId & task & status:{} ---- {} ---- {}, operation: {}, time: {}  ======================",
                siteMonitor.getId(), siteMonitor.getTask(), siteMonitor.getStatus(), operation, DateParseUtil.dateTimeToString(new Date()));

        switch (operation) {
            case ADD:
                indexService.createSiteInfo(siteMonitor);
                break;
            case MODIFY:
                indexService.modifySiteInfo(siteMonitor);
                break;
            case DELETE:
                break;
        }
    }


}
