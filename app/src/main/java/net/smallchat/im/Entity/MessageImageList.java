package net.smallchat.im.Entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author dongli
 *
 */
public class MessageImageList implements Serializable{

	/**
	 *
	 */
	private  List<Picture> pictureList=new ArrayList<Picture>();
	private int id=0;
	private String nickname;
	private String uid;

	private static final long serialVersionUID = 1L;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public MessageImageList() {
		super();
	}

	public List<Picture> getPictureList() {
		return pictureList;
	}

	public void setPictureList(List<Picture> pictureList) {
		this.pictureList = pictureList;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
