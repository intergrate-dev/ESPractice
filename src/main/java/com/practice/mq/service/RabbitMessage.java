package com.practice.mq.service;

import com.practice.bus.bean.DocInfo;
import com.practice.bus.bean.EnumOperation;

import java.io.Serializable;

/**
 * 发给RabbitMQ的消息bean 
 */
public class RabbitMessage implements Serializable {
	private EnumOperation operation;
	private DocInfo docInfo;
	public EnumOperation getOperation() {
		return operation;
	}
	public void setOperation(EnumOperation operation) {
		this.operation = operation;
	}
	public DocInfo getDocInfo() {
		return docInfo;
	}
	public void setDocInfo(DocInfo docInfo) {
		this.docInfo = docInfo;
	}
}
