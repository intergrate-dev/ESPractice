package com.practice.bus.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.practice.bus.bean.DocInfo;
import com.practice.bus.bean.SiteMonitorEntity;
import com.practice.bus.service.DocService;
import com.practice.common.ResponseObject;
import com.practice.common.SystemConstant;
import com.practice.es.service.ESService;
import com.practice.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务Controller
 * 使用for循环模拟大量请求，检测MQ和ES是否能正常工作
 */
@Controller
@RequestMapping("/")
public class IndexController {

    /**
     * 查询文档
     */
    @RequestMapping("/index")
    @ResponseBody
    public String search() {
        return "/index.html";
    }
}
