package com.practice.bus.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.practice.bus.bean.TaskEntity;
import com.practice.bus.controller.DocController;
import com.practice.common.enums.JobStatusEnum;
import com.practice.common.quartz.utils.QuartzManager;
import com.practice.util.FastJsonConvertUtil;
import com.practice.util.JsonUtil;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    QuartzManager quartzManager;

    public void initSchedule() throws SchedulerException {
        // 这里获取任务信息数据
        String path = TaskService.class.getClassLoader().getResource("conf/task-conf.json").getPath();
        List<TaskEntity> jobList = FastJsonConvertUtil.convertArrayToList(JsonUtil.readJsonFile(path), TaskEntity.class);
        for (TaskEntity task : jobList) {
            if (JobStatusEnum.RUNNING.getCode().equals(task.getJobStatus())) {
                quartzManager.addJob(task);
            }
        }
    }

}
