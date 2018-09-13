package net.smallchat.im.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.smallchat.im.Entity.Room;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.global.IMCommon;

public class RoomTable {

	public static final String TABLE_NAME = "RoomTable";//数据表的名称
	/**
	 * fromId, sendTime, unreadCount, currentUser, primary key(fromId, currentUser
	 */
	public static final String COLUMN_ROOM_ID = "roomid";
	public static final String COLUMN_ROOM_NAME = "roomname";//群昵称
	public static final String COLUMN_CREATE_USER_ID = "uid";
	public static final String COLUMN_IS_OWNER = "isOwner";
	public static final String COLUMN_LOGIN_ID = "loginid";
	public static final String COLUMN_GROUP_NICK_NAME = "group_nick_name";//用户所在群的昵称
	public static final String COLUMN_IS_PUBLISH_GROUP = "is_publish_group";//是否公开群
	public static final String COLUMN_IS_GET_GROUP_MSG = "is_get_group_msg";//是否接受群消息
	public static final String COLUMN_IS_SHOW_NICKNAME = "is_show_nickname";//是否显示群昵称

	//是否开启隐私模式
	public static final String COLUMN_PRIVACY_MODE = "privacyMode";
	//是否开启加密消息
	public static final String COLUMN_ENCRYPT_MODE = "privacyMode";


	public static final String COLUMN_CREATETIME = "createtime";

	public static final String COLUMN_TEXT_TYPE = "text";
	public static final String COLUMN_INTEGER_TYPE = "integer";





	public static final String PRIMARY_KEY_TYPE = "primary key(";

	private SQLiteDatabase mDBStore;

	private static String mSQLCreateWeiboInfoTable = null;
	private static String mSQLDeleteWeiboInfoTable = null;

	public RoomTable(SQLiteDatabase sqlLiteDatabase) {
		mDBStore = sqlLiteDatabase;
	}

	public static String getCreateTableSQLString() {
		if (null == mSQLCreateWeiboInfoTable) {

			HashMap<String, String> columnNameAndType = new HashMap<String, String>();
			columnNameAndType.put(COLUMN_ROOM_ID, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_ROOM_NAME, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_CREATE_USER_ID, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_IS_OWNER, COLUMN_INTEGER_TYPE);
			columnNameAndType.put(COLUMN_LOGIN_ID, COLUMN_TEXT_TYPE);

			columnNameAndType.put(COLUMN_GROUP_NICK_NAME, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_IS_PUBLISH_GROUP, COLUMN_INTEGER_TYPE);//是否公开群
			columnNameAndType.put(COLUMN_IS_GET_GROUP_MSG, COLUMN_INTEGER_TYPE);//是否接受群消息
			columnNameAndType.put(COLUMN_IS_SHOW_NICKNAME, COLUMN_INTEGER_TYPE);

			columnNameAndType.put(COLUMN_PRIVACY_MODE, COLUMN_INTEGER_TYPE);
			columnNameAndType.put(COLUMN_ENCRYPT_MODE, COLUMN_INTEGER_TYPE);

			columnNameAndType.put(COLUMN_CREATETIME, COLUMN_TEXT_TYPE);
			String primary_key = PRIMARY_KEY_TYPE + COLUMN_ROOM_ID + "," + COLUMN_LOGIN_ID + ")";

			mSQLCreateWeiboInfoTable = SqlHelper.formCreateTableSqlString(TABLE_NAME, columnNameAndType, primary_key);
		}
		return mSQLCreateWeiboInfoTable;

	}

	public static String getDeleteTableSQLString() {
		if (null == mSQLDeleteWeiboInfoTable) {
			mSQLDeleteWeiboInfoTable = SqlHelper.formDeleteTableSqlString(TABLE_NAME);
		}  
		return mSQLDeleteWeiboInfoTable;
	}

	public void insert(List<Room> rooms) {
		List<Room> roomList = new ArrayList<Room>();
		roomList.addAll(rooms);
		for (Room room : roomList) {
			ContentValues allPromotionInfoValues = new ContentValues();

			allPromotionInfoValues.put(COLUMN_ROOM_ID, room.groupId);
			allPromotionInfoValues.put(COLUMN_ROOM_NAME, room.groupName);
			allPromotionInfoValues.put(COLUMN_CREATE_USER_ID, room.uid);
			allPromotionInfoValues.put(COLUMN_IS_OWNER, room.isOwner);
			allPromotionInfoValues.put(COLUMN_LOGIN_ID, IMCommon.getUserId(ChatApplication.getInstance()));
			allPromotionInfoValues.put(COLUMN_GROUP_NICK_NAME, room.groupnickname);
			allPromotionInfoValues.put(COLUMN_IS_GET_GROUP_MSG, room.isgetmsg);//是否接受群消息
			allPromotionInfoValues.put(COLUMN_CREATETIME, room.createTime);
			allPromotionInfoValues.put(COLUMN_IS_SHOW_NICKNAME, room.isShowNickname);

			allPromotionInfoValues.put(COLUMN_PRIVACY_MODE, room.privacyMode);
			allPromotionInfoValues.put(COLUMN_ENCRYPT_MODE, room.encryptMode);


			delete(room.groupId);
			try {
				mDBStore.insertOrThrow(TABLE_NAME, null, allPromotionInfoValues);
			} catch (SQLiteConstraintException e) {
				e.printStackTrace();
			}		
		}						
	}

	public void insert(Room room) {

		ContentValues allPromotionInfoValues = new ContentValues();

		allPromotionInfoValues.put(COLUMN_ROOM_ID, room.groupId);
		allPromotionInfoValues.put(COLUMN_ROOM_NAME, room.groupName);
		allPromotionInfoValues.put(COLUMN_CREATE_USER_ID, room.uid);
		allPromotionInfoValues.put(COLUMN_IS_OWNER, room.isOwner);
		allPromotionInfoValues.put(COLUMN_LOGIN_ID, IMCommon.getUserId(ChatApplication.getInstance()));
		allPromotionInfoValues.put(COLUMN_GROUP_NICK_NAME, room.groupnickname);
		allPromotionInfoValues.put(COLUMN_IS_GET_GROUP_MSG, room.isgetmsg);//是否接受群消息
		allPromotionInfoValues.put(COLUMN_CREATETIME, room.createTime);
		allPromotionInfoValues.put(COLUMN_IS_SHOW_NICKNAME, room.isShowNickname);


		allPromotionInfoValues.put(COLUMN_PRIVACY_MODE, room.privacyMode);
		allPromotionInfoValues.put(COLUMN_ENCRYPT_MODE, room.encryptMode);
		delete(room.groupId);
		try {
			mDBStore.insertOrThrow(TABLE_NAME, null, allPromotionInfoValues);
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}		
	}


	public boolean update(Room room) {
		ContentValues allPromotionInfoValues = new ContentValues();
		allPromotionInfoValues.put(COLUMN_ROOM_NAME, room.groupName);
		allPromotionInfoValues.put(COLUMN_GROUP_NICK_NAME, room.groupnickname);
		allPromotionInfoValues.put(COLUMN_IS_GET_GROUP_MSG, room.isgetmsg);//是否接受群消息
		allPromotionInfoValues.put(COLUMN_IS_SHOW_NICKNAME, room.isShowNickname);
		allPromotionInfoValues.put(COLUMN_PRIVACY_MODE, room.privacyMode);
		allPromotionInfoValues.put(COLUMN_ENCRYPT_MODE, room.encryptMode);
		try {
			mDBStore.update(TABLE_NAME, allPromotionInfoValues, COLUMN_ROOM_ID + " = '" + room.groupId 
					+ "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}			

		return false;
	}

	public boolean updatePublish(int isPublish,String roomId) {
		ContentValues allPromotionInfoValues = new ContentValues();
		allPromotionInfoValues.put(COLUMN_IS_PUBLISH_GROUP, isPublish);//是否公开群

		try {
			mDBStore.update(TABLE_NAME, allPromotionInfoValues, COLUMN_ROOM_ID + " = '" + roomId 
					+ "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}			

		return false;
	}
	public boolean updateIsGetMsg(int isGetMsg,String roomId) {
		ContentValues allPromotionInfoValues = new ContentValues();
		allPromotionInfoValues.put(COLUMN_IS_GET_GROUP_MSG, isGetMsg);//是否接受群消息

		try {
			mDBStore.update(TABLE_NAME, allPromotionInfoValues, COLUMN_ROOM_ID + " = '" + roomId 
					+ "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}			

		return false;
	}


	public boolean updateencryptMode(int privacyMode,String roomId) {
		ContentValues allPromotionInfoValues = new ContentValues();
		allPromotionInfoValues.put(COLUMN_ENCRYPT_MODE, privacyMode);

		try {
			mDBStore.update(TABLE_NAME, allPromotionInfoValues, COLUMN_ROOM_ID + " = '" + roomId
					+ "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

		return false;
	}


	public boolean updateIsPrivacyMode(int isPrivacyMode,String roomId) {
		ContentValues allPromotionInfoValues = new ContentValues();
		allPromotionInfoValues.put(COLUMN_PRIVACY_MODE, isPrivacyMode);

		try {
			mDBStore.update(TABLE_NAME, allPromotionInfoValues, COLUMN_ROOM_ID + " = '" + roomId
					+ "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean delete(String roomId) {
		try {
			mDBStore.delete(TABLE_NAME, COLUMN_ROOM_ID + " = '" + roomId + "' AND " + 
					COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean delete() {
		try {
			mDBStore.delete(TABLE_NAME, COLUMN_LOGIN_ID + "='"
					+ IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public Room query(String roomId){
		Cursor cursor = null;
		try {
			cursor = mDBStore.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " +
					COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) +
					"' AND " + COLUMN_ROOM_ID + "='" + roomId + "'" , null);
			if (cursor != null) {

				if (!cursor.moveToFirst()) {
					return null;
				}				

				int indexRoomId = cursor.getColumnIndex(COLUMN_ROOM_ID);
				int indexRoomName = cursor.getColumnIndex(COLUMN_ROOM_NAME);
				int indexUid = cursor.getColumnIndex(COLUMN_CREATE_USER_ID);
				int indexIsOwner = cursor.getColumnIndex(COLUMN_IS_OWNER);

				int indexGroupNickName = cursor.getColumnIndex(COLUMN_GROUP_NICK_NAME);
				int indexIsGetGroupMsg = cursor.getColumnIndex(COLUMN_IS_GET_GROUP_MSG);
				int indexIsShowNickName = cursor.getColumnIndex(COLUMN_IS_SHOW_NICKNAME);


				int indexIsPrivacyMode = cursor.getColumnIndex(COLUMN_PRIVACY_MODE);
				int indexEncryptMode = cursor.getColumnIndex(COLUMN_ENCRYPT_MODE);


				int indexCreateTime = cursor.getColumnIndex(COLUMN_CREATETIME);
				

				Room room = new Room();
				room.groupId = cursor.getString(indexRoomId);
				room.groupName = cursor.getString(indexRoomName);
				room.uid = cursor.getString(indexUid);
				room.isOwner = cursor.getInt(indexIsOwner);
				room.isgetmsg = cursor.getInt(indexIsGetGroupMsg);
				room.isShowNickname = cursor.getInt(indexIsShowNickName);
				room.groupnickname = cursor.getString(indexGroupNickName);

				room.encryptMode=cursor.getInt(indexEncryptMode);
				room.privacyMode =cursor.getInt(indexIsPrivacyMode);


				room.createTime = cursor.getLong(indexCreateTime);
				return room;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return null;
	}

	public List<Room> query() {
		List<Room> allInfo = new ArrayList<Room>();
		Cursor cursor = null;
		try {
			cursor = mDBStore.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " 
					+ COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			if (cursor != null) {

				if (!cursor.moveToFirst()) {
					return null;
				}

				int indexRoomId = cursor.getColumnIndex(COLUMN_ROOM_ID);
				int indexRoomName = cursor.getColumnIndex(COLUMN_ROOM_NAME);
				int indexUid = cursor.getColumnIndex(COLUMN_CREATE_USER_ID);
				int indexIsOwner = cursor.getColumnIndex(COLUMN_IS_OWNER);

				int indexGroupNickName = cursor.getColumnIndex(COLUMN_GROUP_NICK_NAME);
				int indexIsGetGroupMsg = cursor.getColumnIndex(COLUMN_IS_GET_GROUP_MSG);
				int indexIsShowNickName = cursor.getColumnIndex(COLUMN_IS_SHOW_NICKNAME);



				int indexIsPrivacyMode = cursor.getColumnIndex(COLUMN_PRIVACY_MODE);
				int indexEncryptMode = cursor.getColumnIndex(COLUMN_ENCRYPT_MODE);


				int indexCreateTime = cursor.getColumnIndex(COLUMN_CREATETIME);


				do {
					Room room = new Room();
					room.groupId = cursor.getString(indexRoomId);
					room.groupName = cursor.getString(indexRoomName);
					room.uid = cursor.getString(indexUid);
					room.isOwner = cursor.getInt(indexIsOwner);
					room.groupnickname = cursor.getString(indexGroupNickName);
					room.isgetmsg = cursor.getInt(indexIsGetGroupMsg);
					room.isShowNickname = cursor.getInt(indexIsShowNickName);

					room.encryptMode=cursor.getInt(indexEncryptMode);
					room.privacyMode =cursor.getInt(indexIsPrivacyMode);


					room.createTime = cursor.getLong(indexCreateTime);
					allInfo.add(room);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return allInfo;
	}

}
