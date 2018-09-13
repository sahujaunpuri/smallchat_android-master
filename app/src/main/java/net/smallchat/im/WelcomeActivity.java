package net.smallchat.im;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import net.smallchat.im.global.IMCommon;

/**
 * 欢迎页面
 * @author dongli
 *
 */
public class WelcomeActivity extends Activity{

	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		mContext = this;
		DisplayMetrics metrics= new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		IMCommon.mScreenWidth =metrics.widthPixels;
		IMCommon.mScreenHeight = metrics.heightPixels;
		new Thread(){
			public void run() {
				Cursor cursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},null, null,
						"sort_key COLLATE LOCALIZED asc");
			};
		}.start();
//		showMainpage();

		SharedPreferences setting = getSharedPreferences(IMCommon.SHOWGUDIEVERSION, 0);
		Boolean user_first = setting.getBoolean("FIRST",true);
		if(user_first){//第一次
			setting.edit().putBoolean("FIRST", false).commit();
			firstShow();//第一次进入时的方法
		}else{
			showMainpage();//以后进入时的方法
		}
	}
	//第一次进入应用的时候显示
	public void firstShow(){//显示指导页面
		Intent intent = new Intent(WelcomeActivity.this,GuideActivity.class);
		WelcomeActivity.this.startActivityForResult(intent,0);
	}
	public void showMainpage(){
		 
		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				
			    Intent intentt = new Intent(WelcomeActivity.this,MainActivity.class);
			    WelcomeActivity.this.startActivity(intentt);
			    WelcomeActivity.this.finish();
			}
		}, 2000);
	 }
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			this.finish();
			System.exit(0);
		}
		return super.dispatchKeyEvent(event);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK){
			Handler handler = new Handler();
			handler.postDelayed(new Runnable(){
				@Override
				public void run() {

					Intent intentt = new Intent(WelcomeActivity.this,MainActivity.class);
					WelcomeActivity.this.startActivity(intentt);
					//WelcomeActivity.this.finish();
				}
			}, 500);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
