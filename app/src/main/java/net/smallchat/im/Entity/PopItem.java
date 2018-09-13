package net.smallchat.im.Entity;

import java.io.Serializable;

import net.smallchat.im.global.FeatureFunction;

/**
 * popwindow 子项
 * @author dongli
 *
 */
public class PopItem implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public String option;
	public int resource_id;
	public boolean isVisible;
	public PopItem(int id, String menu_name,String resource_name) {
		super();
		this.id = id;
		this.option = menu_name;
		if(resource_name!=null && !resource_name.equals("")){
			this.resource_id = FeatureFunction.getSourceIdByName(resource_name);
		}
	}
	public PopItem(int id, String menu_name) {
		super();
		this.id = id;
		this.option = menu_name;

	}
	public PopItem() {
		super();
	}
	public PopItem(int id) {
		super();
		this.id = id;
	}



}
