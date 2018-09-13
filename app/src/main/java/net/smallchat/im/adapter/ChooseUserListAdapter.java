package net.smallchat.im.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import net.smallchat.im.Entity.Login;
import net.smallchat.im.R;
import net.smallchat.im.global.ImageLoader;

/**
 * 选择用户适配器
 * @author dongli
 *
 */
public class ChooseUserListAdapter extends BaseAdapter{

	private final LayoutInflater mInflater;
	HashMap<Integer, View> hashMap;
	private List<Login> mData;
	private ImageLoader mImageLoader;
	private Context mContext;
	private boolean mIsShow = true;

	public ChooseUserListAdapter(Context context, List<Login> data){
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


	public void setIsShow(boolean isShow){
		mIsShow = isShow;
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
			holder.mContentSplite = (ImageView)convertView.findViewById(R.id.content_splite);
			holder.mGroupLayout = (RelativeLayout) convertView.findViewById(R.id.grouplayout);
			holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkperson);
			convertView.setTag(holder);  
			hashMap.put(position, convertView);
		}else {
			holder=(ViewHolder) convertView.getTag();  
		}


		Login login = mData.get(position);
		//根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);

		//如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if(position == getPositionForSection(section)){
			holder.mGroupLayout.setVisibility(View.VISIBLE);
			holder.mGroupNameView.setVisibility(View.VISIBLE);
			holder.mContentSplite.setVisibility(View.VISIBLE);

			if(login.sortName == null || login.sortName.equals("")){
				holder.mGroupLayout.setVisibility(View.GONE);
				holder.mGroupNameView.setVisibility(View.GONE);
			}else{
				holder.mGroupNameView.setText(login.sortName);
			}
		}else{
			holder.mGroupLayout.setVisibility(View.GONE);
			holder.mGroupNameView.setVisibility(View.GONE);
			holder.mContentSplite.setVisibility(View.VISIBLE);
		}

		if(login.headSmall != null && !login.headSmall.equals("")){
			mImageLoader.getBitmap(mContext, holder.mHeaderView, null, login.headSmall, 0, false, true);
		}

		if(mIsShow){
			holder.mCheckBox.setVisibility(View.VISIBLE);
			holder.mCheckBox.setChecked(login.isShow);
		}else {
			holder.mCheckBox.setVisibility(View.GONE);
		}
		String name = login.remark;
		if(name == null || name.equals("")){
			name = login.nickname;
		}
		holder.mUserNameTextView.setText(name);
		if(login.sign != null){
			holder.mContentTextView.setText(login.sign);
		}

		return convertView;
	}


	final static class ViewHolder {  
		TextView mUserNameTextView;  
		TextView mContentTextView;
		TextView mTimeTextView;
		ImageView mHeaderView,mContentSplite;
		TextView mGroupNameView;
		private CheckBox mCheckBox;
		RelativeLayout mGroupLayout;

		@Override
		public int hashCode() {
			return this.mUserNameTextView.hashCode() + mContentTextView.hashCode() + mTimeTextView.hashCode() + 
					mGroupNameView.hashCode() + mHeaderView.hashCode() + mGroupLayout.hashCode() + mCheckBox.hashCode();
		}
	} 


	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		Log.e("SortAdapter", "positon:"+position);
		if(mData.get(position)!=null && mData.get(position).sort!=null
				&& !mData.get(position).sort.equals("")){
			Log.e("SortAdapter_two", "positon:"+position);
			return mData.get(position).sort.charAt(0);
		}
		return 0;
	}



	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = mData.get(i).sort;
			if(sortStr!=null && !sortStr.equals("")){
				if(sortStr.toUpperCase()!=null && !sortStr.toUpperCase().equals("")){
					char firstChar = sortStr.toUpperCase().charAt(0);
					if (firstChar == section) {
						return i;
					}
				}

			}


		}

		return -1;
	}

}
