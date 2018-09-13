package net.smallchat.im.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import net.smallchat.im.Entity.Login;
import net.smallchat.im.R;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.IMCommon;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.room.RoomDetailActivity;

/**
 * 搜索用户数据适配器
 * @author dongli
 *
 */
public class SearchUserListAdapter extends BaseAdapter{

	private final LayoutInflater mInflater;
	HashMap<Integer, View> hashMap;
	private List<Login> mData;
	private ImageLoader mImageLoader;
	private Context mContext;
	private int mIsHide;
	public SearchUserListAdapter(Context context, List<Login> data,int isHide){
		mInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mData = data;
		hashMap= new HashMap<Integer, View>(); 
		mImageLoader = new ImageLoader();
		mIsHide = isHide;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}



	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		convertView = hashMap.get(position);
		ViewHolder holder;  

		if (convertView==null) {  
			convertView=mInflater.inflate(R.layout.contact_item, null);   
			holder=new ViewHolder();  

			holder.mUserNameTextView = (TextView) convertView.findViewById(R.id.username);
			holder.mTimeTextView = (TextView) convertView.findViewById(R.id.releasetime);
			holder.mContentTextView = (TextView) convertView.findViewById(R.id.prompt);
			holder.mGroupNameView = (TextView) convertView.findViewById(R.id.sortKey);
			holder.mHeaderView = (ImageView) convertView.findViewById(R.id.headerimage);
			holder.mGroupLayout = (RelativeLayout) convertView.findViewById(R.id.grouplayout);
			holder.mUserDetailLayout = (RelativeLayout)convertView.findViewById(R.id.user_detail_layout);
			holder.mGroupHeaderLayout   = (LinearLayout) convertView.findViewById(R.id.group_header);
			convertView.setTag(holder);  
			hashMap.put(position, convertView);
		}else {
			holder=(ViewHolder) convertView.getTag();  
		}

		final Login user = mData.get(position);

		if(user.mIsRoom == 100){//不是房间消息
			holder.mHeaderView.setVisibility(View.VISIBLE);
			holder.mGroupHeaderLayout.setVisibility(View.GONE);
			if(user.headSmall != null && !user.headSmall.equals("")){
				mImageLoader.getBitmap(mContext, holder.mHeaderView, null, user.headSmall, 0, false, true);
			}
		}else{

			String[] headUrlArray;
			if(user.headSmall!=null && !user.headSmall.equals("")){
				headUrlArray = user.headSmall.split(",");
			}else{
				headUrlArray = new String[]{IMCommon.getLoginResult(mContext).headSmall};
			}

			if(headUrlArray != null && headUrlArray.length!= 0){
				int count = 4;
				if(headUrlArray.length < 4){
					count = headUrlArray.length;
				}



				if(count == 1){
					holder.mGroupHeaderLayout.setVisibility(View.GONE);
					holder.mHeaderView.setVisibility(View.VISIBLE);
					mImageLoader.getBitmap(mContext, holder.mHeaderView, null, headUrlArray[0], 0, false, true);
				}else {
					holder.mGroupHeaderLayout.setVisibility(View.VISIBLE);
					holder.mHeaderView.setVisibility(View.GONE);

					boolean single = count % 2 == 0 ? false : true;
					int row = !single ? count / 2 : count / 2 + 1;
					for (int i = 0; i < row; i++) {
						LinearLayout outLayout = new LinearLayout(mContext);
						outLayout.setOrientation(LinearLayout.HORIZONTAL);
						int width = FeatureFunction.dip2px(mContext, 23);
						outLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, width));
						int padding = FeatureFunction.dip2px(mContext, 1);
						if(single && i == 0){
							LinearLayout layout = new LinearLayout(mContext);
							LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
							layout.setPadding(padding, padding, padding, padding);
							layout.setLayoutParams(params);
							ImageView imageView = new ImageView(mContext);
							imageView.setImageResource(R.drawable.contact_default_header);
							mImageLoader.getBitmap(mContext, imageView, null, headUrlArray[0], 0, false, true);
							imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
							layout.addView(imageView);
							outLayout.setGravity(Gravity.CENTER_HORIZONTAL);
							outLayout.addView(layout);
						}else {
							for (int j = 0; j < 2; j++) {
								LinearLayout layout = new LinearLayout(mContext);
								LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
								layout.setPadding(padding, padding, padding, padding);
								layout.setLayoutParams(params);
								ImageView imageView = new ImageView(mContext);
								imageView.setImageResource(R.drawable.contact_default_header);
								if(single){
									mImageLoader.getBitmap(mContext, imageView, null, headUrlArray[(2 * i + j - 1)], 0, false, true);
								}else {
									mImageLoader.getBitmap(mContext, imageView, null, headUrlArray[(2 * i + j)], 0, false, true);
								}
								imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
								layout.addView(imageView);
								outLayout.addView(layout);
							}
						}

						holder.mGroupHeaderLayout.addView(outLayout);
					}
				}
			}
		}



		String name = user.nickname;

		holder.mUserNameTextView.setText(name);

		if(user.mIsRoom!=100){
			holder.mContentTextView.setVisibility(View.GONE);
		}else{
			if(user.sign != null){
				holder.mContentTextView.setText(user.sign);
			}
			holder.mContentTextView.setVisibility(View.VISIBLE);
		}
		
		holder.mUserDetailLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent userIntent = new Intent();
				if(user.mIsRoom != 100){
					userIntent = new Intent(mContext, RoomDetailActivity.class);
					userIntent.putExtra("room", user.room);
					userIntent.putExtra("groupurl", user.headSmall);
				}else{
					userIntent.setClass(mContext, UserInfoActivity.class);
					userIntent.putExtra("type",2);
					userIntent.putExtra("uid",user.uid);
					userIntent.putExtra("ishide", mIsHide);
				}
				if(userIntent!=null){
					mContext.startActivity(userIntent);
				}

			}
		});
		holder.mGroupLayout.setVisibility(View.GONE);
		return convertView;
	}


	final static class ViewHolder {  
		TextView mUserNameTextView;  
		TextView mContentTextView;
		TextView mTimeTextView;
		ImageView mHeaderView;
		TextView mGroupNameView;
		RelativeLayout mGroupLayout,mUserDetailLayout;
		LinearLayout mGroupHeaderLayout;

		@Override
		public int hashCode() {
			return this.mUserNameTextView.hashCode() + mContentTextView.hashCode() + mTimeTextView.hashCode() + 
					mGroupNameView.hashCode() + mHeaderView.hashCode() + mGroupLayout.hashCode()
					+mGroupHeaderLayout.hashCode();
		}
	} 

}
