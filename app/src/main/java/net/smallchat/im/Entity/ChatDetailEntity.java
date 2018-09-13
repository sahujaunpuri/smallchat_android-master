package net.smallchat.im.Entity;

import java.io.Serializable;

public class ChatDetailEntity implements Serializable{

	private static final long serialVersionUID = -148538571545L;

	public Login mLogin;
	public int mType = 0;
	
	public ChatDetailEntity(){}
	
	public ChatDetailEntity(Login login, int type){
		this.mLogin = login;
		this.mType = type;
	}
}
