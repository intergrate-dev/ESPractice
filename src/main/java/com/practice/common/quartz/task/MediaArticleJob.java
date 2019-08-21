package com.practice.common.quartz.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.practice.bus.service.ApiService;
import com.practice.util.DateParseUtil;
import com.practice.util.JsonUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@DisallowConcurrentExecution //作业不并发
@Component
public class MediaArticleJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(MediaArticleJob.class);

    @Autowired
    ApiService apiService;

    @Override
    public void execute(JobExecutionContext jobExecContext) throws JobExecutionException {
        logger.info("----------------------- 定时任务 开始执行, 时间： {}", DateParseUtil.dateTimeToString(new Date()));
        String path = ApiService.class.getClassLoader().getResource("conf/media-conf.json").getPath();
        JSONArray array = JSONArray.parseArray(JsonUtil.readJsonFile(path));
        if (array == null || array.size() == 0) {
            return;
        }
        array.stream().forEach(a -> {
            JSONObject json = (JSONObject) a;
            apiService.fetchAndPutData(json.getString("mediaId"), json.getString("codes"), null,
                    json.getString("types"));
        });
    }

}