package net.smallchat.im.contact;

import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.Entity.Card;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.R;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.IMCommon;

/**
 * 选择的名片用户详情
 * @author dongli
 *
 */
public class CardUserDetailActivity extends BaseActivity {
	
	/*
	 * 定义全局变量
	 */
	private Login mCardLogin,mToLogin,mLogin;
	private ImageView mUserIcon,mToUserIcon;
	private TextView mUserNickName,mUserSign,mToUserNickName,mToUserSign;
	
	private ImageLoader mImageLoader;
	private int mChatType = 100;

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.card_user_detail_view);
		mCardLogin = (Login) getIntent().getSerializableExtra("user");
		mToLogin = (Login)getIntent().getSerializableExtra("toLogin");
		mLogin = IMCommon.getLoginResult(mContext);
		mChatType = getIntent().getIntExtra("chatType",100);
		mImageLoader = new ImageLoader();
		initCompent();
	}
	
	/*
	 * 示例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,R.drawable.ok_btn,R.string.recommend_to_friend);
		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);
		
		mUserIcon = (ImageView)findViewById(R.id.user_icon);
		mToUserIcon = (ImageView)findViewById(R.id.to_user_icon);
		mUserNickName = (TextView)findViewById(R.id.user_nickname);
		mUserSign = (TextView)findViewById(R.id.user_sign);
		mToUserNickName = (TextView)findViewById(R.id.to_user_nickname);
		mToUserSign = (TextView)findViewById(R.id.to_user_sign);
		if(mCardLogin!=null){
			if(mCardLogin.headSmall!=null && !mCardLogin.headSmall.equals("")){
				mImageLoader.getBitmap(mContext, mUserIcon, null,mCardLogin.headSmall,0,false,true);
			}
			mUserNickName.setText(mCardLogin.nickname);
			mUserSign.setText(mCardLogin.sign);
		}
		
		if(mToLogin!=null){
			if(mToLogin.headSmall!=null && !mToLogin.headSmall.equals("")){
				mImageLoader.getBitmap(mContext, mToUserIcon, null,mToLogin.headSmall,0,false,true);
			}
			mToUserNickName.setText(mToLogin.nickname);
			mToUserSign.setText(mToLogin.sign);
		}
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
			CardUserDetailActivity.this.finish();
			break;
		case R.id.right_btn://确认选择该名片
			if(mCardLogin == null ||  mToLogin == null){
				return;
			}
						
			Card card = new Card(mCardLogin.uid, mCardLogin.headSmall, mCardLogin.nickname, mCardLogin.sign);
			
			ChatMessage msg = new ChatMessage();
			msg.fromId = IMCommon.getUserId(mContext);
			msg.tag = UUID.randomUUID().toString();
			msg.fromName = mLogin.nickname;
			msg.fromUrl = mLogin.headSmall;
			msg.toId = mToLogin.uid;
			msg.toName = mToLogin.nickname;
			msg.toUrl = mToLogin.headSmall;
			msg.messageType = MessageType.CARD;
			msg.chatType = mChatType;
			msg.content = Card.getInfo(card);
			msg.time = System.currentTimeMillis();
			msg.readState = 1;
			
			Intent intent = new Intent(ChatMainActivity.ACTION_RECOMMEND_CARD);
			intent.putExtra("cardMsg", msg);
			sendBroadcast(intent);
			sendBroadcast(new Intent(ChooseUserActivity.ACTION_DESTROY_ACTIVITY));
			CardUserDetailActivity.this.finish();
			break;

		default:
			break;
		}
	}
	
	

}
