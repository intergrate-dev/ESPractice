package com.practice.bus.dao;


import com.practice.bus.bean.OperationEntity;

import java.util.List;

public interface LiangHuiNewsDao {
	
	public List<OperationEntity> queryLiangHuiByConfig(int pageId);

}
