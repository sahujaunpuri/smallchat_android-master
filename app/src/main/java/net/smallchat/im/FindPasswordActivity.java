package net.smallchat.im;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
 * 找回密码
 * @author dongli
 *
 */
public class FindPasswordActivity extends BaseActivity {

	private Button mOkBtn;
	private Button mBtnGetCode;

    private EditText mEditTextAccount;
	private EditText mEditTextCode;
	private String mInputAccount;
	private String mInputCode;
	private Dialog  mDialog;

	private int mTotalTime = 60;
	private String mServerVcode="";
	private boolean mIsCheck = true;


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
				case GlobalParam.MSG_CHECK_VALID_ERROR://验证码验证失败
					IMResponseState validState = (IMResponseState)msg.obj;
					if(validState == null){
						Toast.makeText(mContext,mContext.getResources().getString(R.string.commit_data_error),Toast.LENGTH_LONG).show();
						return;
					}
					String validErrorMsg = validState.errorMsg;
					if(validErrorMsg ==null || validErrorMsg.equals("")){
						validErrorMsg = mContext.getResources().getString(R.string.commit_data_error);
					}
					Toast.makeText(mContext, validErrorMsg, Toast.LENGTH_LONG).show();
					break;
				case GlobalParam.MSG_CHECK_STATE://请求验证码后，倒计时
					IMResponseState status = (IMResponseState) msg.obj;
					if (status!=null) {
						mTotalTime = 60;
						if (status.code == 0) {
							mBtnGetCode.setBackgroundResource(R.drawable.green_btn);

							//TODO：为了测试方便默认直接将验证码显示到验证码输入框
							//mEditTextCode.setText(status.validCode);
							mServerVcode=status.validCode;//将返回到验证码存放到内存
							mBtnGetCode.setEnabled(false);//禁用发送验证码按钮
							mBtnGetCode.setText(mContext.getResources().getString(R.string.Countdown)+mTotalTime+")");
							Message timeMessage = mHandler.obtainMessage(GlobalParam.MSG_UPDATEA_TIP_TIME);
							mHandler.sendMessageDelayed(timeMessage,1000);
						}
						else{
							Toast.makeText(mContext, status.errorMsg,Toast.LENGTH_LONG).show();
						}
					}else{
						Toast.makeText(mContext, R.string.send_veri_code, Toast.LENGTH_LONG).show();
					}
					break;
				case GlobalParam.MSG_UPDATEA_TIP_TIME://倒计时
					mTotalTime--;
					mBtnGetCode.setText(mContext.getResources().getString(R.string.Countdown)+mTotalTime+")");

					if(mTotalTime > 0){
						Message backMessage = mHandler.obtainMessage(GlobalParam.MSG_UPDATEA_TIP_TIME);
						mHandler.sendMessageDelayed(backMessage, 1000);      // send message
					}else{
						mTotalTime = 90;
						mBtnGetCode.setText(mContext.getResources().getString(R.string.get_valid_code));
						mBtnGetCode.setBackgroundResource(R.drawable.login_btn);
						mBtnGetCode.setEnabled(true);
					}

					break;
				default:
					break;
			}
		}
	};





	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see com.bigchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_password);
		mContext = this;
		initCompent();
	}

	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,0,R.string.find_pwd);
		mEditTextAccount =(EditText)findViewById(R.id.find_pwd_phone_or_email);
		mEditTextCode=(EditText)findViewById(R.id.find_pwd_code);
		mLeftBtn.setOnClickListener(this);
		mBtnGetCode=(Button)findViewById(R.id.btn_get_code);
		mBtnGetCode.setOnClickListener(this);
		mOkBtn = (Button)findViewById(R.id.ok_btn);
		mOkBtn.setOnClickListener(this);
	}

	/**
	 * 获取验证码
	 */
	private void getVeriCode() {

		mInputAccount = mEditTextAccount.getText().toString();

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
	 * @see com.bigchat.im.BaseActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.left_btn://点击退回
			FindPasswordActivity.this.finish();
			break;
		case R.id.ok_btn://点击下一步,进入密码重置框
			showResetPassword();
			break;
		case R.id.btn_get_code: //发送验证码
			   getVeriCode();
			break;
		default:
			break;
		}
	}
	//显示密码重置框
	private void showResetPassword()
	{
		mInputCode=mEditTextCode.getText().toString().trim();

		//在显示之前，需要先判断验证码是否跟系统上发出去验证码一致
		if(!mServerVcode.equals(mInputCode)){
			//提示输入正确到验证码
			String notice=mContext.getResources().getString(R.string.please_input_right_valid_code);
			showDialog(this,notice);
		}else {
			//启动重置密码界面
            Intent intent = new Intent(this,ResetPasswordActivity.class);
            //将手机号和验证码传递过去
            intent.putExtra(ResetPasswordActivity.ACCOUNT, mInputAccount);
            intent.putExtra(ResetPasswordActivity.VERIFY_CODE,mInputCode);
            startActivity(intent);
            this.finish();
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
				FindPasswordActivity.this.finish();
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
