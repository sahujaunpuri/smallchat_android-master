package net.smallchat.im.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;

import net.smallchat.im.Entity.Login;

/**
 * 获取系统的联系人
 * @author dongli
 *
 */
public class SystemContactGlobal {

	private ContactAsyncQueryHandler mContactAsync;
	private List<Login> mUserList = new ArrayList<Login>();
	private StringBuffer mPhoneBuffer = new StringBuffer();
	private Map<Integer, Login> contactIdMap ;
	private int mContactCount;
	private Handler mHandler;


	public SystemContactGlobal() {
		super();
	}

	public SystemContactGlobal(Context context,Handler handler) {
		mHandler = handler;
		mContactAsync=new ContactAsyncQueryHandler(context.getContentResolver());
		new Thread(){
			public void run() {
				mHandler.sendEmptyMessage(GlobalParam.MSG_GET_CONTACT_DATA);
				startContactQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
			};
		}.start();
		
	}



	private void startContactQuery(Uri uri) {
		String[] projection = { 
				ContactsContract.CommonDataKinds.Phone._ID,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.DATA1,"sort_key",
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
				ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
				ContactsContract.CommonDataKinds.Phone.CONTACT_STATUS_TIMESTAMP
		}; //查询的列
		mContactAsync.startQuery(0, null, uri, projection, null, null,
				"sort_key COLLATE LOCALIZED asc"); //按照sort_key升序查询
	}

	private class ContactAsyncQueryHandler extends AsyncQueryHandler {
		public ContactAsyncQueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected Handler createHandler(Looper looper) {
			return super.createHandler(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (mUserList!=null && mUserList.size()>0) {
				mUserList.clear();
			}
			Pattern pattern = Pattern.compile("[0-9]*"); 
			if (cursor != null && cursor.getCount() > 0) {
				contactIdMap = new HashMap<Integer, Login>();
				int i=0; cursor.getCount();
				int count = cursor.getCount();
				mContactCount = count;
				while (cursor.moveToNext()) {
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);
					int contactId = cursor.getInt(4);// 取得联系人ID
					Long photoId = cursor.getLong(5);
					String lookUpKey = cursor.getString(6);
					String checkTime = cursor.getString(7);
					count --;
					if (contactIdMap.containsKey(contactId)) {
						/*Contact contact=contactIdMap.get(contactId);
						contact.phoneList.add(new Phone(number, "sccd","3"));
						contactIdMap.put(contactId, contact);*/
					}else{
						//FocusItem(contactId,number,name,0);
						String index="";
						/*String uid, String sort, String phone, String headSmall,
						String name*/
						String phoneNumber="";
						if(number.contains(" ")){
							phoneNumber=number.replace(" ","");
						}else{
							phoneNumber=number;
						}
						Login contact= new Login(String.valueOf(contactId),index,phoneNumber,null, name);
						/*new Login(String.valueOf(contactId),number,
								String.valueOf(photoId),name,index);*/



						/*if (selectMap!=null && !selectMap.isEmpty() && selectMap.size()>0) {
							if (selectMap.containsKey(contactId)) {
									contact.selceted=1;
							}
						}*/
						mPhoneBuffer.append(phoneNumber);
						if (count!=0) {
							mPhoneBuffer.append(",");
						}
						mUserList.add(contact);
						contactIdMap.put(contactId, contact);
						i++;
					}
				}
			}
			mHandler.sendEmptyMessage(GlobalParam.MSG_SHOW_LISTVIEW_DATA);
		}
	}


	public List<Login> getmUserList() {
		return mUserList;
	}



	public String getPhoneString() {
		if(mPhoneBuffer.length()>0){
			return mPhoneBuffer.toString();
		}
		return null;
	}
	
	public int getContactCount() {
		return mContactCount;
	}


}
