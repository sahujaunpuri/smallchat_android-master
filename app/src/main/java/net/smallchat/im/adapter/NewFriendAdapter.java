package net.smallchat.im.adapter;

import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.Entity.NewFriendItem;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.IMCommon;

/**
 * 搜索用户结果数据集
 * @author dongli
 *
 */
public class NewFriendAdapter extends BaseAdapter{
	
	private final LayoutInflater mInflater;
	HashMap<Integer, View> hashMap;
	private List<NewFriendItem> mData;
	private Context mContext;
	private ImageLoader mImageLoader;
	private Handler mHandler;
	
	public NewFriendAdapter(Context context, List<NewFriendItem> data,Handler handler){
		mInflater = (LayoutInflater)context.getSystemService(
	            Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mData = data;
		hashMap= new HashMap<Integer, View>(); 
		mImageLoader = new ImageLoader();
		mHandler = handler;
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

	@SuppressLint("NewApi")
	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		convertView = hashMap.get(position);
        ViewHolder holder;  
        
        if (convertView==null) {  
        	convertView=mInflater.inflate(R.layout.block_item, null);   
            holder=new ViewHolder();  
              
            holder.mUserNameTextView = (TextView) convertView.findViewById(R.id.username);
            holder.mContentTextView = (TextView) convertView.findViewById(R.id.sign);
            holder.mHeaderView = (ImageView) convertView.findViewById(R.id.header);
            holder.mCancelBtn = (Button) convertView.findViewById(R.id.cancelbtn);
            holder.mCancelBtn.setTextColor(mContext.getResources().getColor(R.color.application_black));
            convertView.setTag(holder);  
            hashMap.put(position, convertView);
        }else {
        	holder=(ViewHolder) convertView.getTag();  
		}
        
        final NewFriendItem user = mData.get(position);
        if(user != null){
        	LayoutParams layoutparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        	
        	if(user.colorBgtype == 1){//新的
        		convertView.setBackgroundResource(R.drawable.last_new_friend);
        	}else{
        		convertView.setBackgroundResource(R.drawable.double_splite);
        	}
        	switch (user.type) {
			case 0://添加 好友申请
				holder.mCancelBtn.setBackground(mContext.getResources().getDrawable(R.drawable.small_red_btn));
				holder.mCancelBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.add));
				holder.mCancelBtn.setTextColor(Color.parseColor("#ffffff"));
				break;
			case 3://同意好友的请求
				holder.mCancelBtn.setBackground(mContext.getResources().getDrawable(R.drawable.invite_friends_btn));
				holder.mCancelBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.accept));
				break;
			case 1://已添加
				holder.mCancelBtn.setBackgroundDrawable(null);
				holder.mCancelBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.has_added));
				break;
			case 2://等待验证
				holder.mCancelBtn.setBackgroundDrawable(null);
				holder.mCancelBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.validing));
				holder.mCancelBtn.setTextColor(Color.parseColor("#2d2d2d"));
				break;

			default:
				break;
			}
        	String nickName = user.name;
        	if(nickName == null || nickName.equals("")){
        		nickName = user.phone;
        	}
        	holder.mUserNameTextView.setText(nickName);
        	String contactName = user.contactName;
        	if(contactName==null || contactName.equals("")){
        		contactName = user.phone;
        	}
        	if(user.fromtype == 1){
        		holder.mContentTextView.setText(contactName);
        	}else{
        		holder.mContentTextView.setText("手机联系人："+contactName);
        	}
        	
        	
        	if(user.headSmall != null && !user.headSmall.equals("")){
        		mImageLoader.getBitmap(mContext, holder.mHeaderView, null, user.headSmall, 0, false, true);
        	}
        }
        
        holder.mCancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (user.type) {
				case 0://发送添加好友邀请
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_VALID_FRIENDS,position);
					break;
				case 3://添加好友
					IMCommon.sendMsg(mHandler, GlobalParam.MSG_AGREE_ADD_FRIENDS_REQUEST,position);
					break;
				case 2://等待验证不处理
					break;

				default:
					break;
				}
			
			}
		});
        
        return convertView;
	}

	
	final static class ViewHolder {  
        TextView mUserNameTextView;  
        TextView mContentTextView;
        ImageView mHeaderView;
        Button mCancelBtn;
        
        @Override
        public int hashCode() {
			return this.mUserNameTextView.hashCode() + mContentTextView.hashCode() + 
					mCancelBtn.hashCode() + mHeaderView.hashCode();
        }
    } 

}
