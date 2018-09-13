package net.smallchat.im.adapter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.MessagePrivacyMode;
import net.smallchat.im.R;
import net.smallchat.im.DB.DBHelper;
import net.smallchat.im.DB.MessageTable;
import net.smallchat.im.DB.UserTable;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.Session;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.IMCommon;

/**
 * 聊天页面适配器
 * @author dongli
 *
 */
public class ChatTabAdapter extends BaseAdapter{

	private final LayoutInflater mInflater;
	HashMap<Integer, View> hashMap;
	private List<Session> mData;
	private List<Login> mUserList;
	private Context mContext;
	private ImageLoader mImageLoader;


	public ChatTabAdapter(Context context, List<Session> data){
		mInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mData = data;
		hashMap= new HashMap<Integer, View>(); 
		mImageLoader = new ImageLoader();
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

	public void setData(List<Session> data){
		mData = data;
	}



	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		convertView = hashMap.get(position);
		ViewHolder holder;  

		if (convertView==null) {  
			convertView=mInflater.inflate(R.layout.chat_item, null);
			holder=new ViewHolder();  

			holder.mParentLayout = (LinearLayout)convertView.findViewById(R.id.hsv);
			holder.mUserNameTextView = (TextView) convertView.findViewById(R.id.username);
			holder.mTimeTextView = (TextView) convertView.findViewById(R.id.releasetime);
			holder.mContentTextView = (TextView) convertView.findViewById(R.id.content);
			holder.mMessageCount = (TextView) convertView.findViewById(R.id.message_count);
			holder.mHeaderView = (ImageView) convertView.findViewById(R.id.header);
			holder.mGroupHeaderLayout = (LinearLayout) convertView.findViewById(R.id.group_header);

			convertView.setTag(holder);  
			hashMap.put(position, convertView);
		}else {
			holder=(ViewHolder) convertView.getTag();  
		}

		final Session session = mData.get(position);
		final SQLiteDatabase dbDatabase = DBHelper.getInstance(mContext).getWritableDatabase();
		UserTable userTable = new UserTable(dbDatabase);
		final MessageTable messageTable = new MessageTable(dbDatabase);

		final int index = position;

		if(session.isTop != 0){
			holder.mParentLayout.setBackgroundResource(R.drawable.last_new_friend);
		}else{
			holder.mParentLayout.setBackgroundResource(R.drawable.devider_line_n);
		}
		if(session.type == ChatType.ContentPushMessage){//内容推荐
			holder.mGroupHeaderLayout.setVisibility(View.GONE);
			holder.mHeaderView.setVisibility(View.VISIBLE);

			holder.mUserNameTextView.setText("最新推荐");
			holder.mHeaderView.setImageResource(R.drawable.notify_msg_content_push);
		}else if(session.type == ChatType.FileTransferMessage){//文件传输
			holder.mGroupHeaderLayout.setVisibility(View.GONE);
			holder.mHeaderView.setVisibility(View.VISIBLE);

			holder.mUserNameTextView.setText("文件传输");
			holder.mHeaderView.setImageResource(R.drawable.notify_msg_file_transfer);

		}else if(session.type == ChatType.ServiceMessage){//服务通知
			holder.mGroupHeaderLayout.setVisibility(View.GONE);
			holder.mHeaderView.setVisibility(View.VISIBLE);

			holder.mUserNameTextView.setText("服务通知");
			holder.mHeaderView.setImageResource(R.drawable.notify_msg_service);

		}else if(session.type == ChatType.PayMessage){//支付通知
			holder.mGroupHeaderLayout.setVisibility(View.GONE);
			holder.mHeaderView.setVisibility(View.VISIBLE);

			holder.mUserNameTextView.setText("支付通知");
			holder.mHeaderView.setImageResource(R.drawable.notify_msg_pay);

		}else if(session.type == ChatType.SystemMessage){//系统通知
			holder.mGroupHeaderLayout.setVisibility(View.GONE);
			holder.mHeaderView.setVisibility(View.VISIBLE);

			holder.mUserNameTextView.setText("系统通知");
			holder.mHeaderView.setImageResource(R.drawable.notify_msg_system);

		}else if(session.type == ChatType.PrivateMessage){//单聊
			holder.mGroupHeaderLayout.setVisibility(View.GONE);
			holder.mHeaderView.setVisibility(View.VISIBLE);

			holder.mUserNameTextView.setText(session.name);
			holder.mHeaderView.setImageResource(R.drawable.contact_default_header);
			if(session.heading != null && !session.heading.equals("")){
				mImageLoader.getBitmap(mContext, holder.mHeaderView, null, session.heading, 0, false, true);
			}else {
				holder.mHeaderView.setImageResource(R.drawable.contact_default_header);
			}

		}else {//群聊消息
			String[] headUrlArray;
			if(session.heading!=null && !session.heading.equals("")){
				headUrlArray = session.heading.split(",");
			}else{
				headUrlArray = new String[]{IMCommon.getLoginResult(mContext).headSmall};
			}


			List<String> headUrlList = new ArrayList<String>();

			if(headUrlArray != null && headUrlArray.length!= 0){
				int count = 4;
				if(headUrlArray.length < 4){
					count = headUrlArray.length;
				}

				if(holder.mGroupHeaderLayout.getChildCount() != 0){
					holder.mGroupHeaderLayout.removeAllViews();
				}

				if(count == 1){
					holder.mGroupHeaderLayout.setVisibility(View.GONE);
					holder.mHeaderView.setVisibility(View.VISIBLE);
					mImageLoader.getBitmap(mContext, holder.mHeaderView, null, headUrlArray[0], 0, false, true);
					headUrlList.add(headUrlArray[0]);
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
							headUrlList.add(headUrlArray[0]);
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
								if(single){
									headUrlList.add(headUrlArray[(2 * i + j - 1)]);
									if(headUrlArray[(2 * i + j - 1)] == null || headUrlArray[(2 * i + j - 1)].equals("")){
										imageView.setImageResource(R.drawable.contact_default_header);
									}else{
										mImageLoader.getBitmap(mContext, imageView, null, headUrlArray[(2 * i + j - 1)], 0, false, true);
									}
								}else {
									headUrlList.add(headUrlArray[(2 * i + j)]);
									if(headUrlArray[(2 * i + j)] == null || headUrlArray[(2 * i + j)].equals("")){
										imageView.setImageResource(R.drawable.contact_default_header);
									}else{
										mImageLoader.getBitmap(mContext, imageView, null, headUrlArray[(2 * i + j)], 0, false, true);
									}
								}
								imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
								layout.addView(imageView);
								outLayout.addView(layout);
							}
						}

						holder.mGroupHeaderLayout.addView(outLayout);
					}
				}
				holder.mUserNameTextView.setText(session.name);
				//showGrouName = session.name; 
			}
		}

		holder.mMessageCount.setText(String.valueOf(session.mUnreadCount));
		if(session.mUnreadCount == 0){
			holder.mMessageCount.setVisibility(View.GONE);
		}else if(session.mUnreadCount > 0){
			holder.mMessageCount.setVisibility(View.VISIBLE);
		}
		if(session.mChatMessage !=null){
			if(session.mChatMessage.time != 0){
				holder.mTimeTextView.setText(FeatureFunction.calculaterReleasedTime(mContext,
						new Date(session.mChatMessage.time),session.mChatMessage.time,0));
			}

			if(mData.get(position).mChatMessage.messageType == MessageType.TEXT){
				if(mData.get(position).mChatMessage.privacyMode == MessagePrivacyMode.PrivacyModeEnabled){
					//隐私模式消息
					holder.mContentTextView.setText("[" + mContext.getString(R.string.privacy_mode_message) + "]");
				}else {
					holder.mContentTextView.setText(EmojiUtil.getExpressionString(mContext, mData.get(position).mChatMessage.content, ChatMessageAdapter.EMOJIREX));
				}

			}else if(mData.get(position).mChatMessage.messageType == MessageType.AUDIO){
				//显示语音消息
				holder.mContentTextView.setText("[" + mContext.getString(R.string.message_type_voice) + "]");
			}else if(mData.get(position).mChatMessage.messageType == MessageType.IMAGE){
				holder.mContentTextView.setText("[" + mContext.getString(R.string.message_type_picture) + "]");
			}else if(mData.get(position).mChatMessage.messageType == MessageType.LOCATION){
				holder.mContentTextView.setText("[" + mContext.getString(R.string.message_type_location) + "]");
			}else if(mData.get(position).mChatMessage.messageType == MessageType.CARD){
				holder.mContentTextView.setText("[" + mContext.getString(R.string.message_type_contact_card) + "]");
			}else if(mData.get(position).mChatMessage.messageType == MessageType.VIDEO){
				holder.mContentTextView.setText("[" + mContext.getString(R.string.message_type_small_video) + "]");
			}else if(mData.get(position).mChatMessage.messageType == MessageType.TRANSFER){
				holder.mContentTextView.setText("[" + mContext.getString(R.string.message_type_transfer) + "]");
			}else if(mData.get(position).mChatMessage.messageType == MessageType.REDPACKET){
				holder.mContentTextView.setText("[" + mContext.getString(R.string.message_type_redpacket) + "]");
			}
		}else{
			holder.mContentTextView.setText("");
			holder.mTimeTextView.setText("");
		}

		return convertView;
	}


	final static class ViewHolder {  
		TextView mUserNameTextView;  
		TextView mContentTextView;
		TextView mTimeTextView;
		TextView mMessageCount;
		ImageView mHeaderView;
		LinearLayout mGroupHeaderLayout,mParentLayout;

		@Override
		public int hashCode() {
			return this.mUserNameTextView.hashCode() + mContentTextView.hashCode() + mTimeTextView.hashCode() + 
					mMessageCount.hashCode() + mHeaderView.hashCode() + mGroupHeaderLayout.hashCode()
					+ mParentLayout.hashCode();
		}
	} 

}
