package com.practice.bus.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.practice.bus.bean.SiteMonitorEntity;
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

import com.practice.bus.bean.DocInfo;
import com.practice.bus.service.DocService;

import java.util.*;

/**
 * 业务Controller
 * 使用for循环模拟大量请求，检测MQ和ES是否能正常工作
 */
@Controller
@RequestMapping("/doc")
public class DocController {
    @Autowired
    DocService docService;
    @Autowired
    ESService esService;


    /**
     * 查询文档
     */
    @RequestMapping("/search")
    @ResponseBody
    public String search() {
        Map<String, String> map = new HashMap();
        map.put("docId", "002");
        docService.searchDocs(map);
        return "add success";
    }

    /**
     * 添加文档
     */
    @RequestMapping("/createDoc")
    @ResponseBody
    public String createDoc() {
        DocInfo docInfo = null;
        for (long i = 5; i < 7; i++) {
            docInfo = new DocInfo();
            docInfo.setDocId(i);
            docService.createDoc(docInfo);
        }
        return "add success";
    }

    /**
     * 修改文档
     */
    @RequestMapping("/modifyDoc")
    @ResponseBody
    public String modifyDoc() {
        DocInfo docInfo = null;
        for (long i = 0; i < 1; i++) {
            docInfo = new DocInfo();
            docInfo.setDocId(i);
            docInfo.setDocName("文档--" + i + ".doc");
            docService.modifyDoc(docInfo);
        }
        return "modify success";
    }

    /**
     * 删除文档
     */
    @RequestMapping("/deleteDoc")
    @ResponseBody
    public String deleteDoc() {
        DocInfo docInfo = new DocInfo();
        docInfo.setDocId(1L);
        docService.deleteDoc(docInfo);
        return "delete success";
    }


    @RequestMapping("/mappingSiteInfo")
    @ResponseBody
    public String mappingSiteInfo() {

        String path = DocController.class.getClassLoader().getResource("esconf/mapping-siteMonitor.json").getPath();
        String s = JsonUtil.readJsonFile(path);
        JSONObject json = JSON.parseObject(s);

        esService.mappingSiteInfo(json);
        return "mappingSiteInfo success";
    }

    @RequestMapping("/createSiteInfo")
    @ResponseBody
    public String createSiteInfo() {
        //String define = "task_";
        String define = "schedule_";
        String[] channels = {"网站", "微信", "微博", "APP"};
        SiteMonitorEntity sme = null;
        for (int i = 1; i < 3; i++) {
            sme = new SiteMonitorEntity();
            sme.setId("sid_00".concat(String.valueOf(i)));
            sme.setSiteName(define.concat(String.valueOf(i)));
            sme.setTask(define.concat(String.valueOf(i)));
            sme.setDataChannel(Arrays.asList(channels));
            sme.setStatus(String.valueOf(i % 2));
            sme.setCreateTime(new Date());
            sme.setUpdateTime(new Date());
            esService.createSiteInfo(sme);
        }
        return "createSiteInfo success";
    }


    @RequestMapping("/modifySiteInfo")
    @ResponseBody
    public String modifySiteInfo() {
        SiteMonitorEntity sme = null;
        for (int i = 1; i < 2; i++) {
            sme = new SiteMonitorEntity();
            sme.setId("sid_00".concat(String.valueOf(i)));
            sme.setStatus(String.valueOf(3));
            sme.setTask("task_".concat(String.valueOf(i)));
            sme.setUpdateTime(new Date());
            esService.modifySiteInfo(sme);
        }
        return "modifySiteInfo success";
    }


    @RequestMapping(value = "/querySiteInfo", method = RequestMethod.POST)
    @ResponseBody
    public ResponseObject querySiteInfo(@RequestParam(name = "pageNo", required = true) Integer pageNo,
                                @RequestParam(name = "limit", required = true) Integer limit) {
        Map<String, Object> queryMap =  esService.querySiteInfo(pageNo, limit);
        queryMap.put("pageNo", pageNo);
        queryMap.put("limit", limit);
        return ResponseObject.newSuccessResponseObject(queryMap, SystemConstant.REQ_SUCCESS);
    }

    /**
     * 删除文档
     */
    @RequestMapping("/deleteSiteInfo")
    @ResponseBody
    public String deleteSiteInfo() {
        String index = "1";
        SiteMonitorEntity siteMonitor = new SiteMonitorEntity();
        siteMonitor.setId("sid_00".concat(index));
        siteMonitor.setTask("task_".concat(index));
        esService.deleteSiteInfo(siteMonitor);
        return "delete success";
    }
}
