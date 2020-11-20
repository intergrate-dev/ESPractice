package com.practice.bus.controller;

import com.practice.bus.bean.param.MediaStatsParam;
import com.practice.bus.service.ApiService;
import com.practice.common.ResponseObject;
import com.practice.common.Constant;
import com.practice.common.annotation.ApiCheck;
import com.practice.es.service.ESService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/api")
public class ApiController {

    private static Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    ApiService apiService;

    @Autowired
    ESService esService;

    /**
     * 媒体机构关联的信源（微信公众号）
     */
    @RequestMapping(value = "/mediaSource", method = RequestMethod.POST)
    @ResponseBody
    public ResponseObject mediaSource(@RequestParam(name = "pageNo", required = false) Integer pageNo,
                               @RequestParam(name = "limit", required = false) Integer limit,
                               @RequestParam(name = "types", required = true) List<String> types,
                               @RequestParam(name = "mediaId", required = true) String mediaId) {
        Map<String, Object> queryMap =  null;
        try {
            // types: wechat,weibo
            /*queryMap = apiService.queryMediaSrouce(pageNo, limit, types, mediaId);
            queryMap.put("mediaId", mediaId);
            apiService.cacheMediaSource(queryMap);*/
        } catch (Exception e) {
            logger.error("----------------------- 获取站点信息失败！， error: {} --------------------------", e.getMessage());
            e.printStackTrace();
            return ResponseObject.newErrorResponseObject(Constant.REQ_ILLEGAL_CODE, "获取站点更新信息失败！");
        }
        return ResponseObject.newSuccessResponseObject(queryMap, Constant.REQ_SUCCESS);
    }

    /**
     * 查询文档
     */
    @RequestMapping(value = "/getMediaArticles", method = RequestMethod.POST)
    @ResponseBody
    public ResponseObject getMediaArticles(@RequestParam(name = "pageNo", required = false) Integer pageNo,
                               @RequestParam(name = "limit", required = false) Integer limit,
                               @RequestParam(name = "types", required = true) List<String> types,
                               @RequestParam(name = "mediaId", required = true) String mediaId) {

        Map<String, Object> queryMap =  null;
        try {
            //queryMap = apiService.getMediaArticles(pageNo, limit, types, mediaId);
        } catch (Exception e) {
            logger.error("----------------------- 获取站点信息失败！， error: {} --------------------------", e.getMessage());
            e.printStackTrace();
            return ResponseObject.newErrorResponseObject(Constant.REQ_ILLEGAL_CODE, "获取站点更新信息失败！");
        }
        return ResponseObject.newSuccessResponseObject(queryMap, Constant.REQ_SUCCESS);
    }

    /**
     * 上一周微信公众号互动数据查询
     * 目前只需根据配置的微信公众号检索发稿统计
     * 增加配置项，可扩展支持查询媒体所关联公众号对应发稿统计
     */
    @RequestMapping(value = "/queryMediaStats", method = RequestMethod.POST)
    @ResponseBody
    @CrossOrigin("*")
    @ApiCheck
    public ResponseObject queryMediaStats(@Valid MediaStatsParam param) {

        Map<String, Object> queryMap =  null;
        try {
            // TODO api token
            queryMap = apiService.queryMediaStats(param);
        } catch (Exception e) {
            logger.error("----------------------- 获取信源互动统计数据失败！， error: {} --------------------------", e.getMessage());
            e.printStackTrace();
            return ResponseObject.newErrorResponseObject(Constant.REQ_ILLEGAL_CODE, "查询互动统计数据失败！");
        }
        logger.info("------------------------ queryMediaStats, return datas  --------------------");
        return ResponseObject.newSuccessResponseObject(queryMap, Constant.REQ_SUCCESS);
    }

}
