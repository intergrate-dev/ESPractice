package com.practice.bus.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.practice.es.service.ESService;
import com.practice.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.practice.bus.bean.DocInfo;
import com.practice.bus.service.DocService;

import java.util.HashMap;
import java.util.Map;

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
		for(long i = 0;i<1;i++) {
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
	public String mappingSiteInfo(){

		String path = DocController.class.getClassLoader().getResource("mapping-siteMonitor.json").getPath();
		String s = JsonUtil.readJsonFile(path);
		JSONObject json = JSON.parseObject(s);

		esService.mappingSiteInfo(json);
		return "delete success";
	}

	@RequestMapping("/createSiteInfo")
	@ResponseBody
	public String createSiteInfo() {

		esService.createSiteInfo(null);
		return "delete success";
	}


	@RequestMapping("/modifySiteInfo")
	@ResponseBody
	public String modifySiteInfo() {

		esService.modifySiteInfo(null);
		return "delete success";
	}


	@RequestMapping("/querySiteInfo")
	@ResponseBody
	public String querySiteInfo() {

		esService.querySiteInfo();
		return "delete success";
	}
}
