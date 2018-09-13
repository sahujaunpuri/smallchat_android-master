package net.smallchat.im.contact;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.R;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.config.SMSConfig;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.utils.ShareToFriend;

/**
 * 邀请
 * @author dongli
 *
 */
public class InviteActivity extends BaseActivity implements OnClickListener{
	
	private Button mInviteBtn;
	private ImageView mIcon;
	private TextView mUserName,mUserPhone;

	private ImageLoader mImageLoader;
	private Login mLogin;
	
	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_view);
		mContext = this;
		mLogin = (Login)getIntent().getSerializableExtra("entity");
		mImageLoader = new ImageLoader();
		initCompent();
	}
	
	/*
	 * 实例化控件
	 */
	private void initCompent(){
	    setTitleContent(R.drawable.back_btn, 0, R.string.invite);
        mLeftBtn.setOnClickListener(this);
        
		mInviteBtn = (Button)findViewById(R.id.invite);
		mInviteBtn.setOnClickListener(this);
		
		mIcon = (ImageView)findViewById(R.id.user_icon);
		mUserName = (TextView)findViewById(R.id.user_name);
		mUserName.getPaint().setFakeBoldText(true);
		mUserPhone = (TextView)findViewById(R.id.user_phone);
		setText();
	}
	
	/*
	 * 给控件设置文本
	 */
	private void setText(){
		if (mLogin == null) {
			return;
		}
		mIcon.setImageResource(R.drawable.contact_default_header);
		mUserName.setText(mLogin.nickname);
		mUserPhone.setText("手机号："+mLogin.phone);
	}

	/*
	 * 发送短信
	 */
	private void sendSMS(String smsBody){
		Uri smsToUri = Uri.parse("smsto:"+mLogin.phone);
		Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
		intent.putExtra("sms_body", smsBody);
		startActivity(intent);
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
			InviteActivity.this.finish();
			break;
		case R.id.invite:
			/**
			 *
			 * Isfriend 0  没有关系
			 * 1 userID 关注 toUserID
			 * 2 toUserID 关注 usrID
			 * 3  相互关注
			 */
			if(mLogin == null){
				return;
			}
			switch (mLogin.isfriend) {
			case 0:
				ShareToFriend.shareToSMS(this,mLogin.phone,SMSConfig.INVITE_MESSAGE_SMS);
				break;
			case 1:
				break;
			case 2:
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

}
