package net.smallchat.im.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import net.smallchat.im.Entity.Group;
import net.smallchat.im.R;
import net.smallchat.im.contact.UserInfoActivity;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.widget.MyListView;

/**
 * 用户列表adapter
 * @author dongli
 *
 */
public class UserListAdapter extends BaseAdapter{

	private final LayoutInflater mInflater;
	HashMap<Integer, View> hashMap;
	private List<Group> mData;
	private ImageLoader mImageLoader;
	private Context mContext;
	
	public UserListAdapter(Context context, List<Group> groupList){
		mInflater = (LayoutInflater)context.getSystemService(
	            Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mData = groupList;
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
	


	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		convertView = hashMap.get(position);
        ViewHolder holder;  
        
        final int index = position;
        if (convertView==null) {  
        	convertView=mInflater.inflate(R.layout.contact_user_item, null);   
            holder=new ViewHolder();  
              
            holder.mGroupNameView = (TextView) convertView.findViewById(R.id.sortKey);
            holder.mListView = (MyListView)convertView.findViewById(R.id.user_list);
            holder.mListView.setDivider(mContext.getResources().getDrawable(R.drawable.order_devider_line));
            holder.mListView.setCacheColorHint(0);
            holder.mListView.setSelector(mContext.getResources().getDrawable(R.drawable.transparent_selector));
            if(mData.get(position).mUserList != null 
            		&& mData.get(position).mUserList.size()>0){
            	holder.mAdapter =  new SearchUserListAdapter(mContext, mData.get(position).mUserList,0);
            	holder.mListView.setAdapter(holder.mAdapter);
            }
        	holder.mListView.setOnItemClickListener(new OnItemClickListener() {

    			@Override
    			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
    					long arg3) {
    				Intent intent = new Intent(mContext, UserInfoActivity.class);
    				intent.putExtra("user", mData.get(index).mUserList.get(arg2));
    				mContext.startActivity(intent);
    			}
            	
    		});
        	holder.mListView.setTag(mData.get(position).id);
            convertView.setTag(holder);  
            hashMap.put(position, convertView);
        }else {
        	holder=(ViewHolder) convertView.getTag();  
		}
        
        MyListView listview = (MyListView) convertView.findViewWithTag(mData.get(position).id);
        if(mData.get(position).mUserList != null && mData.get(position).mUserList.size()>0){
        	listview.setVisibility(View.VISIBLE);
        	if(listview.getAdapter() != null){
        		holder.mAdapter.notifyDataSetChanged();
        	}else {
        		holder.mAdapter =  new SearchUserListAdapter(mContext, mData.get(position).mUserList,0);
        		listview.setAdapter(holder.mAdapter);
			}
        	
        }else {
        	listview.setVisibility(View.GONE);
		}
        
        holder.mGroupNameView.setText(mData.get(position).teamName);
        
        return convertView;
	}

	
	final static class ViewHolder {  
        TextView mGroupNameView;
        MyListView mListView;
        SearchUserListAdapter mAdapter;
        
        @Override
        public int hashCode() {
			return mGroupNameView.hashCode() + mListView.hashCode() + mAdapter.hashCode();
        }
    } 
	
}
