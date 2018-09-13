package net.smallchat.im.adapter;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.Entity.NotifiyType;
import net.smallchat.im.Entity.NotifiyMessage;
import net.smallchat.im.global.FeatureFunction;
import net.smallchat.im.global.ImageLoader;

/**
 * 我的相册—消息适配器
 * @author dongli
 *
 */
public class MessageMovingAdapter extends BaseAdapter {

	private final LayoutInflater mInflater;
	private Context mContext;
	private List<NotifiyMessage> mData;
	private ImageLoader mImageLoader;

	public MessageMovingAdapter(Context context,List<NotifiyMessage> list) {
		mContext = context;
		mData = list;
		mInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;  
		if (convertView==null) {  
			convertView=mInflater.inflate(R.layout.moving_message_list_item, null);   
			holder=new ViewHolder();  
			holder.mHeaderView = (ImageView) convertView.findViewById(R.id.header);
			holder.mUserNameTextView = (TextView) convertView.findViewById(R.id.username);
			holder.mContentTextView = (TextView) convertView.findViewById(R.id.comment_content);
			holder.mTimeTextView = (TextView) convertView.findViewById(R.id.comment_time);
			holder.mOldImg = (ImageView) convertView.findViewById(R.id.old_img);
			holder.mOldMessage = (TextView) convertView.findViewById(R.id.old_content);
			holder.mZanIcon = (ImageView)convertView.findViewById(R.id.zan_icon);
			convertView.setTag(holder);  
		}else {
			holder=(ViewHolder) convertView.getTag();  
		}

		NotifiyMessage item = mData.get(position);
		if(item.getUser()!=null){
			if(item.getUser().headSmall!=null && !item.getUser().headSmall.equals("")){
				mImageLoader.getBitmap(mContext, holder.mHeaderView,null, item.getUser().headSmall, 0, false, true);
			}

		}

		holder.mUserNameTextView.setText(item.userName);
		if(item.sharePicture!=null && !item.sharePicture.equals("")){
			holder.mOldMessage.setVisibility(View.GONE);
			holder.mOldImg.setVisibility(View.VISIBLE);
			mImageLoader.getBitmap(mContext, holder.mOldImg,null, item.sharePicture, 0, false, false);
		}else{
			holder.mOldMessage.setVisibility(View.VISIBLE);
			holder.mOldImg.setVisibility(View.GONE);
			if(item.shareContent!=null && !item.shareContent.equals("")){
				holder.mOldMessage.setText(EmojiUtil.getExpressionString(mContext, item.shareContent,ChatMessageAdapter.EMOJIREX));
			}else{
				holder.mOldMessage.setText("");
			}

		}

		holder.mTimeTextView.setText(FeatureFunction.calculaterReleasedTime(mContext, new Date(item.getTime()), item.getTime(),0));
		if(item.getType() == NotifiyType.ADD_LIKE){
			holder.mZanIcon.setVisibility(View.VISIBLE);
			holder.mContentTextView.setVisibility(View.GONE);
		}else if(item.getType() == NotifiyType.REPLY){
			holder.mZanIcon.setVisibility(View.GONE);
			holder.mContentTextView.setVisibility(View.VISIBLE);
			if(item.getContent()!=null && !item.getContent().equals("")){
				holder.mContentTextView.setText(EmojiUtil.getExpressionString(mContext, item.getContent(),ChatMessageAdapter.EMOJIREX));
			}else{
				holder.mContentTextView.setText("");
			}

		}
	

		return convertView;
	}

	final static class ViewHolder {  
		ImageView mHeaderView;
		TextView mUserNameTextView;  
		TextView mContentTextView;
		TextView mTimeTextView;
		TextView mOldMessage;
		ImageView mOldImg,mZanIcon;



		@Override
		public int hashCode() {
			return this.mUserNameTextView.hashCode() + mContentTextView.hashCode() + mTimeTextView.hashCode() + 
					+ mHeaderView.hashCode()+mZanIcon.hashCode();
		}
	} 

}
