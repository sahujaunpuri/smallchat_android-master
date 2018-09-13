package net.smallchat.im.widget;

import android.content.res.AssetManager;

public class FlowTag {
	private int flowId;
	private String fileName;
	private String mThumbUrl;
	public final int what = 1;

	public int getFlowId() {
		return flowId;
	}

	public void setFlowId(int flowId) {
		this.flowId = flowId;
	}

	public String getFileName() {
		return fileName;
	}
	
	public String getThumbPicUrl(){
		return mThumbUrl;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setThumbUrl(String thumbUrl){
		mThumbUrl = thumbUrl;
	}

	private AssetManager assetManager;
	private int ItemWidth;

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public void setAssetManager(AssetManager assetManager) {
		this.assetManager = assetManager;
	}

	public int getItemWidth() {
		return ItemWidth;
	}

	public void setItemWidth(int itemWidth) {
		ItemWidth = itemWidth;
	}
}
