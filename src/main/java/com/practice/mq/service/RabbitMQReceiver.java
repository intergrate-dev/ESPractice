package com.practice.mq.service;

import com.alibaba.fastjson.JSONObject;
import com.practice.bus.bean.EnumOperation;
import com.practice.bus.bean.SiteMonitorEntity;
import com.practice.config.RabbitMQConfig;
import com.practice.es.service.ESService;
import com.practice.util.DateParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

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
        logger.info("------------------------- recieve handleMessage, operation: {}, info:{} ---- {} ---- {} ---- updateTime: {}, at now:  {}, extInfo: {} ----------------------------------",
                operation, siteMonitor.getId(), siteMonitor.getTask(), siteMonitor.getStatus(), siteMonitor.getUpdateTime(),
                DateParseUtil.dateTimeToString(new Date()), siteMonitor.getExtInfo());

        switch (operation) {
            case ADD:
                indexService.createSiteInfo(siteMonitor);
                break;
            case MODIFY:
                indexService.modifySiteInfo(siteMonitor);
                break;
            default:
                break;
        }
    }


}
