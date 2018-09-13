package net.smallchat.im;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.smallchat.im.Entity.LoginResult;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

/**
 * 登录
 * @author dongli
 *
 */
public class LoginActivity extends BaseActivity implements OnClickListener{

	/**
	 * 定义全局变量
	 */

	private EditText mUserNameText, mPasswordText;
	private Button mLoginBtn,mRegisterBtn,mForgetPwdBtn;
	public SharedPreferences mPreferences;


	/**
	 * 导入控件
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.login);
		initComponent();
	}

	/**
	 * 初始化控件
	 */
	private void initComponent(){
		setTitleContent(0, 0, R.string.login);
		mPreferences = this.getSharedPreferences(IMCommon.REMENBER_SHARED, 0);
		mUserNameText = (EditText) findViewById(R.id.username);
		mPasswordText = (EditText) findViewById(R.id.password);

		mLoginBtn = (Button) findViewById(R.id.login_btn);
		mRegisterBtn = (Button) findViewById(R.id.register);
		mForgetPwdBtn = (Button) findViewById(R.id.forget_pwd);


		mLoginBtn.setOnClickListener(this);
		mRegisterBtn.setOnClickListener(this);
		mForgetPwdBtn.setOnClickListener(this);

		String username = mPreferences.getString(IMCommon.USERNAME, "");
		String password = mPreferences.getString(IMCommon.PASSWORD, "");

		mUserNameText.setText(username);
		mPasswordText.setText(password);

		setUIValue();
	}

	/**
	 * 给控件设置值
	 */
	private void setUIValue(){
		mUserNameText.setHint(mContext.getResources().getString(R.string.username));
		mPasswordText.setHint(mContext.getResources().getString(R.string.password));
		mLoginBtn.setText(mContext.getResources().getString(R.string.login));
	}

	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.login_btn:
				checkLogin();
				break;
			case R.id.register:
				Intent intent = new Intent();
				intent.setClass(mContext, RegisterActivity.class);
				startActivityForResult(intent, 0);
				break;
			case R.id.forget_pwd:
				Intent forgetIntent = new Intent();
				forgetIntent.setClass(mContext, FindPasswordActivity.class);
				startActivity(forgetIntent);
				break;
			default:
				break;
		}
	}

	/**
	 * 检测用户名和密码是否合法
	 */
	private void checkLogin(){
		String username = mUserNameText.getText().toString().trim();
		String password = mPasswordText.getText().toString().trim();


		if(username.equals("")){
			Toast.makeText(mContext, R.string.please_input_username, Toast.LENGTH_SHORT).show();
			return;
		}

		if(password.equals("")){
			Toast.makeText(mContext, R.string.please_input_password, Toast.LENGTH_SHORT).show();
			return;
		}

		if(!IMCommon.verifyNetwork(mContext)){
			Toast.makeText(mContext, R.string.network_error, Toast.LENGTH_SHORT).show();
			return;
		}

		Message message = new Message();
		message.what = GlobalParam.SHOW_PROGRESS_DIALOG;
		message.obj = mContext.getResources().getString(R.string.loading_login);
		mHandler.sendMessage(message);

		getLogin(username, password);
	}

	/**
	 * 处理消息
	 */
	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case GlobalParam.SHOW_PROGRESS_DIALOG:
					String dialogMsg = (String)msg.obj;
					showProgressDialog(dialogMsg);
					break;
				case GlobalParam.HIDE_PROGRESS_DIALOG:
					hideProgressDialog();
					break;
				case GlobalParam.MSG_LOGIN_SUCCESS:
					setResult(RESULT_OK);
					LoginActivity.this.finish();
					break;
				case GlobalParam.MSG_LOGIN_FAILED:
					//setResult(RESULT_OK);
					//LoginActivity.this.finish();
					break;
				case GlobalParam.MSG_LOAD_ERROR:
					hideProgressDialog();
					String error_Detail = (String)msg.obj;
					if(error_Detail != null && !error_Detail.equals("")){
						Toast.makeText(mContext,error_Detail,Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(mContext,R.string.load_error,Toast.LENGTH_LONG).show();
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
						message=mContext.getResources().getString(R.string.timeout);
					}
					Toast.makeText(mContext,message, Toast.LENGTH_LONG).show();
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * 登录
	 * @param username
	 * @param password
	 */
	private void getLogin(final String username, final String password) {
		new Thread() {
			@Override
			public void run() {
				try {
					LoginResult result = IMCommon.getIMServerAPI().getLogin(username, password);
					if (result != null) {
						if (result.mState != null && result.mState.code == 0) {
							if (result.mLogin != null) {
								result.mLogin.password = password;
								IMCommon.saveLoginResult(mContext, result.mLogin);
								IMCommon.setUid(result.mLogin.uid);
							}

							Editor editor = mPreferences.edit();
							editor.putBoolean(IMCommon.ISREMENBER, true);
							editor.putString(IMCommon.USERNAME, username);
							editor.putString(IMCommon.PASSWORD, password);
							editor.commit();

							if(ChatApplication.getContryList() == null
									|| ChatApplication.getContryList().mCountryList == null
									|| ChatApplication.getContryList().mCountryList.size()<=0){
								ChatApplication.setContryList(IMCommon.getIMServerAPI().getCityAndContryUser());
							}
							mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
							mHandler.sendEmptyMessage(GlobalParam.MSG_LOGIN_SUCCESS);

							LoginActivity.this.finish();
						}else {
							Message message = new Message();
							message.what = GlobalParam.MSG_TIME_OUT_EXCEPTION;
							if(result.mState != null && result.mState.errorMsg != null && !result.mState.errorMsg.equals("")){
								message.obj = result.mState.errorMsg;
							}else {
								message.obj = mContext.getResources().getString(R.string.login_error);

							}
							mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
							mHandler.sendMessage(message);
						}
					} else {
						Message message = new Message();
						message.what = GlobalParam.MSG_LOAD_ERROR;
						message.obj = mContext.getResources().getString(R.string.login_error);
						mHandler.sendEmptyMessage(GlobalParam.HIDE_PROGRESS_DIALOG);
						mHandler.sendMessage(message);
					}
				} catch (IMException e) {
					Message msg = new Message();
					msg.what = GlobalParam.MSG_TIME_OUT_EXCEPTION;
					msg.obj = mContext.getResources().getString(R.string.timeout);
					mHandler.sendMessage(msg);
				}
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 键盘返回事件
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			/*setResult(GlobalParam.RESULT_EXIT);*/
			Intent intent = new Intent();
			intent.setAction(GlobalParam.EXIT_ACTION);
			sendBroadcast(intent);
			LoginActivity.this.finish();
			this.finish();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * 注册成功时销毁登陆界面
	 * @param arg0 requestCode 请求值
	 * @param arg1 resultCode 请求值
	 */
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		//super.onActivityResult(arg0, arg1, arg2);
		if(arg0 == 0 && arg1 == 1){
			Log.e("LOGIN","销毁登陆界面");
			LoginActivity.this.finish();
		}
	}


}
