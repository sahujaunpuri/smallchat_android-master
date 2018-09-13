package net.smallchat.im.Entity;

import java.io.Serializable;
import java.util.List;

import android.widget.LinearLayout;

/**
 * 
 * 
 * @author dongli
 *
 */
public class ShowFriendsLoopUser implements  Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public LinearLayout parentLayout;
	public LinearLayout childLayout;
	public List<CommentUser> list;
	public ShowFriendsLoopUser(LinearLayout parentLayout,LinearLayout childLayout,
			List<CommentUser> list) {
		super();
		this.parentLayout = parentLayout;
		this.childLayout = childLayout;
		this.list = list;
	}
	
	
	public ShowFriendsLoopUser(LinearLayout parentLayout,
			LinearLayout childLayout) {
		super();
		this.parentLayout = parentLayout;
		this.childLayout = childLayout;
	}


	public ShowFriendsLoopUser(LinearLayout parentLayout, List<CommentUser> list) {
		super();
		this.parentLayout = parentLayout;
		this.list = list;
	}


	public ShowFriendsLoopUser() {
		super();
	}
	
	
}
