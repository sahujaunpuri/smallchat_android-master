package net.smallchat.im.Entity;

import java.io.Serializable;




public class Session implements Serializable{
	private static final long serialVersionUID = 5389219102904727377L;
	private String fromId;		// 会话来源用户ID
	public int type = 0;
	public int isTop;//session 置顶 序号
	public ChatMessage mChatMessage;
	public String name = "";
	public String heading = "";
	public int mUnreadCount = 0;
	public long lastMessageTime = 0;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fromId == null) ? 0 : fromId.hashCode());
		result = prime * result;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Session other = (Session) obj;
		if (fromId == null) {
			if (other.fromId != null)
				return false;
		} else if (!fromId.equals(other.fromId))
			return false;
		return true;
	}
	
	
	/**
	 * @return the fuid
	 */
	public String getFromId() {
		return fromId;
	}
	
	/**
	 * @param fuid the fromId to set
	 */
	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	
}
