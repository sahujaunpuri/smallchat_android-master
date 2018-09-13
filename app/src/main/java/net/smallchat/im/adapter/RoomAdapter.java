package net.smallchat.im.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.chat.ChatMainActivity;
import net.smallchat.im.room.RoomDetailActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.Room;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.IMCommon;


/**
 * 我的群组数据适配器
 * @author dongli
 *
 */
public class RoomAdapter extends BaseAdapter{

	private final LayoutInflater mInflater;
	HashMap<Integer, View> hashMap;
	private List<Room> mData;
	private List<Login> mUserList;
	private Context mContext;
	private ImageLoader mImageLoader;
	private String mGroupHeadUrl="";

	private int mJump ;

	public RoomAdapter(Context context, List<Room> data,int screenWidth,List<Login> userList,int jump){
		mInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mData = data;
		hashMap= new HashMap<Integer, View>(); 
		mImageLoader = new ImageLoader();
		this.mUserList = userList;
		this.mJump = jump;
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

	public List<Login> getUserList(){
		return mUserList;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		convertView = hashMap.get(position);
		ViewHolder holder;  

		if (convertView==null) {  
			convertView=mInflater.inflate(R.layout.group_list_item, null);   
			holder=new ViewHolder();  

			holder.mUserNameTextView = (TextView) convertView.findViewById(R.id.username);
			holder.mTimeTextView = (TextView) convertView.findViewById(R.id.releasetime);
			holder.mContentTextView = (TextView) convertView.findViewById(R.id.content);
			holder.mMessageCount = (TextView) convertView.findViewById(R.id.message_count);
			holder.mHeaderView = (ImageView) convertView.findViewById(R.id.header);
			holder.mHeaderView.setVisibility(View.GONE);
			holder.mGroupHeaderLayout = (LinearLayout) convertView.findViewById(R.id.group_header);
			holder.mGroupHeaderLayout.setVisibility(View.VISIBLE);
			holder.mMessageCount.setVisibility(View.GONE);
			//设置内容view的大小为屏幕宽度,这样按钮就正好被挤出屏幕外
			holder.content = convertView.findViewById(R.id.ll_content);

			convertView.setTag(holder);  
			hashMap.put(position, convertView);
		}else {
			holder=(ViewHolder) convertView.getTag();  
		}

		final Room room = mData.get(position);

		mGroupHeadUrl ="";


		List<Login> userList = new ArrayList<Login>();
		if(room.mUserList!=null && room.mUserList.size()>0){
			if(userList!=null && userList.size()>0){
				userList.clear();
			}
			userList.addAll(room.mUserList);
		}else{
			Login user = IMCommon.getLoginResult(mContext);
			if(user != null){
				userList.add(user);
			}
		}

		if(userList != null && userList.size() != 0){
			int count = 4;
			if(userList.size() < 4){
				count = userList.size();
			}

			String name = "";
			for (int i = 0; i < count; i++) {
				String displayName = userList.get(i).name;
				if(i == count - 1){
					name += displayName;
					continue;
				}
				name += displayName + ",";
			}

			for (int j = 0; j < count; j++) {
				if(count - 1 == j){
					mGroupHeadUrl += userList.get(j).headSmall;
				}else{
					mGroupHeadUrl += userList.get(j).headSmall+",";
				}
			}

			if(holder.mGroupHeaderLayout.getChildCount() != 0){
				holder.mGroupHeaderLayout.removeAllViews();
			}

			if(count == 1){
				holder.mGroupHeaderLayout.setVisibility(View.GONE);
				holder.mHeaderView.setVisibility(View.VISIBLE);
				mImageLoader.getBitmap(mContext, holder.mHeaderView, null, userList.get(0).headSmall, 0, false, true);
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
						mImageLoader.getBitmap(mContext, imageView, null, userList.get(0).headSmall, 0, false, true);
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
								mImageLoader.getBitmap(mContext, imageView, null, userList.get(2 * i + j - 1).headSmall, 0, false, true);
							}else {
								mImageLoader.getBitmap(mContext, imageView, null, userList.get(2 * i + j).headSmall, 0, false, true);
							}
							imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
							layout.addView(imageView);
							outLayout.addView(layout);
						}
					}

					holder.mGroupHeaderLayout.addView(outLayout);
				}
			}
			if(room.groupName == null || room.groupName.equals("")){
				holder.mUserNameTextView.setText(name+"("+room.groupCount+"人)");
			}else{
				holder.mUserNameTextView.setText(room.groupName+"("+room.groupCount+"人)");
			}

		}else {
			if(room.groupName == null || room.groupName.equals("")){
				holder.mUserNameTextView.setText(room.groupId+"("+room.groupCount+"人)");
			}else{
				holder.mUserNameTextView.setText(room.groupName+"("+room.groupCount+"人)");
			}
		}

		boolean isAdd = true;
		if(mUserList!=null){
			for (int j = 0; j < mUserList.size(); j++) {
				if(mUserList.get(j).room.groupName.equals(room.groupName)){
					isAdd = false;
					break;
				}
			}
			if(isAdd){
				mUserList.add(new Login(room,room.groupName,mGroupHeadUrl,300));
			}
		}else{}

		holder.content.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mJump == 1){
					Login user = new Login();
					user.uid = room.groupId;
					user.nickname = room.groupName;
					user.headSmall = mGroupHeadUrl;
					user.mIsRoom = 300;
					Intent intent = new Intent(mContext, ChatMainActivity.class);
					intent.putExtra("data", user);
					mContext.startActivity(intent);
				}else{
					Intent intent = new Intent(mContext, RoomDetailActivity.class);
					intent.putExtra("room", room);
					intent.putExtra("groupurl", mGroupHeadUrl);
					mContext.startActivity(intent);
				}
				
				//刷新ListView内容
			}
		});


		return convertView;
	}

	final static class ViewHolder {  
		TextView mUserNameTextView;  
		TextView mContentTextView;
		TextView mTimeTextView;
		TextView mMessageCount;
		ImageView mHeaderView;
		LinearLayout mGroupHeaderLayout;
		public View content;

		@Override
		public int hashCode() {
			return this.mUserNameTextView.hashCode() + mContentTextView.hashCode() + mTimeTextView.hashCode() + 
					mMessageCount.hashCode() + mHeaderView.hashCode() + mGroupHeaderLayout.hashCode();
		}
	} 

}
