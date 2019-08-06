package com.practice.bus.service;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component
public class HttpAPIService {

	private Logger log = LoggerFactory.getLogger(HttpAPIService.class);

	/*
	 * @Autowired private CloseableHttpClient httpClient;
	 */

	@Autowired
	private RequestConfig config;

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

			// log.info("[ Get请求返回状态码 ]：" +
			// response.getStatusLine().getStatusCode());
			if (null != entity
					&& response.getStatusLine().getStatusCode() == 200) {
				context = EntityUtils.toString(entity, "UTF-8");
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
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
				log.error("[ HttpClient-Get ] 连接流关闭失败.");
				e.printStackTrace();
				return null;
			}
		}

		return context;
	}

	/**
	 *
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String doGet(String url) throws Exception {
		return this.doGet(url, null);
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
			/*
			 * Iterator headerIterator = headerMap.entrySet().iterator();
			 * //循环增加header while(headerIterator.hasNext()){
			 * Entry<String,String> elem = (Entry<String, String>)
			 * headerIterator.next();
			 * post.addHeader(elem.getKey(),elem.getValue()); }
			 */
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
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public String doPost(String url, Map<String, String> param, String tokens) {

		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = null;
		String context = StringUtils.EMPTY;
		try {
			httpPost = new HttpPost(url);
			httpPost.setConfig(config);
			if (param != null) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (String key : param.keySet()) {
					paramList.add(new BasicNameValuePair(key, param.get(key)));
				}

				UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(paramList, "UTF-8");
				/*HttpEntity httpEntity = new StringEntity(paramList.toString(),
						ContentType.APPLICATION_FORM_URLENCODED);*/
				httpPost.setEntity(urlEncodedFormEntity);
			}
			httpPost.addHeader("token", tokens);
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			httpPost.addHeader("Accept", "application/json");

			response = httpClient.execute(httpPost);

			// 获取结果实体
			entity = response.getEntity();
			// log.info("[ Post 请求返回状态码 ]：" +
			// response.getStatusLine().getStatusCode());
			if (null != entity
					&& response.getStatusLine().getStatusCode() == 200) {
				context = EntityUtils.toString(entity, "UTF-8");
			}
			/*
			 * return new HttpResult(response.getStatusLine().getStatusCode(),
			 * EntityUtils.toString( response.getEntity(), "UTF-8"));
			 */
		} catch (ParseException e) {
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
				if (null != httpPost) {
					httpPost.abort();
				}
				httpClient.close();
			} catch (IOException e) {
				log.error("[ HttpClient-Post ] 连接流关闭失败.");
				e.printStackTrace();
				return null;
			}
		}

		return context;
	}

	/**
	 *
	 * 
	 * @param url
	 * @param map
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
			if (param != null) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (String key : param.keySet()) {
					paramList.add(new BasicNameValuePair(key, param.get(key)));
				}

				UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(
						paramList, "UTF-8");

				httpPost.setEntity(urlEncodedFormEntity);
			}
			httpPost.addHeader("Content-Type",
					"application/x-www-form-urlencoded");
			httpPost.addHeader("Accept", "application/json");

			response = httpClient.execute(httpPost);

			// 获取结果实体
			entity = response.getEntity();
			// log.info("[ Post 请求返回状态码 ]：" +
			// response.getStatusLine().getStatusCode());
			if (null != entity
					&& response.getStatusLine().getStatusCode() == 200) {
				context = EntityUtils.toString(entity, "UTF-8");
			}
			/*
			 * return new HttpResult(response.getStatusLine().getStatusCode(),
			 * EntityUtils.toString( response.getEntity(), "UTF-8"));
			 */
		} catch (ParseException e) {
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
				if (null != httpPost) {
					httpPost.abort();
				}
				httpClient.close();
			} catch (IOException e) {
				log.error("[ HttpClient-Post ] 连接流关闭失败.");
				e.printStackTrace();
				return null;
			}
		}

		return context;
	}

	/**
	 * 
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String doPost(String url) throws Exception {
		return this.doPost(url, null);
	}
}