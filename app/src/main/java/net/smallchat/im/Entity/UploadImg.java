package net.smallchat.im.Entity;

import java.io.Serializable;

public class UploadImg implements Serializable{

	private static final long serialVersionUID = -148538571545L;

	public String mPicPath;
	public int mType = 0;
	
	public UploadImg(){}
	
	public UploadImg(String path, int type){
		this.mPicPath = path;
		this.mType = type;
	}
	
}
