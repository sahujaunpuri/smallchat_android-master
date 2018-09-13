package net.smallchat.im;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.widget.CustomProgressDialog;

/**
 * 所有页面的父类
 * @author dongli
 *
 */
public class BaseActivity extends FragmentActivity implements OnClickListener{
	
	/*
	 * 定义全局变量
	 */
	public final static String SUB_DETAIL_CHANGE = "com.qiyue.subscription_detail_change";

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerFinishCurrentPageWorkMonitor();
	}
	
	
	/**
	 * 注册通知事件
	 */
	public void registerFinishCurrentPageWorkMonitor() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_DESTROY_CURRENT_ACTIVITY);
		registerReceiver(mReceiver, filter);
	}
	
	/*
	 * 处理页面公用通知
	 */
	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent == null){
				return;
			}//销毁整个运用
			if (intent.getAction().equals(GlobalParam.ACTION_DESTROY_CURRENT_ACTIVITY)) {
				String name = ((Activity)mContext).getLocalClassName();
				if(!name.equals("ChatsTab")&& !name.equals("ContactsTab")&& !name.equals("ContactAddrActivity")
					&& !name.equals("ContactPinLiActivity")	&& !name.equals("ContactAddTimeActivity")
					&& !name.equals("ContactProjectActivity") && !name.equals("ContactSubjectActivity")
					&& !name.equals("FindTab") && !name.equals("ProfileTab")
					&& !name.equals("SubTab")){
					((Activity)mContext).finish();
					
				}
				
			}
		}
	};
	
	
	public final static int BASE_SHOW_PROGRESS_DIALOG  = 0x11112;
    public final static int BASE_HIDE_PROGRESS_DIALOG  = 0x11113;
    public final static int BASE_MSG_NETWORK_ERROR = 11114;
    public final static int BASE_MSG_TIMEOUT_ERROR = 11115;
	protected CustomProgressDialog mProgressDialog;
   
	protected Context mContext;
	
	/*
	 * 处理整个运用公用消息
	 */
	 public Handler mBaseHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case BASE_SHOW_PROGRESS_DIALOG://显示提示框
                String dialogMsg = (String)msg.obj;
                showProgressDialog(dialogMsg);
                break;
            case BASE_HIDE_PROGRESS_DIALOG://隐藏提示框
                hideProgressDialog();
                String hintMsg = (String)msg.obj;
                if(hintMsg!=null && !hintMsg.equals("")){
                	Toast.makeText(mContext,hintMsg,Toast.LENGTH_LONG).show();
                }
                break;
            case BASE_MSG_NETWORK_ERROR://网络连接错误
            	Toast.makeText(mContext,R.string.network_error,Toast.LENGTH_LONG).show();
            	  hideProgressDialog();
            	break;
            case BASE_MSG_TIMEOUT_ERROR://连接网络超时
            	  hideProgressDialog();
            	  String timeOutMsg = (String)msg.obj;
            	  Toast.makeText(mContext, timeOutMsg, Toast.LENGTH_LONG).show();
            	break;
            }
        }
    };
	    
    public void showProgressDialog(String msg,Context context){
		mProgressDialog = new CustomProgressDialog(mContext);
		mProgressDialog.setMessage(msg);
		mProgressDialog.show();
	}
	public void showProgressDialog(String msg){
		mProgressDialog = new CustomProgressDialog(mContext);
		mProgressDialog.setMessage(msg);
		mProgressDialog.show();
	}
	
	public void hideProgressDialog(){
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	/**+++ for title bar +++*/
    protected ImageView mLeftIcon, mRightBtn,mSearchBtn,mAddBtn,mMoreBtn,mPrivacyModeBtn;
    //protected LinearLayout mRightBtn;
    protected TextView titileTextView,mFristTitlte,mTrowTitle,mRightTextBtn;
    protected RelativeLayout mFirstLayout;
    protected LinearLayout mLeftBtn,mCenterLayout;


    protected Boolean  mPrivacyMode=false;//阅后即焚模式
    protected Boolean  mEncryptMode=false;//消息加密模式

    protected void setTitleContent(int left_src_id, int right_src_id, int title_id){
        mLeftBtn = (LinearLayout)findViewById(R.id.left_btn);
        mLeftIcon = (ImageView)findViewById(R.id.left_icon);
        mRightBtn = (ImageView)findViewById(R.id.right_btn);
        mPrivacyModeBtn = (ImageView)findViewById(R.id.privacy_mode_btn);
        mRightTextBtn = (TextView)findViewById(R.id.right_text_btn);
        //mRightBtn = (LinearLayout)findViewById(R.tid.right_btn);
        mSearchBtn = (ImageView)findViewById(R.id.search_btn);
        mAddBtn = (ImageView)findViewById(R.id.add_btn);
        mMoreBtn = (ImageView)findViewById(R.id.more_btn);
        
        titileTextView = (TextView)findViewById(R.id.title);
        
        if (left_src_id != 0) {
        	mLeftIcon.setImageResource(left_src_id);
        }
        
        if (right_src_id != 0) {
        	mRightBtn.setImageResource(right_src_id);
        	mRightBtn.setVisibility(View.VISIBLE);
        }
        
        if (title_id != 0) {
            titileTextView.setText(title_id);
        }
    }
    //设置隐私模式
    protected void setPrivacyMode(Boolean flag,Boolean isShow){

        mPrivacyModeBtn = (ImageView)findViewById(R.id.privacy_mode_btn);
        if(isShow)
        {
            mPrivacyModeBtn.setVisibility(View.VISIBLE);
        }else {
            mPrivacyModeBtn.setVisibility(View.GONE);
        }
        if (flag) {
            mPrivacyModeBtn.setImageResource(R.drawable.privacy_mode_btn_open);
            mPrivacyMode=true;

        }else {
            mPrivacyModeBtn.setImageResource(R.drawable.privacy_mode_btn_close);
            mPrivacyMode=false;

        }
    }

    //设置隐私模式
    protected Boolean getPrivacyMode(){

        return mPrivacyMode;

    }


    //设置加密模式
    protected Boolean getEncryptMode(){

        return mEncryptMode;

    }

    protected void setTrowMenuTitleContent(int left_src_id, int right_src_id, 
    		String firstTitlte,String trowTitle){
        mLeftBtn = (LinearLayout)findViewById(R.id.left_btn);
        mLeftIcon = (ImageView)findViewById(R.id.left_icon);
        mRightBtn = (ImageView)findViewById(R.id.right_btn);
        mRightTextBtn = (TextView)findViewById(R.id.right_text_btn);
        //mRightBtn = (LinearLayout)findViewById(R.tid.right_btn);
        mSearchBtn = (ImageView)findViewById(R.id.search_btn);
        mAddBtn = (ImageView)findViewById(R.id.add_btn);
        mMoreBtn = (ImageView)findViewById(R.id.more_btn);
        
        titileTextView = (TextView)findViewById(R.id.title);
        titileTextView.setVisibility(View.GONE);
        
        mFristTitlte= (TextView)findViewById(R.id.other_title);
        mTrowTitle= (TextView)findViewById(R.id.child_title);
        if(firstTitlte!=null && !firstTitlte.equals("")){
        	mFristTitlte.setText(firstTitlte);
        	mFristTitlte.setVisibility(View.VISIBLE);
        }
        if(trowTitle!=null && !trowTitle.equals("")){
        	mTrowTitle.setText(trowTitle);
        	mTrowTitle.setVisibility(View.VISIBLE);
        }
        if (left_src_id != 0) {
        	mLeftIcon.setImageResource(left_src_id);
        }
        
        if (right_src_id != 0) {
        	mRightBtn.setImageResource(right_src_id);
        	mRightBtn.setVisibility(View.VISIBLE);
        }
        
       
    }
    
    protected void setTitleContent(int left_src_id,boolean isShowSearch, int right_src_id, int title_id){
        mLeftBtn = (LinearLayout)findViewById(R.id.left_btn);
        mLeftIcon = (ImageView)findViewById(R.id.left_icon);
        mRightBtn = (ImageView)findViewById(R.id.right_btn);
        mRightTextBtn = (TextView)findViewById(R.id.right_text_btn);
        //mRightBtn = (LinearLayout)findViewById(R.tid.right_btn);
        mSearchBtn = (ImageView)findViewById(R.id.search_btn);
        mAddBtn = (ImageView)findViewById(R.id.add_btn);
        mMoreBtn = (ImageView)findViewById(R.id.more_btn);
        
        titileTextView = (TextView)findViewById(R.id.title);
        
        if (left_src_id != 0) {
        	mLeftIcon.setImageResource(left_src_id);
        }
        
        if(isShowSearch){
        	mSearchBtn.setVisibility(View.VISIBLE);
        }
        
        if (right_src_id != 0) {
        	mRightBtn.setImageResource(right_src_id);
        	mRightBtn.setVisibility(View.VISIBLE);
        }
        
        if (title_id != 0) {
            titileTextView.setText(title_id);
        }
    }
    
    protected void setTitleContent(int left_src_id, boolean showSearchIcon,
    		boolean showAddIcon,boolean showMoreIcon,int title_id){
        mLeftBtn = (LinearLayout)findViewById(R.id.left_btn);
        mLeftIcon = (ImageView)findViewById(R.id.left_icon);
        mRightBtn = (ImageView)findViewById(R.id.right_btn);
        mRightTextBtn = (TextView)findViewById(R.id.right_text_btn);
        //mRightBtn = (LinearLayout)findViewById(R.tid.right_btn);
        mSearchBtn = (ImageView)findViewById(R.id.search_btn);
        mAddBtn = (ImageView)findViewById(R.id.add_btn);
        mMoreBtn = (ImageView)findViewById(R.id.more_btn);
        titileTextView = (TextView)findViewById(R.id.title);
        mCenterLayout = (LinearLayout)findViewById(R.id.center_layout);
        
        if (left_src_id != 0) {
        	mLeftIcon.setImageResource(left_src_id);
        }
        if(showSearchIcon){
        	mSearchBtn.setVisibility(View.VISIBLE);
        }
       
        if(showAddIcon){
        	mAddBtn.setVisibility(View.VISIBLE);
        }
        if(showMoreIcon){
        	mMoreBtn.setVisibility(View.VISIBLE);
        }
        
        
        if (title_id != 0) {
            titileTextView.setText(title_id);
        }
    }
    
    
    protected void setRightTextTitleContent(int left_src_id, String right_src_id, int title_id){
        mLeftBtn = (LinearLayout)findViewById(R.id.left_btn);
        mLeftIcon = (ImageView)findViewById(R.id.left_icon);
        mRightBtn = (ImageView)findViewById(R.id.right_btn);
        mRightTextBtn = (TextView)findViewById(R.id.right_text_btn);
        //mRightBtn = (LinearLayout)findViewById(R.tid.right_btn);
        mSearchBtn = (ImageView)findViewById(R.id.search_btn);
        mAddBtn = (ImageView)findViewById(R.id.add_btn);
        mMoreBtn = (ImageView)findViewById(R.id.more_btn);
        
        titileTextView = (TextView)findViewById(R.id.title);
        
        if (left_src_id != 0) {
        	mLeftIcon.setImageResource(left_src_id);
        }
        
        if (right_src_id != null && right_src_id.equals("")) {
        	mRightTextBtn.setText(right_src_id);
        	mRightTextBtn.setVisibility(View.VISIBLE);
        }
        
        if (title_id != 0) {
            titileTextView.setText(title_id);
        }
    }
    
    
    protected void setRightTextTitleContent(int left_src_id, int right_src_id, int title_id){
        mLeftBtn = (LinearLayout)findViewById(R.id.left_btn);
        mLeftIcon = (ImageView)findViewById(R.id.left_icon);
        mRightBtn = (ImageView)findViewById(R.id.right_btn);
        mRightTextBtn = (TextView)findViewById(R.id.right_text_btn);
        //mRightBtn = (LinearLayout)findViewById(R.tid.right_btn);
        mSearchBtn = (ImageView)findViewById(R.id.search_btn);
        mAddBtn = (ImageView)findViewById(R.id.add_btn);
        mMoreBtn = (ImageView)findViewById(R.id.more_btn);
        
        titileTextView = (TextView)findViewById(R.id.title);
        
        if (left_src_id != 0) {
        	mLeftIcon.setImageResource(left_src_id);
        }
        
        if (right_src_id != 0) {
        	mRightTextBtn.setText(right_src_id);
        	mRightTextBtn.setVisibility(View.VISIBLE);
        }
        
        if (title_id != 0) {
            titileTextView.setText(title_id);
        }
    }
    
    protected void setTitleContent(int left_src_id, int right_src_id, String title_text){
        mLeftBtn = (LinearLayout)findViewById(R.id.left_btn);
        mLeftIcon = (ImageView)findViewById(R.id.left_icon);
        mRightBtn = (ImageView)findViewById(R.id.right_btn);
        mRightTextBtn = (TextView)findViewById(R.id.right_text_btn);
        mSearchBtn = (ImageView)findViewById(R.id.search_btn);
        mAddBtn = (ImageView)findViewById(R.id.add_btn);
        mMoreBtn = (ImageView)findViewById(R.id.more_btn);
        titileTextView = (TextView)findViewById(R.id.title);
       // mRightBtn = (LinearLayout)findViewById(R.tid.right_btn);
        
        if (left_src_id != 0) {
        	mLeftIcon.setImageResource(left_src_id);
        }
        
        if (right_src_id != 0) {
        	mRightBtn.setImageResource(right_src_id);
        	mRightBtn.setVisibility(View.VISIBLE);
        }
        
        if (title_text != null && !title_text.equals("")) {
            titileTextView.setText(title_text);
        }
    }
    
    /**--- for title bar ---*/
    @Override
    protected void onStop() {
        super.onStop();
        IMCommon.appOnStop(mContext);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IMCommon.appOnResume(mContext);
    }
    
   /*
    * 按钮点击事件
    * (non-Javadoc)
    * @see android.view.View.OnClickListener#onClick(android.view.View)
    */
	@Override
	public void onClick(View v) {
		if(getCurrentFocus()!=null && getCurrentFocus().getWindowToken()!=null){
			InputMethodManager manager= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);  
		}
	}


	/*
	 * 页面销毁时释放通知
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mReceiver!=null){
			unregisterReceiver(mReceiver);
		}
	}

    public void setMiniBrowserTitle(String title) {
        if(title!=null) {
            if (titileTextView == null)
                titileTextView = (TextView) findViewById(R.id.title);

            titileTextView.setText(title);
        }
    }
}
