package com.practice.common;

public class SystemConstant {

	public static final int RESCODE_REFTOKEN_MSG = 1006;		//刷新TOKEN(有返回数据)
	public static final int RESCODE_REFTOKEN = 1007;			//刷新TOKEN
	
	public static final int JWT_ERRCODE_NULL = 4000;			//Token不存在
	public static final int JWT_ERRCODE_EXPIRE = 4001;			//Token过期
	public static final int JWT_ERRCODE_FAIL = 4002;			//验证不通过

	public static final int REQUEST_FAIL = -1;
	public static final int REQUEST_SUCESS = 0;

	public static final String JWT_SECERT = "8677df7fc3a34e26a61c034d5ec8245d";			//密匙
	public static final long JWT_TTL = 120 * 60 * 1000;									//token有效时间

	public static final String ISS_USER = "economic_daily";


	public static final String REQ_SUCCESS = "请求成功";

	public static final String ERROR_INNER = "内部错误";
	public static final Integer ERROR_INNER_CODE = 500;

	public static final String REQ_ILLEGAL = "非法请求！url不正确，或者参数字段缺失";
	public static final String PARAM_FORMAT_INCOR = "非法请求！api参数格式不正确";
	public static final Integer REQ_ILLEGAL_CODE = -1;

	public static final String PREFIX_MEDIA_SOURCE = "sitemonitor:media-source:";
	public static final String SOURCE_WECHAT = "wechat";
	public static final String SOURCE_WEIBO = "weibo";
	public static final String SOURCE_APP = "app";

}

