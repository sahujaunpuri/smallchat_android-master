package net.smallchat.im.adapter;

import java.util.List;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smallchat.im.R;
import net.smallchat.im.Entity.Login;
import net.smallchat.im.global.GlobalParam;
import net.smallchat.im.global.ImageLoader;
import net.smallchat.im.global.IMCommon;

/**
 * 搜索用户结果数据集
 * @author dongli
 *
 */
public class SearchResultAdapter extends BaseAdapter{
	private Context ctx;
	private ViewHolder holder;
	private List<Login> list;
	private boolean isShowValidBtn,mIsShowSign;
	private Handler mHandler;
	private ImageLoader mImageLoader;


	public SearchResultAdapter(Context context, List<Login> list,boolean isShowValidBtn,Handler handler,boolean isShowSign) {
		this.ctx = context;
		this.list = list;	
		this.isShowValidBtn = isShowValidBtn;
		this.mHandler = handler;
		mImageLoader = new ImageLoader();
		this.mIsShowSign = isShowSign;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setData(List<Login> list){
		this.list = list;
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		try {
			if (convertView == null || ((ViewHolder) convertView.getTag()).mTag != position) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(ctx).inflate(R.layout.note_item, null);
				holder.mHeadrIcon = (ImageView)convertView.findViewById(R.id.header_icon);
				holder.mIconParentLayout = (RelativeLayout)convertView.findViewById(R.id.icon_parent_layout);
				holder.mNameTextView = (TextView)convertView.findViewById(R.id.tv3);
				holder.mSignTextView = (TextView)convertView.findViewById(R.id.sign);
				holder.mDeepArrowIcon = (ImageView)convertView.findViewById(R.id.deep_arrow_icon);
				if(mIsShowSign){
					holder.mSignTextView.setVisibility(View.VISIBLE);
				}
				holder.mValidFriends = (Button)convertView.findViewById(R.id.valid_friends);
				if (isShowValidBtn  ) {
					holder.mValidFriends.setVisibility(View.VISIBLE);
				}else{
					holder.mValidFriends.setVisibility(View.GONE);
				}
				holder.mTag = position;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 绑定数据
			final Login item = list.get(position);
			if (item.nickname!=null && !item.nickname.equals("")) {
				holder.mNameTextView.setText(item.nickname);
			}else{
				holder.mNameTextView.setText(item.uid);
			}

			holder.mSignTextView.setText(item.sign);

			/**
			 *
			 *type 0-表示没有帐号 1-表示有帐号
			 Phone 电话
			 Isfriend 附近接口里面有说明
			 userID 用户的userid

			 * Isfriend 0  没有关系
			 * 1 userID 关注 toUserID
			 * 2 toUserID 关注 usrID
			 * 3  相互关注
			 * 4 等待验证 
			 */
			if (isShowValidBtn) {
				switch (item.isfriend) {
				case 0:
					if(item.isAccount == 1){
						holder.mValidFriends.setText("添加");
						holder.mValidFriends.setBackground(
								ctx.getResources().getDrawable(R.drawable.small_red_btn));
						holder.mValidFriends.setTextColor(Color.parseColor("#ffffff"));
					}else if(item.isAccount == 0){
						holder.mValidFriends.setText("邀请");
						holder.mValidFriends.setBackground(
								ctx.getResources().getDrawable(R.drawable.invite_friends_btn));
					}
					
					break;
				case 1:
				case 3:
					holder.mValidFriends.setText("已添加");
					holder.mValidFriends.setBackground(null);
					break;
				case 4://等待验证
					holder.mValidFriends.setText("等待验证");
					holder.mValidFriends.setBackground(null);
					break;
				case 2:
					holder.mValidFriends.setText("添加");
					holder.mValidFriends.setBackground(
							ctx.getResources().getDrawable(R.drawable.small_red_btn));
					holder.mValidFriends.setTextColor(Color.parseColor("#ffffff"));

					break;
				default:
					break;
				}
				if(item.isfriend != 3){
					holder.mValidFriends.setVisibility(View.VISIBLE);

				}
			}else{
				holder.mValidFriends.setText(">");
			}
			if(isShowValidBtn){
				holder.mIconParentLayout.setVisibility(View.GONE);
				holder.mDeepArrowIcon.setVisibility(View.VISIBLE);
			}else{
				if(item.headSmall!=null && !item.headSmall.equals("")){
					mImageLoader.getBitmap(ctx, holder.mHeadrIcon,null,item.headSmall,0,false,true);
				}
			}
			
			holder.mValidFriends.setOnClickListener(
					new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							IMCommon.sendMsg(mHandler,
									GlobalParam.MSG_CLICK_LISTENER, position);
						}
					});

		} catch (OutOfMemoryError e) {
			Runtime.getRuntime().gc();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return convertView;
	}
	class ViewHolder {
		public int mTag;
		public ImageView mHeadrIcon,mDeepArrowIcon;
		public TextView mNameTextView;
		public TextView mSignTextView;
		public Button mValidFriends;
		public RelativeLayout mIconParentLayout;

		@Override
		public int hashCode() {
			return mHeadrIcon.hashCode() + mNameTextView.hashCode()
					+ mValidFriends.hashCode()+mSignTextView.hashCode()
					+ mIconParentLayout.hashCode()+ mDeepArrowIcon.hashCode();
		}

	}
}
