package net.smallchat.im.Entity;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * 名片信息
 * @author dongli
 *
 */
public class MessageCard implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public String uid;
	public String headSmall;
	public String nickname;
	public String content;




	public MessageCard(String userID, String head, String name, String sign) {
		super();
		this.uid = userID;
		this.headSmall = head;
		this.nickname = name;
		this.content = sign;
	}

	public MessageCard() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static MessageCard getInfo(String json) {
		try {
			return JSONObject.parseObject(json, MessageCard.class);//toJavaObject(JSONObject.parseObject(json),
					//Card.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(MessageCard info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}
	
}
