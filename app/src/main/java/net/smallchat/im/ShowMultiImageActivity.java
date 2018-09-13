package net.smallchat.im;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.smallchat.im.Entity.FriendsLoopItem;
import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.IMResponseState;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.MovingPic;
import net.smallchat.im.Entity.Picture;
import net.smallchat.im.Entity.PopItem;
import net.smallchat.im.adapter.ImagePagerAdapter;
import net.smallchat.im.album.AlbumCommentActivity;
import net.smallchat.im.contact.ChooseUserActivity;
import net.smallchat.im.exception.SPException;
import net.smallchat.im.friendcircle.FriendCircleActivity;
import net.smallchat.im.friendcircle.FriendCircleDetailActivity;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.MD5;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.api.IMException;
import net.smallchat.im.widget.GestureDetector;
import net.smallchat.im.widget.PhotoView;
import net.smallchat.im.widget.PhotoViewAttacher.OnPhotoTapListener;
import net.smallchat.im.widget.PopWindows;
import net.smallchat.im.widget.PopWindows.PopWindowsInterface;
import net.smallchat.im.widget.ViewPager;
import net.smallchat.im.widget.ViewPager.OnPageChangeListener;

/**
 * 查看大图（多张图片横向滑动）
 * @author dongli
 *
 */
public class ShowMultiImageActivity extends BaseActivity implements OnClickListener, OnPageChangeListener{

	private final static int SAVE_SUCCESS = 5126;
	private List<Picture> mPictureList;
	private final static int LOAD_SIZE = 6;
	public static final int SHOW_IMAGE = 11110;
	public final static int NETWORK_ERROR = 11112;
	private final static int HIDE_PROGRESSBAR = 11119;
	private final static int SHOW_PROGRESSBAR = 11120;
	public final static int RECYCLE_BITMAP = 11123;
	public final static int RECYCLE_BEFORE_BITMAP = 11124;
	public final static int RECYCLE_AFTER_BITMAP = 11125;
	public final static int SET_BITMAP_NULL = 11126;
	private int mPosition;
	LinkedList<View> mDetailList;
	private HashMap<Integer, SoftReference<Bitmap>> mBitmapCache;
	private ViewPager mViewPager;
	private ImagePagerAdapter mPagerAdapter;
	private static final int IMAGE_GETTER_CALLBACK = 1;
	private FriendsLoopItem mShare;
	private LinearLayout mBottomLayout;
	private TextView mContentView;
	private TextView mZanCountView/*, mCommentCountView*/;
	private LinearLayout mZanLayout, mCommentLayout;
	private TextView mRightZanTextView,mRightCommentTextView;
	private ImageView mZanIcon;
	private final static int ZAN_SUCCESS = 0x11211;
	private final static int SHARE_SUCCESS = 0x11212;
	private RelativeLayout mTitleLayout;
	private LinearLayout mRightClickLayout;
	private PopWindows mPopWindows;
	private int mHide;

	private int mSize;
	private String mCurrentImagURL,mCurrentImageSmallUrl;

	private List<PopItem> mPopList = new ArrayList<PopItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.multiscanimagelayout);
		IntentFilter filter = new IntentFilter();
		filter.addAction(GlobalParam.ACTION_DESTROY_ACTIVITY_DEL_SHARE);
		filter.addAction(GlobalParam.ACTION_REFRESH_MOVING_DETAIL);
		registerReceiver(mReceiver, filter);
		initComponent();
	}


	private BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent!=null){
				String action = intent.getAction();
				if(action.equals(GlobalParam.ACTION_DESTROY_ACTIVITY_DEL_SHARE)){
					ShowMultiImageActivity.this.finish();
				}else if(action.equals(GlobalParam.ACTION_REFRESH_MOVING_DETAIL)){
					getShareDetail();
				}
			}
		}};


		private void initComponent(){
			mHide = getIntent().getIntExtra("hide",0);

			mShare = (FriendsLoopItem) getIntent().getSerializableExtra("share");

			mBitmapCache = new HashMap<Integer, SoftReference<Bitmap>>();

			mPosition = getIntent().getIntExtra("pos", 0);
			mBottomLayout = (LinearLayout) findViewById(R.id.bottomlayout);
			if(mHide == 1){
				setTitleContent(R.drawable.back_btn, 0,R.string.look_big_img);
			}else{
				setTrowMenuTitleContent(R.drawable.back_btn, R.drawable.more_btn,
						FeatureFunction.formartTime(mShare.createtime,"MM月dd日 HH:mm"),
						"1/"+mShare.listpic.size());
				String[] itemMenuBg;
				if(mShare.uid.equals(IMCommon.getUserId(mContext))){
					itemMenuBg = mContext.getResources().getStringArray(R.array.album_menu_text_item);
				}else{
					itemMenuBg = mContext.getResources().getStringArray(R.array.album_menu_item);
				}
				//album_menu_text_item
				for (int i = 0; i < itemMenuBg.length; i++) {
					mPopList.add(new PopItem(i+1, itemMenuBg[i],null));
				}

				mPopWindows = new PopWindows(mContext, mPopList, mRightBtn, new PopWindowsInterface() {

					@Override
					public void onItemClick(int position, View view) {
						switch (position) {
						case 1://发送给朋友
							if(mCurrentImagURL == null || !mCurrentImagURL.equals("")){
								MovingPic pic = new MovingPic(mCurrentImagURL, mCurrentImageSmallUrl,MessageType.IMAGE +"");
								ChatMessage chatMessage = new ChatMessage();
								chatMessage.content = MovingPic.getInfo(pic);
								chatMessage.messageType = MessageType.IMAGE;
								chatMessage.time = System.currentTimeMillis();
								chatMessage.readState = 1;

								Intent chooseUserIntent = new Intent();
								chooseUserIntent.setClass(mContext, ChooseUserActivity.class);
								chooseUserIntent.putExtra("forward_msg", chatMessage);
								startActivity(chooseUserIntent);
							}
							break;
						case 2://保存到手机
							savePicture();
							break;
						case 3://收藏
							Log.e("mCurrentImagURL",mCurrentImagURL);
							Log.e("mCurrentImageSmallUrl", mCurrentImageSmallUrl);
							MovingPic pic = new MovingPic(mCurrentImagURL,mCurrentImageSmallUrl, MessageType.IMAGE +"");
							favoriteMoving(MovingPic.getInfo(pic));
							break;
						case 4://删除
							delShare();
							break;

						default:
							break;
						}
					}
				});

				mRightBtn.setOnClickListener(this);
				mBottomLayout.setVisibility(View.VISIBLE);
			}

			mLeftBtn.setOnClickListener(this);


			mTitleLayout = (RelativeLayout) findViewById(R.id.title_layout);

			mContentView = (TextView) findViewById(R.id.content);
			mZanCountView = (TextView) findViewById(R.id.zancount);
			mRightZanTextView = (TextView)findViewById(R.id.right_zancount);
			mRightCommentTextView = (TextView)findViewById(R.id.right_commentcount);


			mZanLayout = (LinearLayout) findViewById(R.id.zanlayout);
			mZanLayout.setOnClickListener(this);
			mCommentLayout = (LinearLayout) findViewById(R.id.commentlayout);
			mCommentLayout.setOnClickListener(this);
			mZanIcon = (ImageView) findViewById(R.id.zanicon);

			setText();

			mDetailList = new LinkedList<View>();
			mViewPager = (ViewPager) findViewById(R.id.viewpager);

			for (int i = 0; i < mPictureList.size(); i++) {
				mDetailList.add(null);
			}

			int start = mPosition - LOAD_SIZE/2 >= 0 ? mPosition - LOAD_SIZE/2 : 0;
			int end = mPosition + LOAD_SIZE/2 <= mPictureList.size() - 1 ? mPosition + LOAD_SIZE/2 : mPictureList.size() - 1;


			mCurrentImagURL = mPictureList.get(0).originUrl;
			mCurrentImageSmallUrl = mPictureList.get(0).smallUrl;
			for (int i = start; i <= end; i++) {
				mDetailList.set(i, addView(mContext));
				if (!mPictureList.get(i).originUrl.equals("")) {
					downLoadImage(mPictureList.get(i).originUrl, i, -1);
					//downloadImage(i);
				} 
			}

			mPagerAdapter = new ImagePagerAdapter(mDetailList);

			mViewPager.setAdapter(mPagerAdapter);
			mViewPager.setOnPageChangeListener(this);
			mViewPager.setCurrentItem(mPosition);

			mRightClickLayout = (LinearLayout)findViewById(R.id.right_click_layout);
			mRightClickLayout.setOnClickListener(this);
		}

		private void setText(){
			if(mShare != null){

				mPictureList = mShare.listpic;
				mSize = mPictureList.size();
				mContentView.setText(mShare.content);
			
				mRightZanTextView.setText(mShare.praises+"");
				mRightCommentTextView.setText(mShare.replys+"");

				if(mShare.ispraise == 0){
					mZanIcon.setImageResource(R.drawable.friend_circle_cancle_praise_btn);
					mZanCountView.setText("赞");
				}else if(mShare.ispraise == 1){
					mZanIcon.setImageResource(R.drawable.friend_circle_praise_btn);
					mZanCountView.setText("取消赞");
				}
			}
		}


		private Handler mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SHOW_IMAGE:
					Bitmap bitmap = (Bitmap) msg.obj;
					int position = msg.arg1;
					if(mDetailList != null && mDetailList.get(position) != null){
						ProgressBar progressBar = (ProgressBar)mDetailList.get(position).findViewById(R.id.loading);
						if(progressBar.getVisibility() == View.VISIBLE){
							progressBar.setVisibility(View.GONE);
						}
						PhotoView imageView = (PhotoView)mDetailList.get(position).findViewById(R.id.image);
						imageView.setImageBitmap(bitmap);
						imageView.setVisibility(View.VISIBLE);
					}
					break;
				case SHOW_PROGRESSBAR:
					int pos = msg.arg1;
					if(pos < mDetailList.size() && mDetailList != null && mDetailList.get(pos) != null){
						ProgressBar progressBar = (ProgressBar)mDetailList.get(pos).findViewById(R.id.loading);
						progressBar.setVisibility(View.VISIBLE);
					}
					break;
				case HIDE_PROGRESSBAR:
					int index = msg.arg1;
					if(mDetailList != null && mDetailList.get(index) != null){
						ProgressBar progressbar = (ProgressBar)mDetailList.get(index).findViewById(R.id.loading);
						if(progressbar.getVisibility() == View.VISIBLE){
							progressbar.setVisibility(View.GONE);
						}
					}
					break;
				case RECYCLE_BITMAP:
					int itemIndex = msg.arg1;
					if(mBitmapCache != null && mBitmapCache.get(itemIndex) != null && mBitmapCache.get(itemIndex).get() != null && !mBitmapCache.get(itemIndex).get().isRecycled()){
						mBitmapCache.get(itemIndex).get().recycle();
					}
					break;
				case RECYCLE_BEFORE_BITMAP:
					int beforeindex = msg.arg1;
					freeBefore(beforeindex);
					break;
				case RECYCLE_AFTER_BITMAP:
					int bitmapindex = msg.arg1;
					freeAfter(bitmapindex);
					break;
				case SET_BITMAP_NULL:
					ImageView imageView = (ImageView) msg.obj;
					if(imageView != null){
						imageView.setImageBitmap(null);
						imageView.setImageResource(R.drawable.default_image);
					}
					break;

				case SAVE_SUCCESS:
					Toast.makeText(mContext, mContext.getString(R.string.save_picture_to_ablun), Toast.LENGTH_SHORT).show();
					break;

				case IMAGE_GETTER_CALLBACK:
					((Runnable) msg.obj).run();
					break;



				case GlobalParam.MSG_LOAD_ERROR:
					String error_Detail = (String)msg.obj;
					if(error_Detail != null && !error_Detail.equals("")){
						Toast.makeText(mContext,error_Detail,Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(mContext,R.string.load_error,Toast.LENGTH_LONG).show();
					}

				case ZAN_SUCCESS:
					hideProgressDialog();
					break;

				case SHARE_SUCCESS:
					hideProgressDialog();
					break;
				case GlobalParam.MSG_SHOW_LOAD_DATA:
					mShare = (FriendsLoopItem)msg.obj;
					setText();
					break;
				case GlobalParam.MSG_CHECK_FAVORITE_STATUS:
					IMResponseState favoriteResult = (IMResponseState)msg.obj;
					if(favoriteResult == null){
						Toast.makeText(mContext, R.string.commit_dataing, Toast.LENGTH_LONG).show();
						return;
					}
					if(favoriteResult.code!=0){
						Toast.makeText(mContext, favoriteResult.errorMsg, Toast.LENGTH_LONG).show();
						return;
					}
					break;
				case GlobalParam.MSG_CHECK_DEL_SHARE_STATUS:
					IMResponseState delStatus = (IMResponseState)msg.obj;
					if(delStatus == null){
						Toast.makeText(mContext, R.string.commit_data_error,Toast.LENGTH_LONG).show();
						return;
					}
					if(delStatus.code !=0){
						Toast.makeText(mContext, delStatus.errorMsg,Toast.LENGTH_LONG).show();
						return;
					}
					sendBroadcast(new Intent(FriendCircleActivity.MSG_REFRESH_MOVIINF));
					ShowMultiImageActivity.this.finish();
					break;
				case GlobalParam.NO_SD_CARD:
					Toast.makeText(mContext, R.string.no_sd_card_hint,Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
			}

		};

		private final Runnable mDismissOnScreenControlRunner = new Runnable() {
			public void run() {
				hideOnScreenControls();
			}
		};

		private void hideOnScreenControls() {

			Animation a = new AlphaAnimation(1, 0);;
			a.setDuration(500);
			mBottomLayout.startAnimation(a);
			mBottomLayout.setVisibility(View.INVISIBLE);
			mTitleLayout.startAnimation(a);
			mTitleLayout.setVisibility(View.GONE);

		}

		private void showOnScreenControls() {

			Animation animation = new AlphaAnimation(0, 1);
			animation.setDuration(500);
			mBottomLayout.startAnimation(animation);
			mBottomLayout.setVisibility(View.VISIBLE);
			mTitleLayout.startAnimation(animation);
			mTitleLayout.setVisibility(View.VISIBLE);
		}

		private void scheduleDismissOnScreenControls() {
			mHandler.removeCallbacks(mDismissOnScreenControlRunner);
			mHandler.postDelayed(mDismissOnScreenControlRunner, 2000);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.left_btn:
				ShowMultiImageActivity.this.finish();
				break;

			case R.id.right_btn:
				mPopWindows.showGroupPopView(mPopList,Gravity.RIGHT,R.drawable.pop_bg,R.color.white,0);
				break;

			case R.id.imageview:
				if(mBottomLayout.getVisibility() == View.INVISIBLE){
					showOnScreenControls();
				}else if(mBottomLayout.getVisibility() == View.VISIBLE){
					hideOnScreenControls();
				}
				break;

			case R.id.zanlayout:
				zan();
				break;

			case R.id.commentlayout://开启新的评论页面
				Intent commentIntent = new Intent(mContext, AlbumCommentActivity.class);
				commentIntent.putExtra("item", mShare);
				startActivity(commentIntent);
				break;
			case R.id.right_click_layout:
				Intent detailIntent = new Intent(mContext, FriendCircleDetailActivity.class);
				detailIntent.putExtra("item", mShare);
				startActivity(detailIntent);
				break;
			default:
				break;
			}
		}

		class GestureListener extends GestureDetector.SimpleOnGestureListener {

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if(mBottomLayout.getVisibility() == View.INVISIBLE){
					showOnScreenControls();
				}else if(mBottomLayout.getVisibility() == View.VISIBLE){
					hideOnScreenControls();
				}
				return super.onSingleTapConfirmed(e);
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return super.onSingleTapUp(e);
			}

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				return super.onDoubleTap(e);
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
					float velocityY) {
				return super.onFling(e1, e2, velocityX, velocityY);
			}

		}

		private void savePicture(){
			new Thread(){
				@Override
				public void run(){
					String fileName = FeatureFunction.getPhotoFileName(0);
					Bitmap bitmap = mBitmapCache.get(mPosition).get();
					if(bitmap == null){
						return;
					}
					String uri = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, fileName, "");
					String filePath = FeatureFunction.getFilePathByContentResolver(mContext, Uri.parse(uri));
					int index = filePath.lastIndexOf("/");
					String filePrefix = filePath.substring(0, index + 1) + fileName;
					try {
						File bitmapFile = new File(filePrefix);
						FileOutputStream bitmapWriter;
						bitmapWriter = new FileOutputStream(bitmapFile);
						if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bitmapWriter)) {
							File oldfile = new File(filePath);
							if(oldfile.exists()){
								oldfile.delete();
							}
							bitmapWriter.flush();
							bitmapWriter.close();
							Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);     
							Uri refreshUri = Uri.fromFile(bitmapFile);     
							intent.setData(refreshUri);     
							mContext.sendBroadcast(intent);  
							mHandler.sendEmptyMessage(SAVE_SUCCESS);
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();

		}

		private void freeBitmap(HashMap<Integer, SoftReference<Bitmap>> cache){
			if(cache.isEmpty()){
				return;
			}

			for (int i = 0; i < mDetailList.size(); i++) {
				if(mDetailList.get(i) != null){
					ImageView imageView = (ImageView)mDetailList.get(i).findViewById(R.id.image);
					if(imageView != null){
						Message message = new Message();
						message.obj = imageView;
						message.what = SET_BITMAP_NULL;
						mHandler.sendMessage(message);
					}
				}
			}

			for(SoftReference<Bitmap> bitmap:cache.values()){
				if(bitmap.get() != null && !bitmap.get().isRecycled()){
					bitmap.get().recycle();
					bitmap = null;

				}
			}
			cache.clear();
			System.gc();
		}

		private void freeBefore( final int position){

			if(mBitmapCache.isEmpty()){
				return;
			}

			if(mBitmapCache.size() >= 0){
				for (int i = 0; i < position; i++) {
					if(mDetailList != null && mDetailList.get(i) != null){
						ImageView imageView = (ImageView)mDetailList.get(i).findViewById(R.id.image);
						if(imageView != null){
							Message message = new Message();
							message.obj = imageView;
							message.what = SET_BITMAP_NULL;
							mHandler.sendMessage(message);
						}
					}

					if(mBitmapCache.get(i) != null && mBitmapCache.get(i).get() != null && !mBitmapCache.get(i).get().isRecycled()){
						mBitmapCache.get(i).get().recycle();
						mBitmapCache.get(i).clear();
						mBitmapCache.remove(i);
					}
				}
				if(mBitmapCache != null){
					Log.e("mBitmapCache", "mBitmapCache.size() = " + mBitmapCache.size());
				}
				System.gc();
			}
		}

		private void freeAfter(final int position){
			
			if(mBitmapCache.size() > 0){

				for (int i = position + 1; i < mPictureList.size(); i++) {
					Log.e("after", "after");
					Log.e("i", "i = " + i);
					if(mDetailList != null && mDetailList.get(i) != null){
						ImageView imageView = (ImageView)mDetailList.get(i).findViewById(R.id.image);
						if(imageView != null){
							Message message = new Message();
							message.obj = imageView;
							message.what = SET_BITMAP_NULL;
							mHandler.sendMessage(message);
						}
					}

					if(mBitmapCache.get(i) != null && mBitmapCache.get(i).get() != null && !mBitmapCache.get(i).get().isRecycled()){
						mBitmapCache.get(i).get().recycle();
						mBitmapCache.get(i).clear();
						mBitmapCache.remove(i);
					}
				}
				System.gc();
			}
		}

		private void BitmpCache(int position,SoftReference<Bitmap> bitmap){
			if(!mBitmapCache.containsKey(position)){
				mBitmapCache.put(position, bitmap);
			}
		}

		public View addView(Context context) {
			View view=null;
			view = LayoutInflater.from(context).inflate(R.layout.item_pager_image, null);
			PhotoView imageView = (PhotoView) view.findViewById(R.id.image);
			imageView.setImageResource(R.drawable.default_image);
			imageView.setOnPhotoTapListener(new OnPhotoTapListener() {

				@Override
				public void onPhotoTap(View view, float x, float y) {
					if(mBottomLayout.getVisibility() == View.INVISIBLE){
						showOnScreenControls();
					}else if(mBottomLayout.getVisibility() == View.VISIBLE){
						hideOnScreenControls();
					}
				
				}
			});

			return view;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {

			for (int i = 0; i < mDetailList.size(); i++) {
				if(i != position){
					if(mDetailList.get(i) != null){
						PhotoView imageView = (PhotoView)mDetailList.get(i).findViewById(R.id.image);
						if(imageView != null){
							imageView.zoomTo(1, 0, 0);
						}
					}
				}
			}

			int end = position + LOAD_SIZE/2;
			int start = position -LOAD_SIZE/2;
			if(position > mPosition && end < mPictureList.size()){
				if(mDetailList.get(end) == null){
					mDetailList.set(end, addView(mContext));
				}
				downLoadImage(mPictureList.get(end).originUrl, end, start);
			}else if(position < mPosition && start >= 0){
				if(mDetailList.get(start) == null){
					mDetailList.set(start, addView(mContext));
				}
				downLoadImage(mPictureList.get(start).originUrl, start, end);

			}
			mCurrentImagURL = mPictureList.get(position).originUrl;
			mCurrentImageSmallUrl = mPictureList.get(position).smallUrl;
			if(mTrowTitle!=null){
				mTrowTitle.setText((position+1)+"/"+mSize);
			}

		}


		@Override
		protected void onDestroy() {
			for (int i = 0; i < mDetailList.size(); i++) {
				if(mDetailList.get(i) != null){
					ImageView imageView = (ImageView)mDetailList.get(i).findViewById(R.id.image);
					if(imageView != null){
						imageView.setImageBitmap(null);
					}
				}

			}
			freeBitmap(mBitmapCache);
			unregisterReceiver(mReceiver);
			super.onDestroy();
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				this.finish();
				return true;
			}
			return super.dispatchKeyEvent(event);
		}

		private void downLoadImage(final String mImageUrl, final int position, final int freePos){
			new Thread(){
				@Override
				public void run() {
					int pos = freePos;
					if(pos < 0 || pos > mPictureList.size() - 1){
						pos = -1;
					}
					if(pos != -1){
						if(pos > position){
							freeAfter(pos);
						}else {
							freeBefore(pos);
						}
					}

					byte[] imageData = null;
					if(mImageUrl != null){
						try {
							Log.e("downLoadImage", "position = " + position);
							File file = null;
							String fileName = new MD5().getMD5ofStr(mImageUrl);// url.replaceAll("/",
							if (FeatureFunction.checkSDCard()) {

								if (FeatureFunction.newFolder(Environment.getExternalStorageDirectory()
										+ net.smallchat.im.global.ImageLoader.SDCARD_PICTURE_CACHE_PATH)) {
									file = new File(
											Environment.getExternalStorageDirectory()
											+ net.smallchat.im.global.ImageLoader.SDCARD_PICTURE_CACHE_PATH, fileName);
									if(file != null && file.exists()){
										try {
											FileInputStream fin = new FileInputStream(file.getPath());
											int length = fin.available();
											byte[] buffer = new byte[length];
											fin.read(buffer);
											fin.close();
											imageData = buffer;
										} catch (FileNotFoundException e) {
											e.printStackTrace();
										} catch (IOException e) {
											e.printStackTrace();
										}

									} else{
										if (IMCommon.verifyNetwork(mContext)){
											imageData = getImage(new URL(mImageUrl),file, position);
										}
									}
									try {
										Message message = new Message();
										if(imageData != null){
											Log.e("imageData_length", imageData.length + "");
											BitmapFactory.Options opt = new BitmapFactory.Options();
											opt.inSampleSize = 1;
											opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
											opt.inPurgeable = true;
											opt.inInputShareable = true;
											BitmpCache(position, new SoftReference<Bitmap>(BitmapFactory.decodeByteArray(imageData, 0,imageData.length, opt)));
											if(mBitmapCache.get(position) != null && mBitmapCache.get(position).get() != null){
												message.obj = mBitmapCache.get(position).get();
											}else {
												message.obj = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
											}
										}else {
											BitmpCache(position, new SoftReference<Bitmap>(BitmapFactory.decodeResource(getResources(), R.drawable.default_image)));
											if(mBitmapCache.get(position) != null && mBitmapCache.get(position).get() != null){
												message.obj = mBitmapCache.get(position).get();
											}else {
												message.obj = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
											}
										}

										imageData = null;
										System.gc();
										message.arg1 = position;
										message.what = SHOW_IMAGE;
										mHandler.sendMessage(message);
									} catch (OutOfMemoryError e) {
										Log.e("BookDetail", "Out of Memory");
										e.printStackTrace();
									}

								}
							}else{
								mHandler.sendEmptyMessage(GlobalParam.NO_SD_CARD);
							}

						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (SPException e) {
							e.printStackTrace();
						}
					}
				}

			}.start();

		}

		private void writeBitmapToCache(byte[] imgData, File file) {

			FileOutputStream fos = null;
			BufferedOutputStream outPutBuffer = null;

			if (file != null) {
				try {
					if (!file.exists()) {
						file.createNewFile();
					}
					fos = new FileOutputStream(file);

					outPutBuffer = new BufferedOutputStream(fos);
					outPutBuffer.write(imgData);
					outPutBuffer.flush();
					fos.flush();

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fos != null) {
							fos.close();
						}

						if (outPutBuffer != null) {
							outPutBuffer.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private byte[] getImage(URL path, File file, int position) throws SPException{
			HttpURLConnection conn = null;
			InputStream is = null;
			byte[] imgData = null;
			try {
				URL url = path;
				conn = (HttpURLConnection) url.openConnection();
				is = conn.getInputStream();
				// Get the length
				int length = (int) conn.getContentLength();
				if (length != -1) {
					imgData = new byte[length];
					byte[] temp=new byte[512];
					int readLen=0;
					int destPos=0;
					while((readLen=is.read(temp))>0){
						System.arraycopy(temp, 0, imgData, destPos, readLen);
						destPos+=readLen;
						Message message = new Message();
						message.what = SHOW_PROGRESSBAR;
						message.arg1 = position;
						mHandler.sendMessage(message);
					}

					Message message = new Message();
					message.what = HIDE_PROGRESSBAR;
					message.arg1 = position;
					mHandler.sendMessage(message);

					if (file != null) {
						writeBitmapToCache(imgData, file);
					}
				}

				if(is != null){
					is.close();
				}

				if(conn != null){
					conn.disconnect();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (OutOfMemoryError e){
				throw new SPException(mContext.getString(R.string.exception_out_of_memory));
			}

			return imgData;
		}

		private void zan(){
			new Thread(){
				@Override
				public void run(){
					if(IMCommon.verifyNetwork(mContext)){
						try {
							IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
									mContext.getResources().getString(R.string.send_request));
							IMResponseState state = IMCommon.getIMServerAPI().sharePraise(mShare.id);
							if(state != null && state.code == 0){
								getShareDetail();
							}else {
								Message msg=new Message();
								msg.what = GlobalParam.MSG_LOAD_ERROR;
								if(state != null && state.errorMsg != null && !state.errorMsg.equals("")){
									msg.obj = state.errorMsg;
								}else {
									msg.obj = mContext.getString(R.string.operate_failed);
								}
								mHandler.sendMessage(msg);
							}
							mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
						} catch (IMException e) {
							e.printStackTrace();
							IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR, 
									mContext.getResources().getString(e.getStatusCode()));
						}
					}else {
						mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
					}
				}
			}.start();
		}

		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	
		}

		private void getShareDetail(){
			if(!IMCommon.getNetWorkState()){
				mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
				return;
			}
			new Thread(){
				public void run() {
					try {
						FriendsLoopItem shareDetail = IMCommon.getIMServerAPI().shareDetail(mShare.id);
						if(shareDetail !=null){
							IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_LOAD_DATA,shareDetail);
						}
					} catch (IMException e) {
						e.printStackTrace();
						IMCommon.sendMsg(mBaseHandler, BASE_MSG_TIMEOUT_ERROR, 
								mContext.getResources().getString(e.getStatusCode()));
					}catch (Exception e) {
						e.printStackTrace();
					}
				};
			}.start();
		}


		private void favoriteMoving(final String favoriteContent){
			if (!IMCommon.getNetWorkState()) {
				mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
				return;
			}
			new Thread(){
				public void run() {
					try {
						IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG,
								mContext.getResources().getString(R.string.send_request));
						IMResponseState status = IMCommon.getIMServerAPI().favoreiteMoving(mShare.uid, null, favoriteContent);
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

		private void delShare(){
			if (!IMCommon.getNetWorkState()) {
				mBaseHandler.sendEmptyMessage(BASE_MSG_NETWORK_ERROR);
				return;
			}
			new Thread(){
				public void run() {
					try {
						IMCommon.sendMsg(mBaseHandler, BASE_SHOW_PROGRESS_DIALOG, 
								mContext.getResources().getString(R.string.send_request));
						IMResponseState status = IMCommon.getIMServerAPI().deleteShare(mShare.id);
						IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_DEL_SHARE_STATUS, status);
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
					} catch (NotFoundException e) {
						e.printStackTrace();
						mBaseHandler.sendEmptyMessage(BASE_HIDE_PROGRESS_DIALOG);
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



}
