package net.smallchat.im.components.treeview;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.global.ImageLoader;

/**
 * ����TreeViewAdapter.java
 * �����������������ݵ���
 * @author wader
 * ����ʱ�䣺2011-11-03 16:32
 */
public class TreeViewAdapter extends BaseAdapter {

	class ViewHolder {
		RelativeLayout mParentLayout,mHeaderLayout;
		ImageView icon,userIcon;
		TextView title,sign;
	}

	
	Context context;
	ViewHolder holder;
	LayoutInflater inflater;

	List<TreeElement> elements;
	ImageLoader mImageLoader;

	public TreeViewAdapter(Context context, List<TreeElement> elements) {
		this.context = context;
		this.elements = elements;
		mImageLoader = new ImageLoader();
	}

	@Override
	public int getCount() {
		return elements.size();
	}

	@Override
	public Object getItem(int position) {
		return elements.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		/**
		 * ---------------------- get holder------------------------
		 */
		 if (convertView == null) {
			 if (inflater == null) {
				 inflater = LayoutInflater.from(context);
			 }
			 holder = new ViewHolder();
			 convertView = inflater	 .inflate(R.layout.tree_view_item_layout, null);
			 holder.icon = (ImageView) convertView	.findViewById(R.id.tree_view_item_icon);
			 holder.title = (TextView) convertView .findViewById(R.id.tree_view_item_title);
			 holder.mParentLayout = (RelativeLayout)convertView.findViewById(R.id.parent_layout);
			 holder.mHeaderLayout = (RelativeLayout)convertView.findViewById(R.id.headerlayout);
			 holder.userIcon = (ImageView)convertView.findViewById(R.id.header);
			 holder.sign = (TextView)convertView.findViewById(R.id.sign);
			 convertView.setTag(holder);
		 } else {
			 holder = (ViewHolder) convertView.getTag();
		 }
		 /**
		  * ---------------------- set holder--------------------------
		  */
		 if (elements.get(position).isHasChild()) {// 
			 if (elements.get(position).isFold()) {
				 holder.icon.setImageResource(R.drawable.tree_view_icon_open);
			 } else if (!elements.get(position).isFold()) {
				 holder.icon.setImageResource(R.drawable.tree_view_icon_close);
			 }
			 holder.icon.setVisibility(View.INVISIBLE);
			 if(elements.get(position).getLevel() == 1){
				 holder.mHeaderLayout.setVisibility(View.GONE);
				 holder.title.setTextColor(Color.parseColor("#212121"));
				 holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimension(R.dimen.title_text_size));
				 holder.sign.setVisibility(View.GONE);
			 }else if(elements.get(position).getLevel() > 1){
				 holder.mHeaderLayout.setVisibility(View.GONE);
				 holder.title.setTextColor(Color.parseColor("#2d2d2d"));
				 holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimension(R.dimen.nineteen));
				 holder.sign.setVisibility(View.GONE);
				 holder.icon.setVisibility(View.INVISIBLE);
			 }
		 } else {
			 if(elements.get(position).isBigItem){
				 holder.icon.setImageResource(R.drawable.tree_view_icon_close);
				 holder.icon.setVisibility(View.INVISIBLE);
				 holder.title.setTextColor(Color.parseColor("#2d2d2d"));
				 holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimension(R.dimen.eight));
				 holder.mParentLayout.setBackgroundColor(Color.parseColor("#FEFFFA"));
				 holder.mHeaderLayout.setVisibility(View.VISIBLE);
				 if(elements.get(position).getHeadUrl()!=null && !elements.get(position).getHeadUrl().equals("") ){
					 mImageLoader.getBitmap(context, holder.userIcon,null, elements.get(position).getHeadUrl(), 0, false,true);
				 }
				 holder.sign.setText(elements.get(position).getSign());
				 holder.sign.setVisibility(View.VISIBLE);
			 }else{
				 holder.mHeaderLayout.setVisibility(View.GONE);
				 holder.title.setTextColor(Color.parseColor("#2d2d2d"));
				 holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimension(R.dimen.nineteen));
				 holder.sign.setVisibility(View.GONE);
				 holder.icon.setVisibility(View.INVISIBLE);
			 }
			
		 }
		
		 holder.icon.setPadding(10 * (elements.get(position).getLevel()), 0, 0, 0);
		 holder.mParentLayout.setPadding(25 * (elements.get(position).getLevel()), 5, 5, 5);
		 holder.title.setText(elements.get(position).getTitle());
		

		 return convertView;
	}
}
