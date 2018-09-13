package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class MessageRedPacket implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public String uid;
	public String hid;
	public int type;//红包类型 0  拼手气红包 1 固定额度红包
	public int packageCount;//红包数量
	public String time;
	public String remark;
	public Double amount;
	public String payType;
	public String payNumber;
	public String payId;
	public int  status;//红包状态


	public MessageRedPacket(String uid,String hid, Double amount ,int packageCount,int type,int status, String payType, String payNumber, String payId, String time, String remark) {
		super();
		this.uid=uid;
		this.time = time;
		this.remark = remark;
		this.hid = hid;
		this.amount=amount;
		this.payId=payId;
		this.payType=payType;
		this.payNumber=payNumber;
		this.type=type;
		this.packageCount=packageCount;
		this.status=status;
	}

	public MessageRedPacket() {
		super();
	}

	public static MessageRedPacket getInfo(String json) {
		try {
			return JSONObject.parseObject(json, MessageRedPacket.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MessageRedPacket info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}

}
