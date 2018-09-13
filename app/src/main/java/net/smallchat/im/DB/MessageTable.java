package net.smallchat.im.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.smallchat.im.Entity.ChatMessage;
import net.smallchat.im.Entity.ChatType;
import net.smallchat.im.Entity.MessageImage;
import net.smallchat.im.global.ChatApplication;
import net.smallchat.im.Entity.MainSearchEntity;
import net.smallchat.im.Entity.MessageType;
import net.smallchat.im.Entity.Session;
import net.smallchat.im.global.IMCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * 消息历史记录表
 */
public class MessageTable {

	public static final String TABLE_NAME = "MessageTable";//数据表的名称

	/**
	 * content, fromId, toId, sessionId, pullTime, sendTime, voiceTime, type,
	 * readState, sendState, sendType, currentUser, primary key(fromId, toId, sendTime, currentUser)
	 */
	public static final String COLUMN_FROM_UID = "fromId";
	public static final String COLUMN_FROM_NAME = "fromName";
	public static final String COLUMN_FROM_HEAD = "fromHead";
	public static final String COLUMN_TO_ID = "toId";
	public static final String COLUMN_TO_NAME = "toName";
	public static final String COLUMN_TO_HEAD = "toHead";
	public static final String COLUMN_LOGIN_ID = "loginId";
	public static final String COLUMN_ID = "messageID";
	public static final String COLUMN_TAG = "messageTag";
	public static final String COLUMN_CONTENT = "content";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_MESSAGE_TYPE = "msgtype";
	public static final String COLUMN_SEND_TIME = "sendTime";

	public static final String COLUMN_READ_STATE = "readState";
	public static final String COLUMN_SEND_STATE = "sendState";

	public static final String COLUMN_SAMPLE_RATE = "sampleRate";
	public static final String COLUMN_SYSTEM_MESSAGE ="system_message";



	//是否开启隐私模式
	public static final String COLUMN_PRIVACY_MODE = "privacyMode";
	//是否开启加密消息
	public static final String COLUMN_ENCRYPT_MODE = "privacyMode";


	public static final String COLUMN_INTEGER_TYPE = "integer";
	public static final String COLUMN_TEXT_TYPE = "text";
	public static final String PRIMARY_KEY_TYPE = "primary key(";

	private SQLiteDatabase mDBStore;

	private static String mSQLCreateWeiboInfoTable = null;
	private static String mSQLDeleteWeiboInfoTable = null;

	public MessageTable(SQLiteDatabase sqlLiteDatabase) {
		mDBStore = sqlLiteDatabase;
	}

	public static String getCreateTableSQLString() {
		if (null == mSQLCreateWeiboInfoTable) {

			HashMap<String, String> columnNameAndType = new HashMap<String, String>();
			columnNameAndType.put(COLUMN_FROM_UID, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_FROM_NAME, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_FROM_HEAD, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_TO_ID, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_TO_NAME, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_TO_HEAD, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_LOGIN_ID, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_ID, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_TAG, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_CONTENT, COLUMN_TEXT_TYPE);
			columnNameAndType.put(COLUMN_TYPE, COLUMN_INTEGER_TYPE);
			columnNameAndType.put(COLUMN_MESSAGE_TYPE, COLUMN_INTEGER_TYPE);
			columnNameAndType.put(COLUMN_SEND_TIME, COLUMN_INTEGER_TYPE);
			columnNameAndType.put(COLUMN_READ_STATE, COLUMN_INTEGER_TYPE);
			columnNameAndType.put(COLUMN_SEND_STATE, COLUMN_INTEGER_TYPE);

			columnNameAndType.put(COLUMN_SAMPLE_RATE, COLUMN_INTEGER_TYPE);
			columnNameAndType.put(COLUMN_SYSTEM_MESSAGE, COLUMN_INTEGER_TYPE);



			columnNameAndType.put(COLUMN_PRIVACY_MODE, COLUMN_INTEGER_TYPE);
			columnNameAndType.put(COLUMN_ENCRYPT_MODE, COLUMN_INTEGER_TYPE);


			String primary_key = PRIMARY_KEY_TYPE + COLUMN_FROM_UID + "," + COLUMN_TO_ID + "," + COLUMN_LOGIN_ID + "," + COLUMN_TAG + ")";

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

	public void insert(List<ChatMessage> messages) {
	    Log.d("write_db","数据写入 size="+messages.size());
		List<ChatMessage> messageList = new ArrayList<ChatMessage>();
		messageList.addAll(messages);
		for (ChatMessage message : messageList) {
		    //关键
            message.convertObjectToContent();
			ContentValues allPromotionInfoValues = new ContentValues();

			allPromotionInfoValues.put(COLUMN_FROM_UID, message.fromId);
			allPromotionInfoValues.put(COLUMN_FROM_NAME, message.fromName);
			allPromotionInfoValues.put(COLUMN_FROM_HEAD, message.fromUrl);
			allPromotionInfoValues.put(COLUMN_LOGIN_ID, IMCommon.getUserId(ChatApplication.getInstance()));
			allPromotionInfoValues.put(COLUMN_TO_ID, message.toId);
			allPromotionInfoValues.put(COLUMN_TO_NAME, message.toName);
			allPromotionInfoValues.put(COLUMN_TO_HEAD, message.toUrl);
			allPromotionInfoValues.put(COLUMN_ID, message.id);
			allPromotionInfoValues.put(COLUMN_TAG, message.tag);
			allPromotionInfoValues.put(COLUMN_CONTENT, message.content);
			allPromotionInfoValues.put(COLUMN_TYPE, message.chatType);
			allPromotionInfoValues.put(COLUMN_MESSAGE_TYPE, message.messageType);
			allPromotionInfoValues.put(COLUMN_SEND_TIME, message.time);
			allPromotionInfoValues.put(COLUMN_READ_STATE, message.readState);
			allPromotionInfoValues.put(COLUMN_SEND_STATE, message.sendState);
			allPromotionInfoValues.put(COLUMN_SAMPLE_RATE, message.sampleRate);
			allPromotionInfoValues.put(COLUMN_SYSTEM_MESSAGE, message.systemMessage);

			allPromotionInfoValues.put(COLUMN_PRIVACY_MODE, message.privacyMode);
			allPromotionInfoValues.put(COLUMN_ENCRYPT_MODE, message.encryptMode);



            Log.d("write_db","数据写入 content="+message.content+" type="+message.messageType);
			try {
				mDBStore.insertOrThrow(TABLE_NAME, null, allPromotionInfoValues);
			} catch (SQLiteConstraintException e) {
				e.printStackTrace();
			}
		}
	}

	public void insert(ChatMessage message) {
        message.convertObjectToContent();
        Log.d("write_db","数据写入  content="+message.content);

		Cursor cursor = null;

		ContentValues allPromotionInfoValues = new ContentValues();

		allPromotionInfoValues.put(COLUMN_FROM_UID, message.fromId);
		allPromotionInfoValues.put(COLUMN_FROM_NAME, message.fromName);
		allPromotionInfoValues.put(COLUMN_FROM_HEAD, message.fromUrl);
		allPromotionInfoValues.put(COLUMN_LOGIN_ID, IMCommon.getUserId(ChatApplication.getInstance()));
		allPromotionInfoValues.put(COLUMN_TO_ID, message.toId);
		allPromotionInfoValues.put(COLUMN_TO_NAME, message.toName);
		allPromotionInfoValues.put(COLUMN_TO_HEAD, message.toUrl);
		allPromotionInfoValues.put(COLUMN_ID, message.id);
		allPromotionInfoValues.put(COLUMN_TAG, message.tag);
		allPromotionInfoValues.put(COLUMN_CONTENT, message.content);
		allPromotionInfoValues.put(COLUMN_TYPE, message.chatType);
		allPromotionInfoValues.put(COLUMN_MESSAGE_TYPE, message.messageType);
		allPromotionInfoValues.put(COLUMN_SEND_TIME, message.time);
		allPromotionInfoValues.put(COLUMN_READ_STATE, message.readState);
		allPromotionInfoValues.put(COLUMN_SEND_STATE, message.sendState);


		allPromotionInfoValues.put(COLUMN_SAMPLE_RATE, message.sampleRate);
		allPromotionInfoValues.put(COLUMN_SYSTEM_MESSAGE, message.systemMessage);


		allPromotionInfoValues.put(COLUMN_PRIVACY_MODE, message.privacyMode);
		allPromotionInfoValues.put(COLUMN_ENCRYPT_MODE, message.encryptMode);


		Log.d("write_db","数据写入 content="+message.content+" type="+message.messageType);

		try {
			mDBStore.insertOrThrow(TABLE_NAME, null, allPromotionInfoValues);
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	public void delete(ChatMessage message) {
		//mDBStore.delete(TABLE_NAME, COLUMN_TAG + "='" + message.tag + "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);


		mDBStore.delete(TABLE_NAME, COLUMN_TAG + "='" + message.tag + "' ", null);
	}



	public void delete(String messageTag) {
		//mDBStore.delete(TABLE_NAME, COLUMN_TAG + "='" + message.tag + "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);


		mDBStore.delete(TABLE_NAME, COLUMN_TAG + "='" + messageTag + "' ", null);
	}

	public boolean delete(String toId, int type) {
		try {

			if(type == ChatType.GroupMessage){//群聊
				mDBStore.delete(TABLE_NAME, COLUMN_TO_ID + "='" + toId + "'" + " AND " + COLUMN_TYPE + "="+ ChatType.GroupMessage + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);

			}else if(type == ChatType.MeetingMessage){
				mDBStore.delete(TABLE_NAME, COLUMN_TO_ID + "='" + toId + "'" + " AND " + COLUMN_TYPE + "="+ChatType.MeetingMessage + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			}else  if(type == ChatType.PrivateMessage){
				mDBStore.delete(TABLE_NAME, (COLUMN_FROM_UID + "='" + toId + "' or " + COLUMN_TO_ID + "='" + toId + "'") + " AND "  + COLUMN_TYPE + "="+ChatType.PrivateMessage + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}


	public boolean deletePrivacyModeHadReadData(String toId, int type) {
		try {

			if(type == ChatType.GroupMessage){//群聊
				mDBStore.delete(TABLE_NAME, COLUMN_TO_ID + "='" + toId + "'" + " AND " + COLUMN_TYPE + "="+ ChatType.GroupMessage + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "' AND "+COLUMN_READ_STATE+"=1  AND "+COLUMN_PRIVACY_MODE+"=1", null);

			}else if(type == ChatType.MeetingMessage){
				mDBStore.delete(TABLE_NAME, COLUMN_TO_ID + "='" + toId + "'" + " AND " + COLUMN_TYPE + "="+ChatType.MeetingMessage + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"+ " AND "+COLUMN_READ_STATE+"=1  AND "+COLUMN_PRIVACY_MODE+"=1", null);
			}else  if(type == ChatType.PrivateMessage){
				mDBStore.delete(TABLE_NAME, (COLUMN_FROM_UID + "='" + toId + "' or " + COLUMN_TO_ID + "='" + toId + "'") + " AND "  + COLUMN_TYPE + "="+ChatType.PrivateMessage + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"+ " AND "+COLUMN_READ_STATE+"=1   AND "+COLUMN_PRIVACY_MODE+"=1", null);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean updateMessage(ChatMessage message){
		ContentValues allPromotionInfoValues = new ContentValues();
		allPromotionInfoValues.put(COLUMN_ID, message.id);

		allPromotionInfoValues.put(COLUMN_SEND_TIME, message.time);

		allPromotionInfoValues.put(COLUMN_SEND_STATE, message.sendState);
		allPromotionInfoValues.put(COLUMN_READ_STATE, message.readState);


		allPromotionInfoValues.put(COLUMN_SYSTEM_MESSAGE, message.systemMessage);
        //更新内容
        allPromotionInfoValues.put(COLUMN_CONTENT, message.content);


		try {
			mDBStore.update(TABLE_NAME, allPromotionInfoValues, COLUMN_TAG + "='" + message.tag + "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean update(ChatMessage message){
		ContentValues allPromotionInfoValues = new ContentValues();
		allPromotionInfoValues.put(COLUMN_SEND_STATE, message.sendState);
		allPromotionInfoValues.put(COLUMN_READ_STATE, message.readState);

		try {
			mDBStore.update(TABLE_NAME, allPromotionInfoValues, COLUMN_TAG + "='" + message.tag + "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'", null);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean updateVoiceContent(String tag, String content){

		try {
			String sql = "UPDATE " + TABLE_NAME + " SET " + COLUMN_CONTENT + "='" + content + "' WHERE " + COLUMN_TAG + "='" + tag + "' AND " + COLUMN_TYPE + "="+ ChatType.PrivateMessage+" AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'";
			mDBStore.execSQL(sql);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean updateReadState(String id, int type){

		try {
			String sql = "";
			if(type ==  ChatType.PrivateMessage){
				sql = "UPDATE " + TABLE_NAME + " SET " + COLUMN_READ_STATE + "=1 WHERE " + (COLUMN_FROM_UID + "='" + id + "' OR " + COLUMN_TO_ID + "='" + id) + "' AND "  + COLUMN_TYPE + "="+  ChatType.PrivateMessage+" AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'";
			}else if(type == ChatType.GroupMessage){
				sql = "UPDATE " + TABLE_NAME + " SET " + COLUMN_READ_STATE + "=1 WHERE " + COLUMN_TO_ID + "='" + id + "' AND " + COLUMN_TYPE + "="+ ChatType.GroupMessage+" AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'";
			}else if(type ==  ChatType.MeetingMessage){
				sql = "UPDATE " + TABLE_NAME + " SET " + COLUMN_READ_STATE + "=1 WHERE " + COLUMN_TO_ID + "='" + id + "' AND " + COLUMN_TYPE + "="+  ChatType.MeetingMessage+" AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'";
			}
			mDBStore.execSQL(sql);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean updatePrivateReadState(String id){

		try {
			String sql = "UPDATE " + TABLE_NAME + " SET " + COLUMN_READ_STATE + "=1 WHERE " + (COLUMN_FROM_UID + "='" + id + "' OR " + COLUMN_TO_ID + "='" + id) + "' AND " + COLUMN_TYPE + "="+  ChatType.PrivateMessage+" AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'";
			mDBStore.execSQL(sql);
			return true;
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

		return false;
	}



	public ChatMessage query(String tag){
		ChatMessage message = new ChatMessage();
		Cursor cursor = null;
		try{
			String querySql = "";
			querySql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_TAG + "='" + tag + "'";
			cursor = mDBStore.rawQuery(querySql, null);
			Log.d("chat_log","聊天记录，单条 tag="+tag
            );
			if (cursor != null) {

				if (!cursor.moveToFirst()) {
					return null;
				}

				int indexFromId = cursor.getColumnIndex(COLUMN_FROM_UID);
				int indexFromName = cursor.getColumnIndex(COLUMN_FROM_NAME);
				int indexFromHead = cursor.getColumnIndex(COLUMN_FROM_HEAD);
				int indexToID = cursor.getColumnIndex(COLUMN_TO_ID);
				int indexToName = cursor.getColumnIndex(COLUMN_TO_NAME);
				int indexToHead = cursor.getColumnIndex(COLUMN_TO_HEAD);
				int indexMessageId = cursor.getColumnIndex(COLUMN_ID);
				int indexMessageTag = cursor.getColumnIndex(COLUMN_TAG);
				int indexContent = cursor.getColumnIndex(COLUMN_CONTENT);
				int indexType = cursor.getColumnIndex(COLUMN_TYPE);
				int indexMessageType = cursor.getColumnIndex(COLUMN_MESSAGE_TYPE);
				int indexSendTime = cursor.getColumnIndex(COLUMN_SEND_TIME);
				int indexReadState = cursor.getColumnIndex(COLUMN_READ_STATE);
				int indexSendState = cursor.getColumnIndex(COLUMN_SEND_STATE);
				int indexSystemMessage = cursor.getColumnIndex(COLUMN_SYSTEM_MESSAGE);


				message.fromId = cursor.getString(indexFromId);
				message.fromName = cursor.getString(indexFromName);
				message.fromUrl = cursor.getString(indexFromHead);
				message.toId = cursor.getString(indexToID);
				message.toName = cursor.getString(indexToName);
				message.toUrl = cursor.getString(indexToHead);
				message.id = cursor.getString(indexMessageId);
				message.tag = cursor.getString(indexMessageTag);
				message.content = cursor.getString(indexContent);

				message.chatType = cursor.getInt(indexType);
				message.messageType = cursor.getInt(indexMessageType);
				message.systemMessage = cursor.getInt(indexSystemMessage);

				message.time = cursor.getLong(indexSendTime);
				message.readState = cursor.getInt(indexReadState);
				message.sendState = cursor.getInt(indexSendState);
				message.convertContentToObject();
				return message;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;

	}

	public ChatMessage queryByID(String id){
		ChatMessage message = new ChatMessage();
		Cursor cursor = null;
		try{
			String querySql = "";
			querySql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "='" + id + "'";
			cursor = mDBStore.rawQuery(querySql, null);
			if (cursor != null) {

				if (!cursor.moveToFirst()) {
					return null;
				}

				int indexFromId = cursor.getColumnIndex(COLUMN_FROM_UID);
				int indexFromName = cursor.getColumnIndex(COLUMN_FROM_NAME);
				int indexFromHead = cursor.getColumnIndex(COLUMN_FROM_HEAD);
				int indexToID = cursor.getColumnIndex(COLUMN_TO_ID);
				int indexToName = cursor.getColumnIndex(COLUMN_TO_NAME);
				int indexToHead = cursor.getColumnIndex(COLUMN_TO_HEAD);
				int indexMessageId = cursor.getColumnIndex(COLUMN_ID);
				int indexMessageTag = cursor.getColumnIndex(COLUMN_TAG);
				int indexContent = cursor.getColumnIndex(COLUMN_CONTENT);
				int indexType = cursor.getColumnIndex(COLUMN_TYPE);
				int indexMessageType = cursor.getColumnIndex(COLUMN_MESSAGE_TYPE);
				int indexSendTime = cursor.getColumnIndex(COLUMN_SEND_TIME);
				int indexReadState = cursor.getColumnIndex(COLUMN_READ_STATE);
				int indexSendState = cursor.getColumnIndex(COLUMN_SEND_STATE);
				int indexSystemMessage = cursor.getColumnIndex(COLUMN_SYSTEM_MESSAGE);

				message.fromId = cursor.getString(indexFromId);
				message.fromName = cursor.getString(indexFromName);
				message.fromUrl = cursor.getString(indexFromHead);
				message.toId = cursor.getString(indexToID);
				message.toName = cursor.getString(indexToName);
				message.toUrl = cursor.getString(indexToHead);
				message.id = cursor.getString(indexMessageId);
				message.tag = cursor.getString(indexMessageTag);
				message.content = cursor.getString(indexContent);
				message.chatType = cursor.getInt(indexType);
				message.messageType = cursor.getInt(indexMessageType);
				message.systemMessage = cursor.getInt(indexSystemMessage);
				message.time = cursor.getLong(indexSendTime);
				message.readState = cursor.getInt(indexReadState);
				message.sendState = cursor.getInt(indexSendState);
				message.convertContentToObject();
				return message;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;

	}

	/**
	 * 根据内容查找
	 * @param searchContent
	 * @param toId
	 * @param type
	 * @return
	 */
	public List<MainSearchEntity> queryByContent(String searchContent,String toId,int type){
		List<MainSearchEntity>  allInfo = new ArrayList<MainSearchEntity>();
		Cursor cursor = null;
		try {
			String querySql = "";
			if(type == 0){//查询所有的数据（包括单聊和群聊）
				querySql = "SELECT * FROM " + TABLE_NAME + " WHERE "+ COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"
						+" AND " +  COLUMN_CONTENT +" like '%"+searchContent+"%' "
						+ " AND "+ COLUMN_MESSAGE_TYPE +" ="+MessageType.TEXT;
			}else{
				if(type ==   ChatType.PrivateMessage && (toId!=null && !toId.equals(""))){
					querySql = "SELECT * FROM " + TABLE_NAME
							+ " WHERE (" + COLUMN_FROM_UID + "='" + toId + "' or " + COLUMN_TO_ID + "='" + toId + "')"
							+ " AND " + COLUMN_TYPE + "=" + type
							+ " AND "+COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"
							+" AND " +COLUMN_CONTENT +" like '%"+searchContent+"%' "
							+ " AND "+ COLUMN_MESSAGE_TYPE +" ="+MessageType.TEXT;
				}else if(type ==   ChatType.GroupMessage&& (toId!=null && !toId.equals(""))){
					querySql = "SELECT * FROM " + TABLE_NAME + " WHERE "+ COLUMN_TO_ID + "='" + toId + "'"
							+ " AND " + COLUMN_TYPE + "=" + type
							+ " AND "+COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"
							+" AND " +COLUMN_CONTENT +" like '%"+searchContent+"%' "
							+ " AND "+ COLUMN_MESSAGE_TYPE +" ="+MessageType.TEXT;
				}
			}



			cursor = mDBStore.rawQuery(querySql, null);
			if (cursor != null) {

				if (!cursor.moveToFirst()) {
					return null;
				}

				int indexFromId = cursor.getColumnIndex(COLUMN_FROM_UID);
				int indexFromName = cursor.getColumnIndex(COLUMN_FROM_NAME);
				int indexFromHead = cursor.getColumnIndex(COLUMN_FROM_HEAD);
				int indexToID = cursor.getColumnIndex(COLUMN_TO_ID);
				int indexToName = cursor.getColumnIndex(COLUMN_TO_NAME);
				int indexToHead = cursor.getColumnIndex(COLUMN_TO_HEAD);
				int indexMessageId = cursor.getColumnIndex(COLUMN_ID);
				int indexMessageTag = cursor.getColumnIndex(COLUMN_TAG);
				int indexContent = cursor.getColumnIndex(COLUMN_CONTENT);
				int indexType = cursor.getColumnIndex(COLUMN_TYPE);
				int indexMessageType = cursor.getColumnIndex(COLUMN_MESSAGE_TYPE);
				int indexSendTime = cursor.getColumnIndex(COLUMN_SEND_TIME);
				int indexReadState = cursor.getColumnIndex(COLUMN_READ_STATE);
				int indexSendState = cursor.getColumnIndex(COLUMN_SEND_STATE);
				int indexSystemMessage = cursor.getColumnIndex(COLUMN_SYSTEM_MESSAGE);


				do{
					int chatType = cursor.getInt(indexType);
					String nickname = cursor.getString(indexToName);
					String headUrl = cursor.getString(indexToHead);
					String uid = cursor.getString(indexToID);
					if(chatType ==  ChatType.PrivateMessage){

					}else if(chatType ==   ChatType.GroupMessage ){
						SessionTable sesTab = new SessionTable(mDBStore);
						Session session = sesTab.query( cursor.getString(indexToID),  ChatType.GroupMessage);
						if(session!=null){
							nickname = session.name;
							headUrl = session.heading;
							uid = session.getFromId();
						}
					}

					MainSearchEntity message = new MainSearchEntity("聊天记录",chatType,nickname ,
							cursor.getString(indexContent), headUrl,
							cursor.getLong(indexSendTime), 2,uid, searchContent,"");
					/*message.fromId = cursor.getString(indexFromId);
					message.fromName = cursor.getString(indexFromName);
					message.fromUrl = cursor.getString(indexFromHead);
					message.toId = cursor.getString(indexToID);
					message.toName = cursor.getString(indexToName);
					message.toUrl = cursor.getString(indexToHead);
					message.tid = cursor.getString(indexMessageId);
					message.tag = cursor.getString(indexMessageTag);
					message.content = cursor.getString(indexContent);
					message.imgUrlS = cursor.getString(indexImgUrls);
					message.imgUrlL = cursor.getString(indexImgUrlL);
					message.voiceUrl = cursor.getString(indexVoiceUrl);
					message.messageType= cursor.getInt(indexMessageType);
					message.imgWidth = cursor.getInt(indexImgWidth);
					message.imgHeight = cursor.getInt(indexImgHeight);
					message.mLat = Double.parseDouble(cursor.getString(indexLatID));
					message.mLng = Double.parseDouble(cursor.getString(indexLngID));
					message.mAddress = cursor.getString(indexAddressID);

					message.time = cursor.getLong(indexSendTime);
					message.voiceTime = cursor.getInt(indexVoiceTime);
					message.readState = cursor.getInt(indexReadState);
					message.sendState = cursor.getInt(indexSendState);
					message.isReadVoice = cursor.getInt(indexVoiceReadState);
					message.systemMessage = cursor.getInt(indexSystemMessage);
					message.voiceString = cursor.getString(indexVoiceString);
					message.imageString = cursor.getString(indexImageString);*/

					allInfo.add(0, message);
				} while (cursor.moveToNext());

				return allInfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}


	public List<ChatMessage> query(String toId, int autoID, int type){
		List<ChatMessage> allInfo = new ArrayList<ChatMessage>();
		Cursor cursor = null;
		try {

			String querySql = "";

			if(type ==  ChatType.PrivateMessage){
				if(autoID == -1){
					querySql = "SELECT rowid, * FROM " + TABLE_NAME + " WHERE (" + COLUMN_FROM_UID + "='" + toId + "' or " + COLUMN_TO_ID + "='" + toId + "')" + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"
							+ " AND " + COLUMN_TYPE + "=" + type +  " ORDER BY rowid" + " DESC LIMIT 0," + IMCommon.LOAD_SIZE;
				}else {
					querySql = "SELECT rowid, * FROM " + TABLE_NAME + " WHERE (" + COLUMN_FROM_UID + "='" + toId + "' or " + COLUMN_TO_ID + "='" + toId + "')" + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"
							+ " AND " + " rowid<" + autoID  + " AND " + COLUMN_TYPE + "=" + type +  " ORDER BY rowid" + " DESC LIMIT 0," + IMCommon.LOAD_SIZE;
				}

			}else {
				if(autoID == -1){
					querySql = "SELECT rowid, * FROM " + TABLE_NAME + " WHERE " + COLUMN_TO_ID + "='" + toId + "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"
							+ " AND " + COLUMN_TYPE + "=" + type +  " ORDER BY rowid" + " DESC LIMIT 0," + IMCommon.LOAD_SIZE;
				}else {
					querySql = "SELECT rowid, * FROM " + TABLE_NAME + " WHERE " + COLUMN_TO_ID + "='" + toId + "' AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"
							+ " AND " + " rowid<" + autoID  + " AND " + COLUMN_TYPE + "=" + type + " ORDER BY rowid" + " DESC LIMIT 0," + IMCommon.LOAD_SIZE;
				}

			}

			cursor = mDBStore.rawQuery(querySql, null);

			if (cursor != null) {
			    Log.d("chat_log","查询到数据");
				if (!cursor.moveToFirst()) {
					return null;
				}

				int indexRowId = cursor.getColumnIndex("rowid");
				int indexFromId = cursor.getColumnIndex(COLUMN_FROM_UID);
				int indexFromName = cursor.getColumnIndex(COLUMN_FROM_NAME);
				int indexFromHead = cursor.getColumnIndex(COLUMN_FROM_HEAD);
				int indexToID = cursor.getColumnIndex(COLUMN_TO_ID);
				int indexToName = cursor.getColumnIndex(COLUMN_TO_NAME);
				int indexToHead = cursor.getColumnIndex(COLUMN_TO_HEAD);
				int indexMessageId = cursor.getColumnIndex(COLUMN_ID);
				int indexMessageTag = cursor.getColumnIndex(COLUMN_TAG);
				int indexContent = cursor.getColumnIndex(COLUMN_CONTENT);
				int indexType = cursor.getColumnIndex(COLUMN_TYPE);
				int indexMessageType = cursor.getColumnIndex(COLUMN_MESSAGE_TYPE);
				int indexSendTime = cursor.getColumnIndex(COLUMN_SEND_TIME);
				int indexReadState = cursor.getColumnIndex(COLUMN_READ_STATE);
				int indexSendState = cursor.getColumnIndex(COLUMN_SEND_STATE);
				int indexSystemMessage = cursor.getColumnIndex(COLUMN_SYSTEM_MESSAGE);

				//文件相关字段

				do{

					ChatMessage message = new ChatMessage();
					//固定字段
					message.fromId = cursor.getString(indexFromId);
					message.fromName = cursor.getString(indexFromName);
					message.fromUrl = cursor.getString(indexFromHead);
					message.toId = cursor.getString(indexToID);
					message.toName = cursor.getString(indexToName);
					message.toUrl = cursor.getString(indexToHead);
					message.id = cursor.getString(indexMessageId);
					message.tag = cursor.getString(indexMessageTag);
					message.content = cursor.getString(indexContent);
					message.chatType = cursor.getInt(indexType);
					message.messageType= cursor.getInt(indexMessageType);
					message.time = cursor.getLong(indexSendTime);
					message.readState = cursor.getInt(indexReadState);
					message.sendState = cursor.getInt(indexSendState);
					message.systemMessage = cursor.getInt(indexSystemMessage);
					message.auto_id = cursor.getInt(indexRowId);
					//JSON对象转换
                    Log.d("chat_log","本地缓存调用 message.content ="+message.content+" type="+message.messageType);
                    message.convertContentToObject();
					allInfo.add(0, message);
				} while (cursor.moveToNext());

				return allInfo;
			}else {
                Log.d("chat_log","数据是空");
            }
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public int queryUnreadCountByID(String id, int type){
		Cursor cursor = null;
		try {

			String querySql = "";

			if(type == ChatType.PrivateMessage){
				querySql = "SELECT rowid, * FROM " + TABLE_NAME + " WHERE " + COLUMN_FROM_UID + "='" + id
						+ "' AND " + COLUMN_TYPE + "="+ChatType.PrivateMessage+" AND " + COLUMN_READ_STATE + "=0" + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"
						+ " ORDER BY " + COLUMN_SEND_TIME + " DESC ";
			}else {
				querySql = "SELECT rowid, * FROM " + TABLE_NAME + " WHERE " + COLUMN_TO_ID + "='" + id
						+ "' AND " + COLUMN_TYPE + "=type AND " + COLUMN_READ_STATE + "=0" + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'"
						+ " ORDER BY " + COLUMN_SEND_TIME + " DESC ";
			}

			cursor = mDBStore.rawQuery(querySql, null);
			if (cursor != null) {

				if (!cursor.moveToFirst()) {
					return 0;
				}

				return cursor.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return 0;
	}

	public int queryUnreadCount(){
		Cursor cursor = null;
		try {

			String querySql = "";
			querySql = "SELECT rowid, * FROM " + TABLE_NAME + " WHERE " + COLUMN_READ_STATE + "=0" + " AND " + COLUMN_LOGIN_ID + "='" + IMCommon.getUserId(ChatApplication.getInstance()) + "'";

			cursor = mDBStore.rawQuery(querySql, null);
			if (cursor != null) {

				if (!cursor.moveToFirst()) {
					return 0;
				}

				return cursor.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return 0;
	}


}
