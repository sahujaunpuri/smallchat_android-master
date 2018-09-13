package net.smallchat.im.favorite;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.FavoriteItem;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.MessageCard;
import net.smallchat.im.Entity.MessageRedPacket;
import net.smallchat.im.Entity.MessageLocation;
import net.smallchat.im.Entity.MessageImage;
import net.smallchat.im.Entity.MessageVideo;
import net.smallchat.im.Entity.MessageTransfer;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.MessageAudio;
import net.smallchat.im.Entity.MovingContent;
import net.smallchat.im.Entity.MovingPic;
import net.smallchat.im.Entity.MovingVoice;
import net.smallchat.im.Entity.PopItem;
import net.smallchat.im.adapter.ChatMessageAdapter;
import net.smallchat.im.adapter.EmojiUtil;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.api.IMException;
import net.smallchat.im.widget.PopWindows;
import net.smallchat.im.widget.PopWindows.PopWindowsInterface;

import net.smallchat.im.BaseActivity;
import net.smallchat.im.R;
import net.smallchat.im.components.ShowImageActivity;
import net.smallchat.im.contact.ChooseUserActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 收藏详情
 * @author dongli
 *
 */
public class FavoriteDetailActivity extends BaseActivity {


	/*
	 * 定义全局变量
	 */
	private ImageView mHeaderIcon,mPic;
	private TextView mUserNameTextView,mTimeTextView,mContentTextView;
	private LinearLayout mVoiceLayout;
	private TextView mVoiceTimeTextView;
	private ProgressBar mSeekBar;
	private Button mPlayBtn;

	private FavoriteItem favoritem;
	private ImageLoader mImageLoader;

	private List<PopItem> mPopList = new ArrayList<PopItem>();
	private PopWindows mPopWindows;

	private MediaPlayer mMediaPlayer = null;
	private Timer mTimer;
	private TimerTask mTimerTask;
	private boolean isChanging=false;//互斥变量，防止定时器与SeekBar拖动时进度冲突
	private boolean isPlaying;
	private String mPlayUrl;

	/*
	 * 导入控件
	 * (non-Javadoc)
	 * @see net.smallchat.im.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_detail_view);
		mContext = this;
		favoritem = (FavoriteItem) getIntent().getSerializableExtra("entity");
		mImageLoader = new ImageLoader();
		initCompent();
	}


	/*
	 * 实例化控件
	 */
	private void initCompent(){
		setTitleContent(R.drawable.back_btn,R.drawable.more_btn,R.string.favorite_detail);
		mLeftBtn.setOnClickListener(this);
		mRightBtn.setOnClickListener(this);
		mHeaderIcon = (ImageView)findViewById(R.id.user_icon);
		mPic = (ImageView)findViewById(R.id.pic_icon);
		mPic.setOnClickListener(this);

		mUserNameTextView = (TextView)findViewById(R.id.user_name);
		mTimeTextView = (TextView)findViewById(R.id.time);
		mContentTextView = (TextView)findViewById(R.id.content);

		mVoiceLayout = (LinearLayout)findViewById(R.id.voice_layout);
		mPlayBtn = (Button)findViewById(R.id.play_btn);
		mPlayBtn.setOnClickListener(this);
		mVoiceTimeTextView = (TextView)findViewById(R.id.voice_time);
		mSeekBar = (ProgressBar)findViewById(R.id.voice_seekbar);



		mMediaPlayer=new MediaPlayer();
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@SuppressLint("NewApi")
			@Override
			public void onCompletion(MediaPlayer arg0) {
				//Toast.makeText(mContext, "结束", 1000).show();
				isPlaying = false;
				mPlayBtn.setBackground(mContext.getResources().getDrawable(R.drawable.play_voice_btn));
				//mMediaPlayer.release();
			}
		});

		setText();
	}


	/*
	 * SeekBar进度改变事件
	 */
	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
									  boolean fromUser) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			isChanging=true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mMediaPlayer.seekTo(seekBar.getProgress());
			isChanging=false;
		}
	}



	/*
	 * 给控件设置文本
	 */
	private void setText(){
		if(favoritem == null){
			return;
		}
		if(favoritem.messageType == MessageType.TEXT){
			if(mPopList!=null && mPopList.size()>0){
				mPopList.clear();
			}
			String[] array = mContext.getResources().getStringArray(R.array.favorite_more_text_item);
			for (int i = 0; i < array.length; i++) {
				mPopList.add(new PopItem(i+1,array[i]));
			}
		}else{
			if(mPopList!=null && mPopList.size()>0){
				mPopList.clear();
			}
			String[] array = mContext.getResources().getStringArray(R.array.favorite_more_item);
			for (int i = 0; i < array.length; i++) {
				mPopList.add(new PopItem(i+1,array[i]));
			}
		}
		mPopWindows = new PopWindows(mContext, mPopList, mRightBtn, new PopWindowsInterface() {

			@Override
			public void onItemClick(int position, View view) {
				switch (position) {
					case 1://发送给朋友
						ChatMessage chatMessage = new ChatMessage();
						chatMessage.time = System.currentTimeMillis();
						chatMessage.readState = 1;
						Intent chooseUserIntent = new Intent();
						if(favoritem.messageType == MessageType.TEXT){
							chatMessage.messageType = MessageType.TEXT;
							MovingContent movingContent = MovingContent.getInfo(favoritem.content);
							chatMessage.content = movingContent.content;
						}else if(favoritem.messageType == MessageType.AUDIO){
							chatMessage.messageType = MessageType.AUDIO;
							chatMessage.content = favoritem.content;
							chatMessage.audioData = MessageAudio.getInfo(chatMessage.content);
						}else if(favoritem.messageType == MessageType.IMAGE){
							chatMessage.messageType = MessageType.IMAGE;
							chatMessage.content = favoritem.content;
							chatMessage.imageData = MessageImage.getInfo(chatMessage.content);
						}else if(favoritem.messageType == MessageType.CARD){
							chatMessage.messageType = MessageType.CARD;
							chatMessage.content = favoritem.content;
							chatMessage.cardData= MessageCard.getInfo(chatMessage.content);
						}else if(favoritem.messageType == MessageType.VIDEO){
							chatMessage.messageType = MessageType.VIDEO;
							chatMessage.content = favoritem.content;
							chatMessage.videoData = MessageVideo.getInfo(chatMessage.content);
						}else if(favoritem.messageType == MessageType.TRANSFER){
							chatMessage.messageType = MessageType.TRANSFER;
							chatMessage.content = favoritem.content;
							chatMessage.transferData= MessageTransfer.getInfo(chatMessage.content);
						}else if(favoritem.messageType == MessageType.REDPACKET){
							chatMessage.messageType = MessageType.REDPACKET;
							chatMessage.content = favoritem.content;
							chatMessage.redpacketData= MessageRedPacket.getInfo(chatMessage.content);
						}else if(favoritem.messageType == MessageType.LOCATION){
							chatMessage.messageType = MessageType.LOCATION;
							chatMessage.content = favoritem.content;
							chatMessage.locationData = MessageLocation.getInfo(chatMessage.content);
						}

						chooseUserIntent.setClass(mContext, ChooseUserActivity.class);
						chooseUserIntent.putExtra("forward_msg", chatMessage);
						startActivity(chooseUserIntent);
						break;
					case 2:
						if(favoritem.messageType == MessageType.TEXT){//复制
							MovingContent movingContent = MovingContent.getInfo(favoritem.content);
							ClipboardManager cm =(ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
							cm.setText(movingContent.content);
						}else {//删除
							canclefavoriteMoving(favoritem.id);
						}
						break;
					case 3://删除
						canclefavoriteMoving(favoritem.id);
						break;

					default:
						break;
				}
			}
		});

		if(favoritem.headSmall!=null && !favoritem.headSmall.equals("")){
			mImageLoader.getBitmap(mContext, mHeaderIcon, null,favoritem.headSmall,0, false,true);
		}

		mUserNameTextView.setText(favoritem.nicknaem);
		mTimeTextView.setText(FeatureFunction.calculaterReleasedTime(mContext,
				new Date((favoritem.createtime*1000)),favoritem.createtime*1000,0));
		switch (favoritem.messageType) {
			case MessageType.TEXT:
				mPic.setVisibility(View.GONE);
				MovingContent movingContent = MovingContent.getInfo(favoritem.content);
				mContentTextView.setText(EmojiUtil.getExpressionString(getBaseContext(),movingContent.content, ChatMessageAdapter.EMOJIREX));
				break;
			case MessageType.IMAGE:
				mContentTextView.setVisibility(View.GONE);
				final MovingPic movingPic = MovingPic.getInfo(favoritem.content);
				mPic.setVisibility(View.VISIBLE);

				String picUrl = movingPic.urllarge;
				if(picUrl == null || picUrl.equals("")){
					picUrl = movingPic.urlsmall;
				}

				final  String  imgUrl = picUrl;

				if(imgUrl!=null && !imgUrl.equals("")){

					if(picUrl.startsWith("http://")){
						mImageLoader.getBitmap(mContext, mPic, null, imgUrl, 0, false,false);
					}else{
						Bitmap bitmap = null;
						if(!mImageLoader.getImageBuffer().containsKey(picUrl)){
							bitmap = BitmapFactory.decodeFile(picUrl);
							mImageLoader.getImageBuffer().put(picUrl, bitmap);
						}else {
							bitmap = mImageLoader.getImageBuffer().get(picUrl);
						}
						if(bitmap!=null && !bitmap.isRecycled()){
							mPic.setImageBitmap(bitmap);
						}
					}


					//if(movingPic.thumbUrl!=null && !movingPic.thumbUrl.equals("")){

					mPic.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(mContext, ShowImageActivity.class);
							intent.putExtra("imageurl", imgUrl);
							intent.putExtra("type", 2);
							mContext.startActivity(intent);
						}
					});
				}
				break;
			case MessageType.AUDIO:
				final MovingVoice movingVoice = MovingVoice.getInfo(favoritem.content);
				mContentTextView.setVisibility(View.GONE);
				mPic.setVisibility(View.GONE);
				mVoiceLayout.setVisibility(View.VISIBLE);
				mVoiceTimeTextView.setText(movingVoice.time);
				mPlayUrl = movingVoice.url;
				break;
			default:
				break;
		}
	}

	/*
	 * 取消收藏
	 */
	private void canclefavoriteMoving(final int favoriteid){
		if (!IMCommon.getNetWorkState()) {
			mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
			return;
		}
		new Thread(){
			public void run() {
				try {
					IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
							mContext.getResources().getString(R.string.send_request));
					IMResponseState status = IMCommon.getIMServerAPI().canclefavMoving(favoriteid);
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_FAVORITE_STATUS,status);
					mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
				} catch (IMException e) {
					e.printStackTrace();
					IMCommon.sendMsg(mBaseHandler,BASE_MSG_TIMEOUT_ERROR,
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
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			case R.id.left_btn:
				FavoriteDetailActivity.this.finish();
				break;
			case R.id.right_btn:
				mPopWindows.showGroupPopView(mPopList,Gravity.RIGHT,R.drawable.pop_bg,R.color.white,0);
				break;
			case R.id.play_btn:
				if(isPlaying){//停止播放
					isPlaying = false;
					mMediaPlayer.stop();
					mPlayBtn.setBackground(mContext.getResources().getDrawable(R.drawable.play_voice_btn));
				}else{//开始播放
					mPlayBtn.setBackground(mContext.getResources().getDrawable(R.drawable.stop_voice_btn));
					isPlaying = true;
					mMediaPlayer.reset();//恢复到未初始化的状态  
					if(mPlayUrl!=null && !mPlayUrl.equals("")){
						try {
							mMediaPlayer.setDataSource(mPlayUrl);
							mMediaPlayer.prepare();    //准备  
							new Handler().postDelayed(new Runnable() {

								@Override
								public void run() {
									mSeekBar.setMax(mMediaPlayer.getDuration());//设置SeekBar的长度  
									mMediaPlayer.start();  //播放  
									mTimer = new Timer();
									mTimerTask = new TimerTask() {
										@Override
										public void run() {
											if(isChanging==true)
												return;
											mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
										}
									};
									mTimer.schedule(mTimerTask, 0, 10);
								}
							}, 1000);


						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}


				}
				break;

			default:
				break;
		}
	}

	/*
	 * 处理消息
	 */
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case GlobalParam.MSG_CHECK_FAVORITE_STATUS:
					IMResponseState canclefavResult = (IMResponseState)msg.obj;
					if(canclefavResult == null){
						Toast.makeText(mContext, R.string.commit_dataing, Toast.LENGTH_LONG).show();
						return;
					}
					if(canclefavResult.code!=0){
						Toast.makeText(mContext, canclefavResult.errorMsg, Toast.LENGTH_LONG).show();
						return;
					}else{
						sendBroadcast(new Intent(GlobalParam.ACTION_REFRESH_MY_FAVORITE));
						FavoriteDetailActivity.this.finish();
					}
					break;

				default:
					break;
			}
		}

	};



}
