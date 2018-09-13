package net.smallchat.im.adapter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import net.smallchat.im.Entity.CommentUser;
import net.smallchat.im.Entity.FriendsLoopItem;
import net.smallchat.im.Entity.PopItem;
import net.smallchat.im.Entity.ShowFriendsLoopUser;
import net.smallchat.im.R;
import net.smallchat.im.ShowMultiImageActivity;
import net.smallchat.im.album.MyAlbumActivity;
import net.smallchat.im.components.LocationActivity;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 朋友圈适配器
 * @author dongli
 *
 */
public class FriendsLoopAdapter extends BaseAdapter{
	private final LayoutInflater mInflater;
	private List<FriendsLoopItem> mData;
	private Context mContext;
	private ImageLoader mImageLoader;
	private List<PopItem> mPopMenuString = new ArrayList<PopItem>();
	private Handler mHandler;
	private boolean mIsBusy = false;
	private int mWidth,mSpliteWdith;



	public FriendsLoopAdapter(Context context, List<FriendsLoopItem> data,Handler handler,DisplayMetrics metric){
		mInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mSpliteWdith = metric.widthPixels;
		mWidth = mSpliteWdith - FeatureFunction.dip2px(mContext, 100);;

		mData = data;
		mImageLoader = new ImageLoader();
		mPopMenuString.add(new PopItem(1, "赞"));
		mPopMenuString.add(new PopItem(2, "评论"));
		mHandler = handler;
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

	public void setFlagBusy(boolean isBusy){
		mIsBusy = isBusy;
		//notifyDataSetChanged();
	}


	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 0:
					ShowFriendsLoopUser item = (ShowFriendsLoopUser)msg.obj;
					if(item !=null && item.parentLayout!=null
							&& (item.list!=null && item.list.size()>0)){
					}
					break;
				case 1:
					ShowFriendsLoopUser showItem = (ShowFriendsLoopUser)msg.obj;
					if(showItem!=null && showItem.parentLayout!=null
							&& showItem.childLayout!=null	){
						showItem.parentLayout.addView(showItem.childLayout);
					}
					break;

				default:
					break;
			}
		}

	};


	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final FriendsLoopItem item = mData.get(position);
		final ViewHolder holder;
		if (convertView==null /*|| ((ChatViewHolder)convertView.getTag()).mTag != position*/) {
			convertView=mInflater.inflate(R.layout.friend_circle_item, null);
			holder=new ViewHolder();

			holder.mUserNameTextView = (TextView) convertView.findViewById(R.id.name);
			holder.mContentTextView = (TextView) convertView.findViewById(R.id.content);
			holder.mPicLayout = (LinearLayout)convertView.findViewById(R.id.send_img_layout);
			holder.mTimeTextView = (TextView) convertView.findViewById(R.id.time);
			holder.mFunctionBtn = (Button) convertView.findViewById(R.id.function_btn);
			holder.mZanLayout = (LinearLayout)convertView.findViewById(R.id.zan_layout);
			holder.mCommentLayout = (LinearLayout)convertView.findViewById(R.id.comment_layout);
			holder.mHeaderIcon = (ImageView)convertView.findViewById(R.id.friends_icon);
			holder.mZanIcon = (ImageView)convertView.findViewById(R.id.zan_icon);
			holder.mZanIcon.setVisibility(View.GONE);

			holder.mJumpLayout = (LinearLayout)convertView.findViewById(R.id.jump_layout);
			holder.mZanIconBtn = (LinearLayout)convertView.findViewById(R.id.zan_btn);
			holder.mZanTextView = (TextView)convertView.findViewById(R.id.zan_text);
			holder.mZanBtnIcon = (ImageView)convertView.findViewById(R.id.zan_btn_icon);
			holder.mLocationAddress = (TextView)convertView.findViewById(R.id.location_addr);
			holder.mDelBtn = (Button)convertView.findViewById(R.id.del_btn);
			holder.mCommentIconBtn = (LinearLayout)convertView.findViewById(R.id.comment_btn_layout);
			//	holder.mPopWindows = new PopWindows(mContext, mPopMenuString, holder.mCommentBtn, mInterface);

			holder.mOtherLayout = (LinearLayout)convertView.findViewById(R.id.other_layout);
			holder.mTag = position;
			convertView.setTag(holder);
		}else {
			holder=(ViewHolder) convertView.getTag();
		}

		if(item.uid.equals(IMCommon.getUserId(mContext))){
			holder.mDelBtn.setVisibility(View.VISIBLE);
			holder.mDelBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_DEL_FRIENDS_CIRCLE, position);
				}
			});
		}else{
			holder.mDelBtn.setVisibility(View.GONE);
		}

		if(item.showView == 1){
			holder.mJumpLayout.setVisibility(View.VISIBLE);
		}else{
			holder.mJumpLayout.setVisibility(View.GONE);
		}
		if(item.address!=null && !item.address.equals("")){
			holder.mLocationAddress.setVisibility(View.VISIBLE);
			holder.mLocationAddress.setText(item.address);
			holder.mLocationAddress.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent  = new Intent(mContext, LocationActivity.class);
					intent.putExtra("show", true);
					intent.putExtra("lat", item.lat);
					intent.putExtra("lng",item.lng);
					intent.putExtra("addr", item.address);
					intent.putExtra("fuid", item.uid);
					mContext.startActivity(intent);
				}
			});
		}else{
			holder.mLocationAddress.setVisibility(View.GONE);
		}
		holder.mUserNameTextView.setText(item.nickname);
		if(item.content!=null && !item.content.equals("") && !item.content.equals("null")){
			holder.mContentTextView.setVisibility(View.VISIBLE);
			holder.mContentTextView.setText(EmojiUtil.getExpressionString(mContext, item.content, "emoji_[\\d]{0,3}"));
		}else{
			holder.mContentTextView.setVisibility(View.GONE);
		}
		if (!mIsBusy) {
			if(item.headSmall!=null && !item.headSmall.equals("")){
				mImageLoader.getBitmap(mContext, holder.mHeaderIcon, null, item.headSmall,0,false,true);
				holder.mHeaderIcon.setTag(item.headSmall);
			}else{
				holder.mHeaderIcon.setImageResource(R.drawable.contact_default_header);
			}
		}else{
			if(item.headSmall!=null && !item.headSmall.equals("")){
				if(mImageLoader.getImageBuffer().containsKey(item.headSmall)){
					mImageLoader.getBitmap(mContext, holder.mHeaderIcon, null, item.headSmall,0,false,true);
				}else{
					holder.mHeaderIcon.setImageResource(R.drawable.contact_default_header);
				}
			}else{
				holder.mHeaderIcon.setImageResource(R.drawable.contact_default_header);
			}

		}


		holder.mHeaderIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent profileAlbumIntent = new Intent();
				profileAlbumIntent.setClass(mContext,MyAlbumActivity.class);
				profileAlbumIntent.putExtra("toUserID",item.uid);
				mContext.startActivity(profileAlbumIntent);
			}
		});

		holder.mTimeTextView.setText(FeatureFunction.calculaterReleasedTime(mContext, new Date((item.createtime*1000)),item.createtime*1000,0));

		holder.mZanIconBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				IMCommon.sendMsg(mHandler, GlobalParam.MSG_COMMENT_PRAISE,position);
			}
		});

		holder.mCommentIconBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_BOTTOM_COMMENT_MENU,position);
			}
		});

		holder.mFunctionBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(item.ispraise == 1){
					holder.mZanTextView.setText(mContext.getResources().getString(R.string.cancel));
					holder.mZanBtnIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.friend_circle_praise_btn));
				}else if(item.ispraise == 0){
					holder.mZanTextView.setText(mContext.getResources().getString(R.string.zan_for_me));
					holder.mZanBtnIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.friend_circle_cancle_praise_btn));
				}
				holder.mJumpLayout.setVisibility(View.VISIBLE);
				TranslateAnimation animation = new TranslateAnimation(mSpliteWdith, 0, 0, 0);
				animation.setDuration(500);
				animation.setAnimationListener(mAnimationListener);
				holder.mJumpLayout.startAnimation(animation);
				IMCommon.sendMsg(mHandler, GlobalParam.MSG_CHECK_FRIENDS_LOOP_POP_STATUS, position);
			}
		});



		if (holder.mPicLayout!=null && holder.mPicLayout.getChildCount()>0) {
			holder.mPicLayout.removeAllViews();
		}
		holder.mPicLayout.setTag(item.id);
		if(item.listpic != null && item.listpic.size()>0){
			if(item.listpic.size() ==1){
				//只有一张朋友圈图片
				int padding = FeatureFunction.dip2px(mContext, 2);
				View view = mInflater.inflate(R.layout.picture_item, null);

				LayoutParams params = new LayoutParams((mWidth/3)*2 , mWidth );
				params.setMargins(10,0,10,0);
				view.setLayoutParams(params);
				view.setPadding(padding, padding, padding, padding);
				view.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_FRIENDS_FAVORITE_DIALOG, item, 2, 0);
						return true;
					}
				});

				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext, ShowMultiImageActivity.class);
						intent.putExtra("share", mData.get(position));
						intent.putExtra("hide", 1);
						intent.putExtra("pos", 0);
						mContext.startActivity(intent);
					}
				});
				ImageView imageView = (ImageView) view.findViewById(R.id.pic);
				if (!TextUtils.isEmpty(item.listpic.get(0).originUrl) && !mIsBusy) {
					imageView.setTag(item.listpic.get(0).originUrl);

//                    if (mImageLoader.getImageBuffer().get(item.listpic.get(0).originUrl) == null) {
//                        imageView.setImageBitmap(null);
//                        imageView.setImageResource(R.drawable.normal);
//                    }

					mImageLoader.getBitmap(mContext, imageView, null, item.listpic.get(0).originUrl, 0, true, false);
				} else {
					//imageView.setImageResource(R.drawable.normal);
				}
				holder.mPicLayout.addView(view);
			}else {
				holder.mPicLayout.setVisibility(View.VISIBLE);
				int rows = item.listpic.size() % 3 == 0 ? item.listpic.size() / 3 : item.listpic.size() / 3 + 1;
				int padding = FeatureFunction.dip2px(mContext, 2);

				for (int i = 0; i < rows; i++) {
					LinearLayout layout = new LinearLayout(mContext);
					layout.setOrientation(LinearLayout.HORIZONTAL);
					layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
					for (int j = 0; j < 3; j++) {
						final int pos = i * 3 + j;

						if (pos < item.listpic.size()) {
							View view = mInflater.inflate(R.layout.picture_item, null);
							LayoutParams params = new LayoutParams(mWidth / 3, mWidth / 3);
							view.setLayoutParams(params);
							view.setPadding(padding, padding, padding, padding);
							view.setOnLongClickListener(new OnLongClickListener() {

								@Override
								public boolean onLongClick(View v) {
									IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_FRIENDS_FAVORITE_DIALOG, item, 2, pos);
									return true;
								}
							});

							view.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(mContext, ShowMultiImageActivity.class);
									intent.putExtra("share", mData.get(position));
									intent.putExtra("hide", 1);
									intent.putExtra("pos", pos);
									mContext.startActivity(intent);
								}
							});
							ImageView imageView = (ImageView) view.findViewById(R.id.pic);
							if (!TextUtils.isEmpty(item.listpic.get(pos).smallUrl) && !mIsBusy) {
								imageView.setTag(item.listpic.get(pos).smallUrl);

								if (mImageLoader.getImageBuffer().get(item.listpic.get(pos).smallUrl) == null) {
									//imageView.setImageBitmap(null);
									//imageView.setImageResource(R.drawable.normal);
								}

								mImageLoader.getBitmap(mContext, imageView, null, item.listpic.get(pos).smallUrl, 0, true, false);
							} else {
								//imageView.setImageResource(R.drawable.normal);
							}

							layout.addView(view);
						}

					}
					holder.mPicLayout.addView(layout);
				}
			}
		}else{
			holder.mPicLayout.setVisibility(View.GONE);
		}

		holder.mContentTextView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_FRIENDS_FAVORITE_DIALOG,item,1,0 );
				return true;
			}
		});

		holder.mContentTextView.setOnClickListener(new OnClickListener() {


			@Override
			public void onClick(View v) {
				IMCommon.sendMsg(mHandler, GlobalParam.MSG_SHOW_FRIENDS_FULL_TEXT,item,1,0 );
				TextView txtView=(TextView)v;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					if (txtView.getMaxLines()>10) {
						txtView.setMaxLines(10);
					}else{
						txtView.setMaxLines(1000);
					}
				}

			}
		});

		if((item.praiselist!=null && item.praiselist.size()>0)
				|| (item.replylist!=null && item.replylist.size()>0)){
			holder.mOtherLayout.setVisibility(View.VISIBLE);
		}else{
			holder.mOtherLayout.setVisibility(View.GONE);
		}

		if(holder.mZanLayout!=null && holder.mZanLayout.getChildCount()>0){
			holder.mZanLayout.removeAllViews();
		}

		//赞
		if (item.praiselist!=null) {
			List<CommentUser> zanList = item.praiselist;
			if (zanList!=null && zanList.size()>0) {
				holder.mZanIcon.setVisibility(View.VISIBLE);
				for (int i = 0; i < item.praiselist.size(); i++) {
					TextView tv = new TextView(mContext);
					LayoutParams param = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
					tv.setLayoutParams(param);
					tv.setText(item.praiselist.get(i).nickname);
					tv.setTextColor(mContext.getResources().getColor(R.color.application_friend_circle_user_name));
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						tv.setBackground(mContext.getResources().getDrawable(R.drawable.friend_circle_long_click_bg_color));
					}
					final int pos = i;
					tv.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent profileAlbumIntent = new Intent();
							profileAlbumIntent.setClass(mContext,MyAlbumActivity.class);
							profileAlbumIntent.putExtra("toUserID",item.praiselist.get(pos).uid);
							mContext.startActivity(profileAlbumIntent);

						}
					});
					holder.mZanLayout.addView(tv);
					if (i!=item.praiselist.size()-1) {
						TextView spliteTv = new TextView(mContext);
						LayoutParams spliteTvparam = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
						spliteTv.setLayoutParams(spliteTvparam);
						spliteTv.setText(",");
						holder.mZanLayout.addView(spliteTv);
					}
				}
			}else{
				holder.mZanIcon.setVisibility(View.GONE);
			}
		}else{
			holder.mZanIcon.setVisibility(View.GONE);
		}

		//评论

		if(holder.mCommentLayout!=null && holder.mCommentLayout.getChildCount()>0){
			holder.mCommentLayout.removeAllViews();
		}

		holder.mCommentLayout.setTag(item.id);
		if(item.replylist!=null){
			List<CommentUser> commentList = item.replylist;
			if (commentList!=null && commentList.size()>0) {
				for (int i = 0; i <item.replylist.size(); i++) {
					final int pos = i;
					LinearLayout layout = new LinearLayout(mContext);
					LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
					layout.setLayoutParams(params);
					layout.setOrientation(LinearLayout.HORIZONTAL);

					TextView tvName = new TextView(mContext);
					LayoutParams tvParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
					tvName.setLayoutParams(tvParams);
					tvName.setGravity(LinearLayout.VERTICAL);
					tvName.setText(item.replylist.get(i).nickname+":");
					tvName.setBackground(mContext.getResources().getDrawable(R.drawable.friend_circle_long_click_bg_color));
					tvName.setTextColor(mContext.getResources().getColor(R.color.application_friend_circle_user_name));
					//tvName.setTypeface(QiyueCommon.mTypeface);
					tvName.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent profileAlbumIntent = new Intent();
							profileAlbumIntent.setClass(mContext,MyAlbumActivity.class);
							profileAlbumIntent.putExtra("toUserID",item.replylist.get(pos).uid);
							mContext.startActivity(profileAlbumIntent);
						}
					});
					layout.addView(tvName);

					TextView tvContent = new TextView(mContext);
					LayoutParams contentParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
					tvContent.setLayoutParams(contentParams);
					tvContent.setTextColor(Color.parseColor("#444444"));
					//tvContent.setTypeface(IMCommon.mTypeface);
					if(item.replylist.get(i).content!=null && !item.replylist.get(i).content.equals("")){
						tvContent.setText(EmojiUtil.getExpressionString(mContext,item.replylist.get(i).content, "emoji_[\\d]{0,3}"));
					}
					tvContent.setGravity(Gravity.CENTER_VERTICAL);
					tvContent.setGravity(LinearLayout.VERTICAL);
					layout.addView(tvContent);
					holder.mCommentLayout.addView(layout);
				}
			}
		}
		return convertView;
	}



	final static class ViewHolder {
		int mTag;
		TextView mUserNameTextView;
		TextView mContentTextView;
		LinearLayout mPicLayout;
		LinearLayout mZanLayout;
		LinearLayout mCommentLayout;

		TextView mTimeTextView;
		TextView mLocationAddress;
		LinearLayout mJumpLayout;

		LinearLayout mZanIconBtn;
		LinearLayout mCommentIconBtn;
		TextView mZanTextView;
		ImageView mZanBtnIcon;


		Button mFunctionBtn,mDelBtn;
		// PopWindows mPopWindows;
		ImageView mHeaderIcon;
		ImageView mZanIcon;

		private LinearLayout mOtherLayout;

		@Override
		public int hashCode() {
			return this.mUserNameTextView.hashCode() + mContentTextView.hashCode()
					+ mPicLayout.hashCode() + mTimeTextView.hashCode()
					+ mFunctionBtn.hashCode()//+mPopWindows.hashCode()
					+ mZanLayout.hashCode() + mLocationAddress.hashCode()
					+ mCommentLayout.hashCode() + mHeaderIcon.hashCode()
					+ mZanIcon.hashCode() + mJumpLayout.hashCode()
					+ mZanIconBtn.hashCode() + mCommentIconBtn.hashCode()
					+ mZanTextView.hashCode() + mZanBtnIcon.hashCode()
					+ mOtherLayout.hashCode();
		}
	}

	AnimationListener mAnimationListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			/*  if (!mIsShowPopView) {
                mMenuScrollView.setVisibility(View.VISIBLE);
            }else{
            	mMenuScrollView.setVisibility(View.GONE);
            }
			 */
			//moveListView(mIsShowTypeMenu);
		}
	};

}
