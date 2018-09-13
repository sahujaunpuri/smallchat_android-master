package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FriendsLoopItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 259457862436629649L;
	public int id;
	public String uid;
	public String nickname;
	public String headSmall;
	public String content;
	public List<Picture> listpic;

	public double lng;
	public double lat;
	public String address;
	public int replys;
	public int praises;
	public int favorite;
	public long createtime;
	public int ispraise;
	public List<CommentUser> replylist;
	public List<CommentUser> praiselist;
	public int showView;

	public FriendsLoopItem() {
		super();
	}
	
	

	public FriendsLoopItem(long createtime) {
		super();
		this.createtime = createtime;
	}



	public FriendsLoopItem(JSONObject json) {
		super();
		if(json == null ){
			return;
		}

		try {
			id = json.getInt("id");
			uid = json.getString("uid");
			nickname = json.getString("nickname");
			headSmall = json.getString("headSmall");
			content = json.getString("content");
			if(!json.isNull("picture")){
				String pictureString = json.getString("picture");
				if(pictureString!=null && !pictureString.equals("")
						&& pictureString.startsWith("[")){
					JSONArray array = json.getJSONArray("picture");
					if(array!=null && array.length()>0){
						listpic = new ArrayList<Picture>();
						for (int i = array.length()-1; i >=0; i--) {
							listpic.add(Picture.getInfo(array.getString(i)));
						}
					}
				}
			}
			try {
				lng = json.getDouble("lng");
				lat = json.getDouble("lat");
			}catch (Exception ex){

			}

			address = json.getString("address");
try {
	praises = json.getInt("praises");
	replys = json.getInt("replys");
	ispraise = json.getInt("ispraise");
}catch (Exception ex){

}

			createtime = json.getLong("createtime");
			if(!json.isNull("replylist")){
				String replyString = json.getString("replylist");
				if(replyString!=null && !replyString.equals("")
						&& replyString.startsWith("[")){
					JSONArray array = json.getJSONArray("replylist");
					if(array!=null && array.length()>0){
						replylist = new ArrayList<CommentUser>();
						for (int i = array.length()-1; i >=0  ; i--) {
							replylist.add(new CommentUser(array.getJSONObject(i)));
						}

					}
				}

			}
			if(!json.isNull("praiselist")){
				String replyString = json.getString("praiselist");
				if(replyString!=null && !replyString.equals("")
						&& replyString.startsWith("[")){
					JSONArray array = json.getJSONArray("praiselist");
					if(array!=null && array.length()>0){
						praiselist = new ArrayList<CommentUser>();
						for (int i = array.length()-1; i >=0; i--) {
							praiselist.add(new CommentUser(array.getJSONObject(i)));
						}

					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public FriendsLoopItem(String reqString) {
		super();
		if(reqString == null || reqString.equals("")){
			return;
		}

		try {
			JSONObject parentJson = new JSONObject(reqString);
			if(!parentJson.isNull("data")){
				JSONObject json = parentJson.getJSONObject("data");

				id = json.getInt("id");
				uid = json.getString("uid");
				nickname = json.getString("nickname");
				headSmall = json.getString("headSmall");
				content = json.getString("content");
				if(!json.isNull("picture")){
					String pictureString = json.getString("picture");
					if(pictureString!=null && !pictureString.equals("")
							&& pictureString.startsWith("[")){
						JSONArray array = json.getJSONArray("picture");
						if(array!=null && array.length()>0){
							listpic = new ArrayList<Picture>();
							for (int i = 0; i < array.length(); i++) {
								listpic.add(Picture.getInfo(array.getString(i)));
							}
						}
					}
				}
				try {
					lng = json.getDouble("lng");
				}catch (Exception ex){
					lng=0;
				}
				try {
					lat = json.getDouble("lat");
				}catch (Exception ex){
					lat=0;
				}
				address = json.getString("address");
				praises = json.getInt("praises");
				replys = json.getInt("replys");
				ispraise = json.getInt("ispraise");
				createtime = json.getLong("createtime");
				if(!json.isNull("data")){
					JSONObject childObj = json.getJSONObject("data");
					if(!childObj.isNull("replylist")){
						String replyString = childObj.getString("replylist");
						if(replyString!=null && !replyString.equals("")
								&& replyString.startsWith("[")){
							JSONArray array = childObj.getJSONArray("replylist");
							if(array!=null && array.length()>0){
								replylist = new ArrayList<CommentUser>();
								for (int i = array.length()-1; i >=0  ; i--) {
									replylist.add(new CommentUser(array.getJSONObject(i)));
								}
								

							}
						}

					}
					if(!childObj.isNull("praiselist")){
						String replyString = childObj.getString("praiselist");
						if(replyString!=null && !replyString.equals("")
								&& replyString.startsWith("[")){
							JSONArray array = childObj.getJSONArray("praiselist");
							if(array!=null && array.length()>0){
								praiselist = new ArrayList<CommentUser>();
								for (int i = array.length()-1; i >=0  ; i--) {
									praiselist.add(new CommentUser(array.getJSONObject(i)));
								}

							}
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}



}
