package net.smallchat.im.action;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.widget.Toast;


import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.components.ReaderImpl;
import net.smallchat.im.global.FeatureFunction;
import com.xizue.recorder.xzAudioTrack;
import com.xizue.recorder.xzAudioTrack.MyOnCompletionListener;



/**
 * 
 * 功能：使用 MediaPlayer 播放音频<br />
 * @since
 */
public class AudioPlay {
	private static final String TAG = "MYAUDIOPLAY";
	private MediaPlayer mMediaPlayer;
	private xzAudioTrack  mAudioTrack = null;
	private boolean state = false;
	private String currUrl = "";
	
	private File voicePath;
	
	private MyMediaListenerImpl mMyListenerImpl;
	private SystemMediaListenerImpl mSysListenerImpl;
	private Context context;
	private String mMessageTag = "";
	
	private final boolean USE_SYSTEM_MEDIA = true;
	
	public AudioPlay(Context context) {
		super();
		voicePath = ReaderImpl.getAudioPath(context);
		if (USE_SYSTEM_MEDIA) {
			mSysListenerImpl = new SystemMediaListenerImpl();
		}
		else {
			mMyListenerImpl = new MyMediaListenerImpl();
		}
		
		this.context = context;
	}
	
	public String getMessageTag(){
		return mMessageTag;
	}
	
	public void setMessageTag(String tag){
		this.mMessageTag = tag;
	}
	
	public boolean getPlayState(){
		return state;
	}
	
	public String getCurrentUrl(){
		return currUrl;
	}

	private void init(ChatMessage chatMessage){
		if(USE_SYSTEM_MEDIA){
			this.mMediaPlayer = new MediaPlayer();
			this.mMediaPlayer.setOnCompletionListener(mSysListenerImpl);
		}
		else {
			mAudioTrack = new xzAudioTrack();
			mAudioTrack.init(chatMessage.sampleRate, 1, 16);
			mAudioTrack.setOnCompletionListener(mMyListenerImpl);
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param tid res.raw....
	 * 作者:fighter <br />
	 * 创建时间:2013-4-8<br />
	 * 修改时间:<br />
	 */
	/*public void play(Context context, int tid){
		mMediaPlayer = MediaPlayer.create(context, tid);
		mMediaPlayer.setOnCompletionListener(mListenerImpl);
		try {
			mMediaPlayer.start();   // 开始播放.
		} catch (Exception e) {
			e.printStackTrace();
			mMediaPlayer.release();
		}
		
	}*/
	
	/**
	 * 播放文件名为URL地址的音频,在本地 Voice目录中进行查找，如果没有,也不进行下载.
	 * 作者:fighter <br />
	 * 创建时间:2013-4-8<br />
	 * 修改时间:<br />
	 */
	public void play(ChatMessage chatMessage, int type){
		play(chatMessage, type, false);
	}
	
	public void play(ChatMessage chatMessage, int type, boolean hasCallBack){
		Log.e(TAG, "play()");
		if(currUrl.equals(chatMessage.audioData.url) && state){
			stop(hasCallBack);
			return;
		} else {
			stop(hasCallBack);
		}
		mMessageTag = chatMessage.tag;
		init(chatMessage);
		
		currUrl = chatMessage.audioData.url;
		String fileName = null;
		File file = null;
		if(currUrl.startsWith("http://")){
			fileName = FeatureFunction.generator(chatMessage.audioData.url);
			file = new File(voicePath, fileName);
		}else{
			fileName = currUrl;
			file = new File(fileName);
		}
		if(!file.exists()){
			Toast.makeText(context, "播放文件不存在!", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			if (USE_SYSTEM_MEDIA) {
				mMediaPlayer.setDataSource(file.getAbsolutePath());
				mMediaPlayer.prepare(); // 初始化.
				mMediaPlayer.start();   // 开始播放.
				mMediaPlayer.setLooping(false);  // 不循环.
			}
			else {
				mAudioTrack.start(file.getAbsolutePath());
			}
			
			onPlayStart(type);
			state = true;
		} catch (IllegalStateException e) {
			state = false;
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void stop(){
		stop(true);
	}
	
	public void stop(boolean hasCallBack){
		if (USE_SYSTEM_MEDIA) {
			if(state && mMediaPlayer != null)
			{
				try {
					mMediaPlayer.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(hasCallBack){
					onPlayStop();
				}
			}
		}
		else {
			if(state && mAudioTrack != null)
			{
				try {
					mAudioTrack.stop();
					mAudioTrack = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(hasCallBack){
					onPlayStop();
				}
			}
		}
		
		state = false;
	}
	
	public void onDestory(){
		stop();
	}
	
	protected void onPlayStart(int type) {
	}
	
	protected void onPlayStop() {
	}
	
	class MyMediaListenerImpl implements MyOnCompletionListener{
		@Override
		public void onCompletion() {
			// 播放完成功后要释放资源
			mAudioTrack.stop();
			
			state = false;
			
			((Activity)context).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onPlayStop();
				}
			});
		}
	}
	
	class SystemMediaListenerImpl implements OnCompletionListener{
		@Override
		public void onCompletion(MediaPlayer mp) {
			// 播放完成功后要释放资源
			mp.release();
			state = false;
			onPlayStop();
		}
	}
}
