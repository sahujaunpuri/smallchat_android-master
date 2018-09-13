package net.smallchat.im.components.sortlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import net.smallchat.im.Entity.Login;
import net.smallchat.im.R;
import net.smallchat.im.global.ImageLoader;

import java.util.HashMap;
import java.util.List;

public class SortAdapter extends BaseAdapter implements SectionIndexer{
	private List<Login> list = null;
	private Context mContext;
	private ImageLoader mImageLoader;

	public SortAdapter(Context mContext, List<Login> list) {
		this.mContext = mContext;
		this.list = list;
		this.mImageLoader = new ImageLoader();
	}

	/**
	 * 获取缓存的图片
	 * @return
	 */
	public HashMap<String, Bitmap> getImageBuffer(){
		return mImageLoader.getImageBuffer();
	}

	/**
	 * 当ListView数据发生变化时,调用此方法来更新ListView
	 * @param list
	 */
	public void updateListView(List<Login> list){
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {
		return this.list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup arg2) {
		ViewHolder holder = null;
		final Login mContent = list.get(position);
		if (convertView == null || ((ViewHolder) convertView.getTag()).mTag != position) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_item, null);
			holder.mHeadrIcon = (ImageView)convertView.findViewById(R.id.headerimage);
			holder.mNameTextView = (TextView)convertView.findViewById(R.id.username);

			holder.index = (TextView) convertView.findViewById(R.id.sortKey);
			holder.indexLayout = (RelativeLayout)convertView.findViewById(R.id.grouplayout);
			holder.mContentSplite = (ImageView)convertView.findViewById(R.id.content_splite);
			holder.mSignTextView = (TextView)convertView.findViewById(R.id.prompt);
			holder.newFriendsIcon= (TextView)convertView.findViewById(R.id.new_notify);
			//holder.contactLayout = (LinearLayout)convertView.findViewById(R.tid.contact_layout);//select_contact_splite

			holder.mTag = position;
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//根据position获取分类的首字母的Char ascii值
		//int section = getSectionForPosition(position);

		String sort = "";
		if(position >0){
			int preIndex = position - 1;
			sort = this.list.get(preIndex).sort;//FeatureFunction.formartTime(mData.get(preIndex).createtime, "yyyy-MM-dd");
		}
		if(list.get(position).sort.equals(sort)){
			holder.indexLayout.setVisibility(View.GONE);
			holder.index.setVisibility(View.GONE);
			holder.mContentSplite.setVisibility(View.VISIBLE);
		}else{
			holder.indexLayout.setVisibility(View.VISIBLE);
			holder.index.setVisibility(View.VISIBLE);
			holder.mContentSplite.setVisibility(View.VISIBLE);

			if(mContent.sortName == null || mContent.sortName.equals("")){
				holder.indexLayout.setVisibility(View.GONE);
				holder.index.setVisibility(View.GONE);
			}else{
				holder.index.setText(mContent.sortName);
			}
		}
		
	/*	//如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if(position == getPositionForSection(section)){
			holder.indexLayout.setVisibility(View.VISIBLE);
			holder.index.setVisibility(View.VISIBLE);
			holder.mContentSplite.setVisibility(View.VISIBLE);

			if(mContent.sortName == null || mContent.sortName.equals("")){
				holder.indexLayout.setVisibility(View.GONE);
				holder.index.setVisibility(View.GONE);
			}else{
				holder.index.setText(mContent.sortName);
			}
		}else{
			holder.indexLayout.setVisibility(View.GONE);
			holder.index.setVisibility(View.GONE);
			holder.mContentSplite.setVisibility(View.VISIBLE);
		}
*/
		//if (item.name!=null && !item.name.equals("")) {
		String name = this.list.get(position).remark;
		if(name == null || name.equals("")){
			name = this.list.get(position).nickname;
		}
		holder.mNameTextView.setText(name);
		if(this.list.get(position).sign!=null && !this.list.get(position).sign.equals("")){
			holder.mSignTextView.setVisibility(View.VISIBLE);
			holder.mSignTextView.setText(this.list.get(position).sign);
		}
		if(name.equals(mContext.getResources().getString(R.string.new_friends))){
			if(mContent.newFriends == 1){
				holder.newFriendsIcon.setVisibility(View.VISIBLE);
			}else{
				holder.newFriendsIcon.setVisibility(View.GONE);
			}
			//holder.mNameTextView.getPaint().setFakeBoldText(true);
			holder.mHeadrIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.new_friends_icon));
			//holder.mContentSplite.setVisibility(View.VISIBLE);
		}else if(name.equals(mContext.getResources().getString(R.string.group_chat))){
			//holder.mNameTextView.getPaint().setFakeBoldText(true);
			//holder.mContentSplite.setVisibility(View.VISIBLE);
			holder.mHeadrIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.service_icon));
		}else{
			if(this.list.get(position).headSmall!=null && !this.list.get(position).headSmall.equals("")){
				holder.mHeadrIcon.setTag(this.list.get(position).headSmall);
				mImageLoader.getBitmap(mContext, holder.mHeadrIcon,null,this.list.get(position).headSmall,0,false,true);
			}else{
				holder.mHeadrIcon.setImageResource(R.drawable.contact_default_header);
			}
		}
		//}



		return convertView;

	}



	final static class ViewHolder {
		public int mTag;
		public ImageView mHeadrIcon,mContentSplite;
		public TextView mNameTextView,mSignTextView;

		public TextView index;
		public RelativeLayout indexLayout,contactLayout;
		public TextView newFriendsIcon;
	}


	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	public int getSectionForPosition(int position) {
		//Log.e("SortAdapter", "positon:"+position);
		if(list.get(position)!=null && list.get(position).sort!=null
				&& !list.get(position).sort.equals("")){
			//Log.e("SortAdapter_two", "positon:"+position);
			return list.get(position).sort.charAt(0);
		}
		return 0;
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).sort;
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

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 *
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String  sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object[] getSections() {
		return null;
	}
}