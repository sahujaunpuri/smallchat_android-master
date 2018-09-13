package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class MessageTransfer implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public String uid="";
	public String time="";
	public String remark ="";
	public String tid ="";
	public Double amount=0.0;
	public String payType="";
	public String payNumber="";
	public String payId="";
	public int status=0;






	public MessageTransfer(String uid,String tid,Double amount ,String payType,String payNumber,String payId,String time, String remark,int status) {
		super();
		this.uid=uid;
		this.status=status;
		this.time = time;
		this.remark = remark;
		this.tid = tid;
		this.amount=amount;
		this.payId=payId;
		this.payType=payType;
		this.payNumber=payNumber;
	}

	public MessageTransfer() {
		super();
	}

	public static MessageTransfer getInfo(String json) {
		try {
			return JSONObject.parseObject(json, MessageTransfer.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MessageTransfer info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}

}
