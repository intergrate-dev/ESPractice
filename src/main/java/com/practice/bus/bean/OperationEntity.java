package com.practice.bus.bean;

public class OperationEntity {
	private String  SITE_NAME;            
	private String SITE_ID; 
	private String SITE_TYPE;
	private String USER_BEHAVIOR_ID;
	private String pageType;      
	private String pageTypeID;         
	private String MEDIA_ID;     
	private String ConfigItem;
	private String Configure; 
	private int Sdisplay;    
	private String extfileds;
	
	
	public String getUSER_BEHAVIOR_ID() {
		return USER_BEHAVIOR_ID;
	}
	public void setUSER_BEHAVIOR_ID(String uSER_BEHAVIOR_ID) {
		USER_BEHAVIOR_ID = uSER_BEHAVIOR_ID;
	}
	public String getSITE_TYPE() {
		return SITE_TYPE;
	}
	public void setSITE_TYPE(String sITE_TYPE) {
		SITE_TYPE = sITE_TYPE;
	}
	public String getSITE_NAME() {
		return SITE_NAME;
	}
	public void setSITE_NAME(String sITE_NAME) {
		SITE_NAME = sITE_NAME;
	}
	public String getSITE_ID() {
		return SITE_ID;
	}
	public void setSITE_ID(String sITE_ID) {
		SITE_ID = sITE_ID;
	}
	public String getPageType() {
		return pageType;
	}
	public void setPageType(String pageType) {
		this.pageType = pageType;
	}
	public String getPageTypeID() {
		return pageTypeID;
	}
	public void setPageTypeID(String pageTypeID) {
		this.pageTypeID = pageTypeID;
	}
	public String getMEDIA_ID() {
		return MEDIA_ID;
	}
	public void setMEDIA_ID(String mEDIA_ID) {
		MEDIA_ID = mEDIA_ID;
	}
	public String getConfigItem() {
		return ConfigItem;
	}
	public void setConfigItem(String configItem) {
		ConfigItem = configItem;
	}
	public String getConfigure() {
		return Configure;
	}
	public void setConfigure(String configure) {
		Configure = configure;
	}
	public int getSdisplay() {
		return Sdisplay;
	}
	public void setSdisplay(int sdisplay) {
		Sdisplay = sdisplay;
	}
	public String getExtfileds() {
		return extfileds;
	}
	public void setExtfileds(String extfileds) {
		this.extfileds = extfileds;
	}
	
}
