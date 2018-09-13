package net.smallchat.im.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.Entity.Group;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.global.ImageLoader;


/**
 * 数据结构adapter
 * @author dongli
 *
 */
public class TreeViewAdapter extends BaseExpandableListAdapter{
	public static final int ItemHeight=58;//每项的高度
	public static final int PaddingLeft=40;//每项的高度
	private int myPaddingLeft=10;//如果是由SuperTreeView调用，则作为子项需要往右移
	
	Context parentContext;
	private ImageLoader mImageLoader = new ImageLoader();
	private List<Group> mGroupList;
	private Handler mHandler;
	
	public TreeViewAdapter(){}
	
	public TreeViewAdapter(Context view, List<Group> groupList){
		parentContext=view;
		mGroupList = groupList;
		
		mHandler = new Handler(){  
			  
            @Override  
            public void handleMessage(Message msg) {  
              TreeViewAdapter.this.notifyDataSetChanged();  
              super.handleMessage(msg);  
            }  
        };  
	}
	
	public void setData(List<Group> data){
		mGroupList = data;
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		LayoutInflater layoutInflater = (LayoutInflater) parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View linearLayout = layoutInflater.inflate(R.layout.contact_item, null);
		if(childPosition < getChildrenCount(groupPosition)){
			TextView userName=(TextView)linearLayout.findViewById(R.id.username);
			TextView sign = (TextView)linearLayout.findViewById(R.id.prompt);
			ImageView mHeaderImageView = (ImageView) linearLayout.findViewById(R.id.headerimage);
			Login user = (Login)getChild(groupPosition, childPosition);
			if (user != null) {
				String name = user.name;
				userName.setText(name);
				
				if(user.sign != null && !user.sign.equals("")){
					sign.setText(user.sign);
				}
				
				if(user.headSmall != null && !user.headSmall.equals("")){
		        	mImageLoader.getBitmap(parentContext, mHeaderImageView, null, user.headSmall, 0, false, true);
		        }
			}
			
			return linearLayout;
		}
		return linearLayout;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		LayoutInflater layoutInflater = (LayoutInflater) parentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.group, null);
		if (getGroupName(groupPosition) != null && !getGroupName(groupPosition).equals("")) {
			TextView textView = (TextView)linearLayout.findViewById(R.id.text_gruop);
			textView.setText(getGroupName(groupPosition));
		}
		
		return linearLayout;
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public Object getGroup(int groupPosition) {
		return mGroupList.get(groupPosition);
	}

	public String getGroupName(int groupPosition){
		return mGroupList.get(groupPosition).teamName;
	}
	
	public int getGroupCount() {
		return mGroupList.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if(mGroupList.get(groupPosition).mUserList != null 
				&& mGroupList.get(groupPosition).mUserList.size()>0){
			return mGroupList.get(groupPosition).mUserList.get(childPosition);
		}
		
		return null;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if(mGroupList.get(groupPosition).mUserList != null 
				&& mGroupList.get(groupPosition).mUserList.size()>0){
			return mGroupList.get(groupPosition).mUserList.size();
		}
		
		return 0;
	}
	
	public void refresh() {
		mHandler.sendMessage(new Message());
	} 
}