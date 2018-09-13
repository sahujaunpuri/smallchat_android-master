package net.smallchat.im.Entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class PageInfo implements Serializable{

	private static final long serialVersionUID = 146435615132L;
	/**
	 * 	当前页
	 */
	public int currentPage;
	/**
	 * 一共有好多页
	 */
	public int totalPage;
	/**
	 * 一共有好多条数据
	 */
	public int totalCount;
	/**
	 * 一页有好多条数据
	 */
	public int pageSize;
	
	/**
	 * 是否还有下一页数据
	 */
	public int hasMore;
	
	//public int pageNext;


	public PageInfo(JSONObject json){
		try {
			currentPage = json.getInt("page");//
			totalPage = json.getInt("pageCount");//
			//Log.e("PageInfo", "currentPage:"+currentPage+",totalPage:"+totalPage);
			//"pageInfo":{"total":"30","count":20,"pageCount":2,"page":1}}
			
			totalCount = json.getInt("total");//
			if(!json.isNull("count")){
				pageSize = json.getInt("count");//
			}else if(!json.isNull("pageSize")){
				pageSize = json.getInt("pageSize");//
			}
			if(!json.isNull("hasMore")){
				this.hasMore = json.getInt("hasMore");
			}
		
		/*	if(!json.isNull("pageNext")){
				pageNext = json.getInt("pageNext");
			}*/
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
