package net.smallchat.im.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smallchat.im.Entity.FriendsLoopItem;
import net.smallchat.im.Entity.Picture;

import net.smallchat.im.R;
import net.smallchat.im.ShowMultiImageActivity;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.friendcircle.FriendCircleDetailActivity;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;

import net.smallchat.im.adapter.EmojiUtil;

import java.util.HashMap;
import java.util.List;

import static net.smallchat.im.global.ChatApplication.TAG;

/**
 * 我的相册数据适配器
 * @author dongli
 *
 */
public class MyAlbumAdpater extends BaseAdapter {

	private final LayoutInflater mInflater;
	private List<FriendsLoopItem> mData;
	private Context mContext;
	private ImageLoader mImageLoader;
	private Handler mHandler;
	private int mItemWidth;
	private String mUserID;
	private String mTodayTime;
	private String mPreTime;

	public MyAlbumAdpater(Context context, List<FriendsLoopItem> data,Handler handler,
						  DisplayMetrics metric,String userID){
		mInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		int layoutWidth = metric.widthPixels;
		int spliteWdith = FeatureFunction.dip2px(mContext, 100);
		this.mItemWidth = spliteWdith /2;
		this.mUserID = userID;
		mData = data;
		mImageLoader = new ImageLoader();
		mHandler = handler;
		mTodayTime = FeatureFunction.formartTime(System.currentTimeMillis()/1000, "yyyy-MM-dd");
		mPreTime = FeatureFunction.dateTime("yyyy-MM-dd");
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mData.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	public void setData(List<FriendsLoopItem> list){
		this.mData = list;
	}

	public HashMap<String, Bitmap> getImageBuffer(){
		return mImageLoader.getImageBuffer();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final FriendsLoopItem weiinfo = mData.get(position);
		final ViewHolder viewHoler;
		if(convertView == null || ((ViewHolder) convertView.getTag()).mTag != position){
			convertView = mInflater.inflate(R.layout.my_album_item, parent, false);
			viewHoler= new ViewHolder();

			viewHoler.mToday =(TextView)convertView.findViewById(R.id.today);
			viewHoler.mMonthText = (TextView)convertView.findViewById(R.id.moth);
			viewHoler.mYearText = (TextView)convertView.findViewById(R.id.year);

			viewHoler.mTimeLayout = (LinearLayout)convertView.findViewById(R.id.time_layout);
			viewHoler.mPicLayout = (RelativeLayout)convertView.findViewById(R.id.headerlayout);
			viewHoler.imgLayout = (LinearLayout)convertView.findViewById(R.id.img_layout);
			viewHoler.mOneImage = (ImageView)convertView.findViewById(R.id.header);


			viewHoler.content = (TextView)convertView.findViewById(R.id.content);
			viewHoler.count = (TextView)convertView.findViewById(R.id.pic_size);

			viewHoler.mSendPhoto = (Button)convertView.findViewById(R.id.send_photo);
			viewHoler.movingLayout = (LinearLayout)convertView.findViewById(R.id.moving_layout);

			if(position == 0 && this.mUserID.equals(IMCommon.getUserId(mContext))){
				viewHoler.mSendPhoto.setVisibility(View.VISIBLE);
				viewHoler.mSendPhoto.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_SELECT_BG_DIALOG);
					}
				});
			}else{
				viewHoler.mSendPhoto.setVisibility(View.GONE);
			}

			viewHoler.mTag = position;
			convertView.setTag(viewHoler);
		}else{
			viewHoler = (ViewHolder) convertView.getTag();
		}
		// Load the image and set it on the ImageView



		TextView name = viewHoler.mToday;

		name.setTypeface(Typeface.create(mContext.getResources().getString(R.string.font_family), Typeface.NORMAL));

		String time = FeatureFunction.formartTime(weiinfo.createtime, "yyyy-MM-dd");
		String preTime = "";
		if(position > 0){
			int preIndex = position - 1;
			preTime = FeatureFunction.formartTime(mData.get(preIndex).createtime, "yyyy-MM-dd");
		}
		if(preTime.equals(time)){
			name.setText("");
			viewHoler.mTimeLayout.setVisibility(View.INVISIBLE);
		}else{
			viewHoler.mTimeLayout.setVisibility(View.VISIBLE);
			Log.d(TAG,"mTodayTime==="+mTodayTime+" time==="+time);
			if(time.equals(mTodayTime)){
				name.setText("今天");
			}else if(time.equals(mPreTime)){
				name.setText("昨天");
			}else{
				if(mTodayTime!=null && time!=null){
					String[] todayArray = mTodayTime.split("-");
					String[] preArray = time.split("-");
					if((todayArray!=null && todayArray.length>0)
							&& (preArray!=null && preArray.length>0)){
						if(todayArray[0].equals(preArray[0])){
							name.setText(preArray[2]);
							String month = preArray[1];
							if(month.startsWith("0")){
								month = month.substring(1);
							}
							viewHoler.mMonthText.setText(month+"月");
						}else{
							viewHoler.mYearText.setText(preArray[0]);
						}
					}
				}else{
					name.setText(time);
				}
			}
		}

		if(weiinfo.id!=0){
			viewHoler.movingLayout.setVisibility(View.VISIBLE);

			viewHoler.content.setText(EmojiUtil.getExpressionString(mContext, weiinfo.content, ChatMessageAdapter.EMOJIREX));

			viewHoler.movingLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(weiinfo.listpic!=null && weiinfo.listpic.size()>0){
						Intent intent = new Intent(mContext, ShowMultiImageActivity.class);
						intent.putExtra("share", mData.get(position));
						intent.putExtra("pos", 0);
						mContext.startActivity(intent);
					}else{
						Intent intent = new Intent();
						intent.setClass(mContext, FriendCircleDetailActivity.class);
						intent.putExtra("item", mData.get(position));
						mContext.startActivity(intent);
					}

				}
			});

			viewHoler.imgLayout.setTag(weiinfo.id);
			viewHoler.imgLayout.removeAllViews();
			List<Picture> albumList  = weiinfo.listpic;
			if(albumList!=null && albumList.size()>0){
				viewHoler.mPicLayout.setVisibility(View.VISIBLE);
				int count = 4;
				if(albumList.size()<=4){
					count = albumList.size();
				}
				viewHoler.count.setText(albumList.size()+mContext.getResources().getString(R.string.zhang));

				if(count == 1){
					viewHoler.mOneImage.setVisibility(View.VISIBLE);
					viewHoler.imgLayout.setVisibility(View.GONE);
					mImageLoader.getBitmap(mContext,viewHoler.mOneImage, null, albumList.get(0).smallUrl, 0, false, true);
				}else{

					if(count == 2){

						int padding = FeatureFunction.dip2px(mContext, 2);
						int height = FeatureFunction.dip2px(mContext, 100);

						for (int i = 0; i < count; i++) {
							LinearLayout layout = new LinearLayout(mContext);
							layout.setOrientation(LinearLayout.VERTICAL);
							LinearLayout.LayoutParams params = new LayoutParams(mItemWidth, height);
							layout.setPadding(padding, padding, padding, padding);
							layout.setLayoutParams(params);
							ImageView imageView = new ImageView(mContext);
							imageView.setScaleType(ScaleType.CENTER_CROP);
							imageView.setImageResource(R.drawable.contact_default_header);
							mImageLoader.getBitmap(mContext, imageView, null,albumList.get(i).smallUrl , 0, false, false);
							imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
							layout.addView(imageView);

							viewHoler.imgLayout.addView(layout);
						}

					}else{
						viewHoler.mOneImage.setVisibility(View.GONE);
						viewHoler.imgLayout.setVisibility(View.VISIBLE);
						boolean single = count % 2 == 0 ? false : true;
						int rows = count % 2 == 0 ? count / 2 :count/ 2 + 1;
						int padding = FeatureFunction.dip2px(mContext, 2);
						for (int i = 0; i < rows; i++) {
							LinearLayout layout = new LinearLayout(mContext);
							layout.setOrientation(LinearLayout.VERTICAL);
							layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
							if(single && i == 0){
								int height = FeatureFunction.dip2px(mContext, 100);
								LinearLayout sLayout = new LinearLayout(mContext);
								LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mItemWidth, height);
								sLayout.setPadding(padding, padding, padding, padding);
								sLayout.setLayoutParams(params);
								ImageView imageView = new ImageView(mContext);
								imageView.setScaleType(ScaleType.CENTER_CROP);
								imageView.setImageResource(R.drawable.contact_default_header);
								mImageLoader.getBitmap(mContext, imageView, null,albumList.get(0).smallUrl , 0, false, false);
								imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
								sLayout.addView(imageView);
								layout.addView(sLayout);

							}else{

								for (int j = 0; j < 2; j++) {
									int pos = i * 2 + j;
									if(single){
										pos --;
									}
									if(pos < albumList.size()){
										View view = mInflater.inflate(R.layout.picture_item, null);
										LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mItemWidth,mItemWidth);
										view.setLayoutParams(params);
										view.setPadding(padding, padding, padding, padding);
										ImageView imageView = (ImageView) view.findViewById(R.id.pic);
										if(!TextUtils.isEmpty(albumList.get(pos).smallUrl)){
											imageView.setTag(albumList.get(pos).smallUrl);

											if(mImageLoader.getImageBuffer().get(albumList.get(pos).smallUrl) == null){
												imageView.setImageBitmap(null);
												imageView.setImageResource(R.drawable.default_image);
											}
											mImageLoader.getBitmap(mContext, imageView, null, albumList.get(pos).smallUrl, 0, false, false);
										}else {
											imageView.setImageResource(R.drawable.default_image);
										}

										layout.addView(view);

									}

								}
							}
							viewHoler.imgLayout.addView(layout);
						}

					}
				}
			}else{
				viewHoler.mPicLayout.setVisibility(View.GONE);
				viewHoler.mOneImage.setVisibility(View.GONE);
				viewHoler.imgLayout.setVisibility(View.GONE);
			}
		}else{
			viewHoler.movingLayout.setVisibility(View.GONE);
		}

		return convertView;
	}

	final static class ViewHolder {
		public int mTag;
		public TextView mToday,mMonthText,mYearText;
		public Button mSendPhoto;
		public LinearLayout mTimeLayout;
		public LinearLayout imgLayout;
		public LinearLayout movingLayout;
		public RelativeLayout mPicLayout;
		public ImageView mOneImage;

		public TextView content;
		public TextView count ;

		@Override
		public int hashCode() {
			return  mToday.hashCode() + mTimeLayout.hashCode()
					+ mMonthText.hashCode() + mYearText.hashCode()
					+ mSendPhoto.hashCode()+movingLayout.hashCode()
					+mPicLayout.hashCode();

		}
	}

}
