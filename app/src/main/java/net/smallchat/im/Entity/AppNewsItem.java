package net.smallchat.im.Entity;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;


/**
 * 四八达新闻类
 * @author dongli
 *
 */
public class AppNewsItem  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 新闻id
	 */
	public String id;
	
	/**
	 * 新闻标题
	 */
	public String title;
	
	/**
	 * 新闻内容前几十个字符
	 */
	public String text;
	
	/**
	 * 新闻发布时间
	 */
	public long createtime;
	
	/**
	 * 图片一张
	 */
	public String pic;
	
	/**
	 * 获取新闻的详细内容的url
	 */
	public String url;

	public AppNewsItem() {
		super();
	}

	public AppNewsItem(String id, String title, String text, long createtime,
			String pic, String url) {
		super();
		this.id = id;
		this.title = title;
		this.text = text;
		this.createtime = createtime;
		this.pic = pic;
		this.url = url;
	}
	
	/*public AppNewsItem(JSONObject json) {
		super();
		try {
			if(json == null || json.equals("")){
				return;
			}
			tid = json.getInt("tid");
			title = json.getString("title");
			text = json.getString("text");
			createtime = json.getLong("createtime");
			pic = json.getString("pic");
			url = json.getString("url");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public AppNewsItem(String reqString) {
		super();
		try {
			if(reqString == null || reqString.equals("")){
				return;
			}
			JSONObject json = new JSONObject(reqString);
			if(!json.isNull("tid")){
				tid = json.getInt("tid");
			}
			if(!json.isNull("title")){
				title = json.getString("title");
			}
			if(!json.isNull("text")){
				text = json.getString("text");
			}
			if(!json.isNull("createtime")){
				createtime = json.getLong("createtime");
			}
			if(!json.isNull("pic")){
				pic = json.getString("pic");
			}
			if(!json.isNull("url")){
				url = json.getString("url");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}*/
	
	public static AppNewsItem getInfo(String json) {
		try {
			return JSONObject.parseObject(json, AppNewsItem.class);//toJavaObject(JSONObject.parseObject(json),
					//Card.class);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getInfo(AppNewsItem info) {
		String json = JSONObject.toJSON(info).toString();
		return json;
	}
	
	
	
	
}
