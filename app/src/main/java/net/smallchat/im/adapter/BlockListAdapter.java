package net.smallchat.im.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.meeting.ApplyMetListActivity;
import net.smallchat.im.contact.BlockListActivity;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.GlobleType;
import net.smallchat.im.global.ImageLoader;

/**
 * 黑名单适配器
 * @author dongli
 *
 */
public class BlockListAdapter extends BaseAdapter{

	private final LayoutInflater mInflater;
	HashMap<Integer, View> hashMap;
	private List<Login> mData;
	private Context mContext;
	private ImageLoader mImageLoader;
	private int mType; //0-黑名单 1-关注 列表 2-申请会议用户

	public BlockListAdapter(Context context, List<Login> data,int type){
		mInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mData = data;
		hashMap= new HashMap<Integer, View>(); 
		mImageLoader = new ImageLoader();
		mType = type;
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

			if(mType == GlobleType.BLOCKLISTACTIVITY_TOP_MEETING_USER_TYPE){
				holder.mCancelBtn.setVisibility(View.VISIBLE);
				holder.mCancelBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.remove));
			}else  if(mType !=GlobleType.BLOCKLISTACTIVITY_BLOCK_TYPE){
				holder.mCancelBtn.setVisibility(View.GONE);
			}
			else{
				holder.mCancelBtn.setText(ChatApplication.getInstance().getResources().getString(R.string.cancel_block));
			}

			convertView.setTag(holder);  
			hashMap.put(position, convertView);
		}else {
			holder=(ViewHolder) convertView.getTag();  
		}

		final Login user = mData.get(position);
		if(user != null){
			holder.mUserNameTextView.setText(user.nickname);
			if(mType == GlobleType.BLOCKLISTACTIVITY_RECOMMEND_TYPE){
				holder.mContentTextView.setText("注册时间："+FeatureFunction.formartTime(user.createtime, "yyyy.MM.dd"));
			}else{
				if(mType == GlobleType.BLOCKLISTACTIVITY_BLOCK_TYPE){
					holder.mContentTextView.setVisibility(View.GONE);
				}else{
					holder.mContentTextView.setVisibility(View.VISIBLE);
					holder.mContentTextView.setText(user.sign);
				}
			}	

			if(user.headSmall != null && !user.headSmall.equals("")){
				mImageLoader.getBitmap(mContext, holder.mHeaderView, null, user.headSmall, 0, false, true);
			}
		}

		holder.mCancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mType == 0){
					Intent intent = new Intent(BlockListActivity.CANCEL_ACTION);
					intent.putExtra("fuid", user.uid);
					/*intent.putExtra("isFollow", user.isfollow==0?1:0);*/
					mContext.sendBroadcast(intent);
				}else if(mType == 1){
					Intent intent = new Intent(BlockListActivity.CANCEL_FOLLOW_ACTION);
					intent.putExtra("fuid", user.uid);
					/*intent.putExtra("isFollow", user.isfollow==0?1:0);*/
					mContext.sendBroadcast(intent);
				}else if(mType == GlobleType.BLOCKLISTACTIVITY_TOP_MEETING_USER_TYPE){
					Intent intent = new Intent(ApplyMetListActivity.REMOVE_USER_ACTION);
					intent.putExtra("fuid", user.uid);
					intent.putExtra("pos", position);
					mContext.sendBroadcast(intent);
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
