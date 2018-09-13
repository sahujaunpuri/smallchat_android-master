package net.smallchat.im.about;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.R;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

/**
 * 意见反馈
 * @author dongli
 *
 */
public class FeedBackActivity extends BaseActivity implements OnClickListener{

	private EditText mContentText;
	private Dialog  mPhoneDialog;
	
	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.feedback_page);
		initComponent();
	}

	/*
	 * 实例化控件
	 */
	private void initComponent(){
		setTitleContent(R.drawable.back_btn, R.drawable.send_map_btn, R.string.feedback);
		mLeftBtn.setOnClickListener(this);
		
		mRightBtn.setOnClickListener(this);
		
		mContentText = (EditText) findViewById(R.id.content);
		
	}
	
	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				
			case GlobalParam.SHOW_PROGRESS_DIALOG:
				String dialogMsg = (String)msg.obj;
				showProgressDialog(dialogMsg);
				break;
			case GlobalParam.HIDE_PROGRESS_DIALOG:
				hideProgressDialog();
				createDialog(mContext);
			
				break;
				
			case GlobalParam.MSG_LOAD_ERROR:
				hideProgressDialog();
				String error_Detail = (String)msg.obj;
				if(error_Detail != null && !error_Detail.equals("")){
					Toast.makeText(mContext,error_Detail,Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.feedback_fail),Toast.LENGTH_LONG).show();
				}
				break;
			case GlobalParam.MSG_NETWORK_ERROR:
				hideProgressDialog();
				Toast.makeText(mContext,R.string.network_error,Toast.LENGTH_LONG).show();
				break;
			case GlobalParam.MSG_TIME_OUT_EXCEPTION:
				hideProgressDialog();
				String message=(String)msg.obj;
				if (message==null || message.equals("")) {
					message= ChatApplication.getInstance().getResources().getString(R.string.timeout);
				}
				Toast.makeText(mContext,message, Toast.LENGTH_LONG).show();
				break;
			}
		}
	};
	
	/*
	 * 按钮点击事件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		
		super.onClick(v);
		
		switch (v.getId()) {
		case R.id.left_btn:
			this.finish();
			break;
			
		case R.id.right_btn:
			String content = mContentText.getText().toString().trim();
			if(content.equals("")){
				Toast.makeText(mContext, ChatApplication.getInstance().getResources().getString(R.string.input_feedback_content), Toast.LENGTH_SHORT).show();
				return;
			}
			
			Message message = new Message();
			message.obj = ChatApplication.getInstance().getResources().getString(R.string.add_more_loading);
			message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
			mHandler.sendMessage(message);
			
			feedback(content);
			break;

		default:
			break;
		}
	}
	
	/*
	 * 意见反馈
	 */
	private void feedback(final String content){
		if(IMCommon.verifyNetwork(mContext)){
			new Thread(){
				@Override
				public void run(){
					try {
						IMResponseState state = IMCommon.getIMServerAPI().feedback(content);
						if(state != null && state.code == 0){
							mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
						}else {
							Message msg=new Message();
							msg.what=GlobalParam.MSG_LOAD_ERROR;
							if(state != null && state.errorMsg != null && !state.errorMsg.equals("")){
								msg.obj = state.errorMsg;
							}else {
								msg.obj = ChatApplication.getInstance().getResources().getString(R.string.feedback_fail);
							}
							mHandler.sendMessage(msg);
						}
					} catch (IMException e) {
						e.printStackTrace();
						Message msg=new Message();
						msg.what=GlobalParam.MSG_TIME_OUT_EXCEPTION;
						msg.obj= ChatApplication.getInstance().getResources().getString(R.string.timeout);
						mHandler.sendMessage(msg);
					}
				}
			}.start();
		}else {
			mHandler.sendEmptyMessage(GlobalParam.MSG_NETWORK_ERROR);
		}
	}

	/*
	 * 创建取消对话框
	 */
	protected void createDialog(Context context) {
		mPhoneDialog = new Dialog (context,R.style.dialog);
		LayoutInflater factor = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View serviceView = factor.inflate(R.layout.ok_dialog, null);

		mPhoneDialog.setContentView(serviceView);
		mPhoneDialog.show();
		mPhoneDialog.setCancelable(false);	
		mPhoneDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT
				, LayoutParams.WRAP_CONTENT);

		
				Button Cancel = (Button)serviceView.findViewById(R.id.no);
		Cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPhoneDialog!=null) {
					mPhoneDialog.dismiss();
					mPhoneDialog = null;
				}
				FeedBackActivity.this.finish();
			}
		});
	}

}
