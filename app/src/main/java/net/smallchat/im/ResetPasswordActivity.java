package net.smallchat.im;

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
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.api.IMException;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;

/**
 * 重置密码
 * @author dongli
 *
 */

public class ResetPasswordActivity extends BaseActivity {

	public static final String VERIFY_CODE ="verifycode" ;
	public static final String ACCOUNT ="account" ;
	private Button mResetBtn;

	private EditText mEditTextPassword;
	private String mInputAccount;
	private String mInputPassword;
	private String mInputCode;
	private Dialog  mDialog;


	/**
	 * 处理消息
	 */
	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

				case GlobalParam.RESET_PASSWORD_REQUEST://重置密码后的处理
					IMResponseState state = (IMResponseState)msg.obj;
					if(state == null ){
						Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
						return;
					}else if(state.code!=0 ){
						Toast.makeText(mContext, state.errorMsg,Toast.LENGTH_LONG).show();
						return;
					}
					createDialog(mContext,state.errorMsg);

					break;
				default:
					break;
			}
		}
	};
	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see com.minwork.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//获取参数
		mInputAccount = getIntent().getStringExtra(ResetPasswordActivity.ACCOUNT);
		mInputCode =getIntent().getStringExtra(ResetPasswordActivity.VERIFY_CODE);
		setContentView(R.layout.reset_password);
		mContext = this;
		initCompent();
	}

	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,0,R.string.reset_password);
		mEditTextPassword=(EditText)findViewById(R.id.reset_password_new_password);
		mLeftBtn.setOnClickListener(this);
		mResetBtn=(Button)findViewById(R.id.btn_reset_password);
		mResetBtn.setOnClickListener(this);
	}

	/*
	 * 重置密码
	 */
	private void resetPassword(){
		mInputPassword=mEditTextPassword.getText().toString().trim();
		if(mInputPassword==""||mInputPassword.isEmpty()){
			String notice=mContext.getResources().getString(R.string.please_input_new_password);
			showDialog(this,notice);
			return;
		}
		if(!IMCommon.getNetWorkState()){
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMResponseState state=null;
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, "数据提交中,请稍后...");
					if(mInputAccount !=null&& !mInputAccount.equals("")&& mInputAccount.contains("@")) {//邮箱找回
						state = IMCommon.getIMServerAPI().resetPassword(mInputAccount,mInputPassword,mInputCode,1);
					}else if(mInputAccount !=null&& !mInputAccount.equals("")) {//手机号码找回
						state = IMCommon.getIMServerAPI().resetPassword(mInputAccount,mInputPassword,mInputCode,0);
					}else {
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					}
					IMCommon.sendMsg(mHandler, GlobalParam.RESET_PASSWORD_REQUEST,state);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				}catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}




	/**
	 * 获取验证码
	 */
	private void getVeriCode() {

		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		if (mInputAccount == null || mInputAccount.equals("")) {
			Toast.makeText(mContext, mContext.getResources().getString(R.string.please_input_phone_number), Toast.LENGTH_LONG)
					.show();
			return;
		}
		new Thread() {
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.get_code));
					int type = 0;

					IMResponseState status = IMCommon.getIMServerAPI().getVerCode(mInputAccount,type);

					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE,status);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR,
							mContext.getResources().getString(e.getStatusCode()));
				} catch (Exception e) {
					e.printStackTrace();
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				}
			};
		}.start();
	}




	/*
	 * 按钮点击事件
	 * (non-Javadoc)
	 * @see com.minwork.im.BaseActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.left_btn://点击退回
				ResetPasswordActivity.this.finish();
				break;
			case R.id.btn_reset_password://点击重置密码
				resetPassword();
				break;
			default:
				break;
		}
	}


	private void createDialog(Context context,String msg) {
		mDialog = new Dialog (context,R.style.dialog);
		LayoutInflater factor = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View serviceView = factor.inflate(R.layout.normal_hint_dialog, null);

		mDialog.setContentView(serviceView);
		mDialog.show();
		mDialog.setCancelable(false);
		mDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT
				/*mContext.getResources().getDimensionPixelSize(R.dimen.bind_phone_height)*/
				, LayoutParams.WRAP_CONTENT);


		TextView chatContent=(TextView) serviceView
				.findViewById(R.id.card_title);
		chatContent.setText(msg);

		Button okBtn=(Button)serviceView.findViewById(R.id.yes);

		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mDialog!=null){
					mDialog.dismiss();
					mDialog = null;
				}
				ResetPasswordActivity.this.finish();
			}
		});

	}



	private void showDialog(Context context,String msg) {
		mDialog = new Dialog (context,R.style.dialog);
		LayoutInflater factor = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View serviceView = factor.inflate(R.layout.normal_hint_dialog, null);

		mDialog.setContentView(serviceView);
		mDialog.show();
		mDialog.setCancelable(false);
		mDialog.getWindow().setLayout(LayoutParams.WRAP_CONTENT
				/*mContext.getResources().getDimensionPixelSize(R.dimen.bind_phone_height)*/
				, LayoutParams.WRAP_CONTENT);


		TextView chatContent=(TextView) serviceView
				.findViewById(R.id.card_title);
		chatContent.setText(msg);

		Button okBtn=(Button)serviceView.findViewById(R.id.yes);

		okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mDialog!=null){
					mDialog.dismiss();
					mDialog = null;
				}

			}
		});

	}

}
