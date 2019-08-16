package com.practice.common.http;

import com.alibaba.fastjson.JSONObject;
import com.practice.config.BigScreenConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;

@Component
public class HttpAPIService {

	private static Logger logger = LoggerFactory.getLogger(HttpAPIService.class);

	@Autowired
	RequestConfig config;

	@Autowired
	BigScreenConfig bigScreenConfig;

	/**
	 * 
	 * 
	 * @param url
	 * @param param
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws Exception
	 */
	public String doGet(String url, Map<String, String> param) {

		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = null;
		String context = StringUtils.EMPTY;
		try {
			URIBuilder builder = new URIBuilder(url);
			if (param != null) {
				for (String key : param.keySet()) {
					builder.addParameter(key, param.get(key));
				}
			}
			URI uri = builder.build();
			httpGet = new HttpGet(uri);
			httpGet.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			httpGet.addHeader("Accept", "application/json");
			httpGet.setConfig(config);
			response = httpClient.execute(httpGet);

			// 获取结果实体
			entity = response.getEntity();
			if (null != entity
					&& response.getStatusLine().getStatusCode() == 200) {
				context = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (URISyntaxException | ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (null != entity) {
					EntityUtils.consume(entity);
				}
				if (null != response) {
					response.close();
				}
				if (null != httpGet) {
					httpGet.abort();
				}
				httpClient.close();
			} catch (Exception e) {
				logger.error("[ HttpClient-Get ] 连接流关闭失败.");
				e.printStackTrace();
			}
		}
		return context;
	}

	public String postMap(String url, Map<String, String> contentMap,
			String token) {
		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost(url);
		List<NameValuePair> content = new ArrayList<NameValuePair>();
		Iterator iterator = contentMap.entrySet().iterator();
		// 将content生成entity
		while (iterator.hasNext()) {
			Entry<String, String> elem = (Entry<String, String>) iterator
					.next();
			content.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
		}
		CloseableHttpResponse response = null;
		try {
			post.addHeader("token", token);
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");
			post.addHeader("Accept", "application/json");
			if (content.size() > 0) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(content,
						"UTF-8");
				post.setEntity(entity);
			}
			response = httpClient.execute(post);
			// 发送请求并接收返回数据
			if (response != null
					&& response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				// 获取response的body部分
				result = EntityUtils.toString(entity);
				// 读取reponse的body部分并转化成字符串
			}
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpClient.close();
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 *
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String doPost(String url, Map<String, String> param) {
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = null;
		String context = StringUtils.EMPTY;
		try {
			httpPost = new HttpPost(url);
			httpPost.setConfig(config);
			this.setHttpPostEntity(param, httpPost);
			String format = "format";
			if (param.get(format) != null) {
				httpPost.addHeader(format, param.get(format));
			}
			httpPost.addHeader("token", param.get("access_token"));
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			httpPost.addHeader("Accept", "application/json");
			response = httpClient.execute(httpPost);
			// 获取结果实体
			entity = response.getEntity();
			if (null != entity
					&& response.getStatusLine().getStatusCode() == 200) {
				context = EntityUtils.toString(entity, "UTF-8");
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			this.httpResourceClose(response, entity, httpClient, httpPost);
		}

		return context;
	}

	private void httpResourceClose(CloseableHttpResponse response, HttpEntity entity, CloseableHttpClient httpClient, HttpPost httpPost) {
		try {
			if (null != entity) {
				EntityUtils.consume(entity);
			}
			if (null != response) {
				response.close();
			}
			if (null != httpPost) {
				httpPost.abort();
			}
			httpClient.close();
		} catch (IOException e) {
			logger.error("[ HttpClient-Post ] 连接流关闭失败.");
			e.printStackTrace();
		}
	}

	private void setHttpPostEntity(Map<String, String> param, HttpPost httpPost) throws UnsupportedEncodingException {
		if (param != null) {
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			for (String key : param.keySet()) {
				if (!key.equals("access_token") && !key.equals("format")) {
					paramList.add(new BasicNameValuePair(key, param.get(key)));
				}
			}
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(
					paramList, "UTF-8");
			httpPost.setEntity(urlEncodedFormEntity);
		}
	}

	public Map<String, Object> callForienApi(String url, Map<String, String> map) {
		Map<String, Object> resMap = new HashMap<>();
		try {
			String tokens = this.doPost(bigScreenConfig.getRooturl().concat(url), map);
			if (tokens != null && !"".equals(tokens)) {
				JSONObject json = JSONObject.parseObject(tokens);
				int status = json.getIntValue("status");
				if (status == 0) {
					if (!StringUtils.isEmpty(json.getString("errcode"))) {
						logger.error("---------------------- query api: {}, occure exception, error: {} --------------------", url, json.getString("errmsg"));
						return resMap;
					}
					JSONObject doc = json.getJSONObject("data");
					resMap.put("result", doc);
					//logger.info("------------------------ query api: {}, result: {} ------------------", url, doc.toString());
				} else {
					logger.info("------------------------ query api: {}, request failure: {} ------------------", url);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("---------------------- query api: {}, occure exception, error: {} --------------------", url, e.getMessage());
		}
		return resMap;
	}

}