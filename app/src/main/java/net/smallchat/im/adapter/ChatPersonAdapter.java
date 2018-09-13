package net.smallchat.im.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.Entity.ChatDetailEntity;
import net.smallchat.im.global.ImageLoader;

/**
 * 聊天用户适配器
 * @author dongli
 *
 */
public class ChatPersonAdapter extends BaseAdapter{

	private final LayoutInflater mInflater;
	HashMap<Integer, View> hashMap;
	private List<ChatDetailEntity> mData;
	private Context mContext;
	private ImageLoader mImageLoader;
	private boolean mIsDelete = false;
	private boolean mIsShowNickName = true;
	private boolean mIsShowAddBtn;

	public ChatPersonAdapter(Context context, List<ChatDetailEntity> data){
		mInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mData = data;
		hashMap= new HashMap<Integer, View>(); 
		mImageLoader = new ImageLoader();
	}

	@Override
	public int getCount() {
		int columns = mData.size() / 4;
		if(mData.size() % 4 != 0){
			return (columns + 1) * 4;
		}else {
			mData.size();
		}
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		if(position < mData.size()){
			return mData.get(position);
		}else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setIsDelete(boolean isDelete){
		mIsDelete = isDelete;
	}

	public boolean getIsDelete(){
		return mIsDelete;
	}

	public void setIsShowNickName(boolean isShowNickName){
		mIsShowNickName = isShowNickName;
	}

	public boolean getIsShowNickName(){
		return mIsShowNickName;
	}

	public void setIsShowAddBtn(boolean isShowBtn){
		mIsShowAddBtn = isShowBtn;
	}

	//username
	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		convertView = hashMap.get(position);
		ViewHolder holder;  

		if (convertView==null) {  
			convertView=mInflater.inflate(R.layout.chat_detail_person_item, null);   
			holder=new ViewHolder();  

			holder.mUserNameTextView = (TextView) convertView.findViewById(R.id.username);
			holder.mHeaderView = (ImageView) convertView.findViewById(R.id.header);
			holder.mDeleteBtn = (ImageView) convertView.findViewById(R.id.deletebtn);
			convertView.setTag(holder);  
			hashMap.put(position, convertView);
		}else {
			holder=(ViewHolder) convertView.getTag();  
		}

		holder.mDeleteBtn.setVisibility(View.GONE);

		if(position < mData.size()){
			if(mData.get(position).mType == 0){
				holder.mHeaderView.setVisibility(View.VISIBLE);
				holder.mUserNameTextView.setVisibility(View.VISIBLE);
				if(mIsDelete){
					holder.mDeleteBtn.setVisibility(View.VISIBLE);
				}

				if(mData.get(position).mLogin != null){
					if(mData.get(position).mLogin.headSmall != null && !mData.get(position).mLogin.headSmall.equals("")){
						mImageLoader.getBitmap(mContext, holder.mHeaderView, null, mData.get(position).mLogin.headSmall, 0, false, true);
					}else{
						holder.mHeaderView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.contact_default_header));
					}
					if(mIsShowNickName){
						holder.mUserNameTextView.setVisibility(View.VISIBLE);
						String displayName = mData.get(position).mLogin.nickname;
						holder.mUserNameTextView.setText(displayName);

					}else{
						holder.mUserNameTextView.setVisibility(View.INVISIBLE);
					}

				}
			}else {
				holder.mUserNameTextView.setVisibility(View.INVISIBLE);
				if(mIsShowAddBtn){
					holder.mHeaderView.setVisibility(View.VISIBLE);
				}else{
					if(mIsDelete){
						holder.mHeaderView.setVisibility(View.INVISIBLE);
					}else {
						holder.mHeaderView.setVisibility(View.VISIBLE);
					}
				}

				if(mData.get(position).mType == 1){
					holder.mHeaderView.setImageResource(R.drawable.smiley_add_btn);
				}else if(mData.get(position).mType == 2){
					holder.mHeaderView.setImageResource(R.drawable.smiley_minus_btn);
				}
			}
		}else {
			holder.mHeaderView.setVisibility(View.INVISIBLE);
			holder.mUserNameTextView.setVisibility(View.INVISIBLE);
		}


		return convertView;
	}


	final static class ViewHolder {  
		TextView mUserNameTextView;  
		ImageView mHeaderView;
		private ImageView mDeleteBtn;

		@Override
		public int hashCode() {
			return this.mUserNameTextView.hashCode() + mHeaderView.hashCode() + mDeleteBtn.hashCode();
		}
	} 

}
