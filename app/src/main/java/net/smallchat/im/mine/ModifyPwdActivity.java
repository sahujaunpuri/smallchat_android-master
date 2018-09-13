package net.smallchat.im.mine;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.R;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;

/**
 * 修改密码
 * @author dongli
 *
 */
public class ModifyPwdActivity extends BaseActivity {
	
	private Button mOkBtn;
	private EditText mOldPwdEdit,mNewPwdEdit,mConfirmPwdEdit;
	private String mInputOldPwd,mInputNewPwd,mInputConfirmPwd;

	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GlobalParam.MSG_CHECK_STATE:
				IMResponseState state = (IMResponseState)msg.obj;
				if(state == null ){
					Toast.makeText(mContext, R.string.commit_data_error, Toast.LENGTH_LONG).show();
				}
				if(state.code == 0){
					Login login = IMCommon.getLoginResult(mContext);
					if(login !=null){
						login.password = mInputNewPwd;
						IMCommon.saveLoginResult(mContext, login);
					}
					String hintMsg =state.errorMsg;
					if(hintMsg == null || hintMsg.equals("") ){
						hintMsg =mContext.getResources().getString(R.string.modity_success) ;
					}
					Toast.makeText(mContext, hintMsg, Toast.LENGTH_LONG).show();
					ModifyPwdActivity.this.finish();
				}else{
					String hintMsg =state.errorMsg;
					if(hintMsg == null || hintMsg.equals("") ){
						hintMsg = mContext.getResources().getString(R.string.commit_data_error);
					}
					Toast.makeText(mContext, hintMsg, Toast.LENGTH_LONG).show();
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
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_pwd_view);
		mContext = this;
		initCompent();
	}
	
	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,0,R.string.setting_modify_password);
		mLeftBtn.setOnClickListener(this);
		
		mOkBtn = (Button)findViewById(R.id.ok);
		mOkBtn.setOnClickListener(this);
		
		mOldPwdEdit = (EditText)findViewById(R.id.old_pwd);
		mNewPwdEdit = (EditText)findViewById(R.id.new_pwd);
		mConfirmPwdEdit = (EditText)findViewById(R.id.confirm_pwd);
	}

	
	/*
	 * 修改密码
	 */
	private void modifyPwd(){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, 
							mContext.getResources().getString(R.string.send_request));
					IMResponseState state = IMCommon.getIMServerAPI().editPasswd(mInputOldPwd, mInputNewPwd);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_STATE,state);
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
		case R.id.ok:
			mInputNewPwd = mNewPwdEdit.getText().toString();
			mInputOldPwd = mOldPwdEdit.getText().toString();
			mInputConfirmPwd = mConfirmPwdEdit.getText().toString();
			if((mInputNewPwd == null || mInputNewPwd.equals(""))
					|| (mInputOldPwd == null || mInputOldPwd.equals(""))
					|| (mInputConfirmPwd == null || mInputConfirmPwd.equals(""))){
				Toast.makeText(mContext, R.string.please_input_old_new_confirm,Toast.LENGTH_LONG).show();
				return;
			}
			if(mInputNewPwd.equals(mInputOldPwd)){
				Toast.makeText(mContext,R.string.new_old_pwd_not_equalse,Toast.LENGTH_LONG).show();
				return;
			}
			if(!mInputNewPwd.equals(mInputConfirmPwd)){
				Toast.makeText(mContext,R.string.check_pwd_hint,Toast.LENGTH_LONG).show();
				return;
			}
			modifyPwd();
			break;
		default:
			break;
		}
		
	}
	
	

}
