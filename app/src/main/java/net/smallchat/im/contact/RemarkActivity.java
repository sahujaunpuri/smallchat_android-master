package net.smallchat.im.contact;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.R;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

public class RemarkActivity extends BaseActivity implements OnClickListener{

	private EditText mMarkNameText;
	private String mFuid;
	private String mRemarkName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.remark_friend);
		mRemarkName = getIntent().getStringExtra("re_name");
		initComponent();
	}

	private void initComponent(){

		mFuid = getIntent().getStringExtra("fuid");

		setTitleContent(R.drawable.back_btn, R.drawable.ok_btn, R.string.alias_info);
		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);

		mMarkNameText = (EditText) findViewById(R.id.markname);
		mMarkNameText.setFocusable(true); 
		mMarkNameText.setFocusableInTouchMode(true); 
		mMarkNameText.requestFocus(); 
		
		mMarkNameText.setHint(mContext.getResources().getString(R.string.alias));
		mMarkNameText.setText(mRemarkName);
		if(mRemarkName!=null && !mRemarkName.equals("")){
			mMarkNameText.setSelection(mRemarkName.length());
		}

		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			@Override
			public void run() {
				InputMethodManager inputManager = 
						(InputMethodManager)mMarkNameText.
						getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
				inputManager.showSoftInput(mMarkNameText, 0);
			}
		}, 100);
		


	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn:
			this.finish();
			break;

		case R.id.right_btn:
			String remark = mMarkNameText.getText().toString().trim();
			if(remark!=null && !remark.equals("")){
				if( remark.length()>=2 && remark.length()<=8){
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, mContext.getResources().getString(R.string.commit_dataing));
					remarkFriend(remark);
				}else{
					Toast.makeText(mContext, mContext.getResources().
							getString(R.string.remark_length_hint),Toast.LENGTH_LONG).show();
					return;
				}
			}else{
				remarkFriend(remark);
			}
		
			break;

		default:
			break;
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case GlobalParam.MSG_LOAD_ERROR:
				hideProgressDialog();
				Toast.makeText(mContext,mContext.getResources().getString(R.string.remark_friend_failed),Toast.LENGTH_LONG).show();
				break;
			}
		}
	};


	private void remarkFriend(final String remark){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().remarkFriend(mFuid, remark);
						if(state != null && state.code == 0){
							Intent intent = new Intent();
							intent.putExtra("markName", remark);
							setResult(RESULT_OK, intent);
							IMCommon.sendMsg(mBaseHandler, BASE_HIDE_PROGRESS_DIALOG, 
									mContext.getResources().getString(R.string.remark_friend_success));
							RemarkActivity.this.finish();
						}else {
							mHandler.sendEmptyMessage(GlobalParam.MSG_LOAD_ERROR);
						}
					} catch (IMException e) {
						e.printStackTrace();
						IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
								mContext.getResources().getString(e.getStatusCode()));
					}
				}
			}.start();
		}else {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
		}
	}

}
