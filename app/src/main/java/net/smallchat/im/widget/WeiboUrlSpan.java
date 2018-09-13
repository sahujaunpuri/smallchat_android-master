package net.smallchat.im.widget;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import net.smallchat.im.contact.UserInfoActivity;

public class WeiboUrlSpan extends ClickableSpan {

	public interface OnViewClick
	{
		void onClick(View view);
	}
	
    @Override
	public void updateDrawState(TextPaint ds) {
    	if(mColor == 0){
    		mColor = Color.argb(255, 72, 145, 198);
    	}
		ds.setColor(mColor);
	}

	private String mUrl;
    private Context parentContext;
    private int mType ;
    private int mColor;
    private OnViewClick mOnclik;

    public WeiboUrlSpan(String url, Context context,OnViewClick onclick,int color) {
        mUrl = url;
        parentContext = context;
        mOnclik = onclick;
        mColor = color;
    }
    
    public WeiboUrlSpan(int type, Context context,OnViewClick onclick) {
        parentContext = context;
        mOnclik = onclick;
        mType = type;
        
    }

    @Override
    public void onClick(View widget) {
        if (mUrl.startsWith("@")){
            Bundle bundleUserInfo = new Bundle();
            bundleUserInfo.putLong("user_info", 0);
            bundleUserInfo.putInt("type", 2);
            bundleUserInfo.putString("username", mUrl.substring(1));

            Intent intentInfo = new Intent(parentContext, UserInfoActivity.class);
            intentInfo.putExtras(bundleUserInfo);
            intentInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            parentContext.startActivity(intentInfo);
        }else if (mUrl.startsWith("http://")){
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            parentContext.startActivity(myIntent);
        }
        else if (mUrl.startsWith("#")){
        /*	String[] trend = mUrl.split("#");
        	Intent intent = new Intent();
			intent.setClass(parentContext, TopicWeiboListActivity.class);
			intent.putExtra("topic_text",trend[1]);
			parentContext.startActivity(intent);*/
        	/*;
        	Toast.makeText(parentContext, "WeiboUrlSpan-onClick",Toast.LENGTH_LONG).show();*/
        	/*WeiboCommon.getWeiboData().mTrendString = trend[1];
            Intent trendIntent = new Intent(parentContext, HomeTab.class);
            trendIntent.putExtra("weiboType", WeiboCommon.WEIBO_TYPE_TOPIC);
            parentContext.startActivity(trendIntent);	*/
        }else {
        	if(mOnclik!=null){
        		mOnclik.onClick(widget);
        	}
        }
    }
}
