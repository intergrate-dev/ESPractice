package com.practice.bus.controller;

import com.practice.bus.service.ApiService;
import com.practice.common.ResponseObject;
import com.practice.common.SystemConstant;
import com.practice.es.service.ESService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


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
            queryMap = apiService.queryMediaSrouce(pageNo, limit, types, mediaId);
            queryMap.put("mediaId", mediaId);
            apiService.cacheMediaSource(queryMap);
        } catch (Exception e) {
            logger.error("----------------------- 获取站点信息失败！， error: {} --------------------------", e.getMessage());
            e.printStackTrace();
            return ResponseObject.newErrorResponseObject(SystemConstant.REQ_ILLEGAL_CODE, "获取站点更新信息失败！");
        }
        return ResponseObject.newSuccessResponseObject(queryMap, SystemConstant.REQ_SUCCESS);
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
            //TODO task
            queryMap = apiService.getMediaArticles(pageNo, limit, types, mediaId);
        } catch (Exception e) {
            logger.error("----------------------- 获取站点信息失败！， error: {} --------------------------", e.getMessage());
            e.printStackTrace();
            return ResponseObject.newErrorResponseObject(SystemConstant.REQ_ILLEGAL_CODE, "获取站点更新信息失败！");
        }
        return ResponseObject.newSuccessResponseObject(queryMap, SystemConstant.REQ_SUCCESS);
    }


    /**
     *
     * 获取配置项，启动
     */
    @RequestMapping(value = "/sourceInit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseObject sourceInit(@RequestParam(name = "mediaId", required = true) String mediaId,
                                          @RequestParam(name = "ids", required = true) String ids,
                                          @RequestParam(name = "names", required = true) String names,
                                          @RequestParam(name = "types", required = true) String types) {

        Map<String, Object> queryMap =  null;
        try {
            //types: wechat
            apiService.sourceInit(mediaId, ids, names, Arrays.asList(types));
        } catch (Exception e) {
            logger.error("----------------------- 获取站点信息失败！， error: {} --------------------------", e.getMessage());
            e.printStackTrace();
            return ResponseObject.newErrorResponseObject(SystemConstant.REQ_ILLEGAL_CODE, "获取信息失败！");
        }
        return ResponseObject.newSuccessResponseObject(queryMap, SystemConstant.REQ_SUCCESS);
    }

    /**
     * 上一周微信公众号互动数据查询
     * 目前只需根据配置的微信公众号检索发稿统计
     * 增加配置项，可扩展支持查询媒体所关联公众号对应发稿统计
     */
    @RequestMapping(value = "/queryMediaStats", method = RequestMethod.POST)
    @ResponseBody
    public ResponseObject queryMediaStats(@RequestParam(name = "mediaId", required = true) String mediaId,
                                          @RequestParam(name = "ids", required = true) String ids,
                                          @RequestParam(name = "names", required = true) String names,
                                          @RequestParam(name = "types", required = true) String types) {

        Map<String, Object> queryMap =  null;
        try {
            //types: wechat
            queryMap = apiService.queryMediaStats(mediaId);
        } catch (Exception e) {
            logger.error("----------------------- 获取站点信息失败！， error: {} --------------------------", e.getMessage());
            e.printStackTrace();
            return ResponseObject.newErrorResponseObject(SystemConstant.REQ_ILLEGAL_CODE, "获取站点更新信息失败！");
        }
        return ResponseObject.newSuccessResponseObject(queryMap, SystemConstant.REQ_SUCCESS);
    }
}
