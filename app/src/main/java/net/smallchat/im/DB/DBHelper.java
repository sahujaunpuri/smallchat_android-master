package net.smallchat.im.DB;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	private SQLiteDatabase mDB = null;
	private static DBHelper mInstance = null;
	public static final String DataBaseName = "im.db";
	public static final int DataBaseVersion = 14;

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (null == mDB) {
			mDB = db;
		}
		db.execSQL(SessionTable.getCreateTableSQLString());
		db.execSQL(UserTable.getCreateTableSQLString());
		db.execSQL(MessageTable.getCreateTableSQLString());
		db.execSQL(GroupTable.getCreateTableSQLString());
		db.execSQL(RoomTable.getCreateTableSQLString());
		
		
	}
	
	public synchronized static DBHelper getInstance(Context context){
		if (mInstance == null) {
			mInstance = new DBHelper(context, DataBaseName, null, DataBaseVersion);
		}
		
		return mInstance;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SessionTable.getDeleteTableSQLString());
		db.execSQL(UserTable.getDeleteTableSQLString());
		db.execSQL(MessageTable.getDeleteTableSQLString());
		db.execSQL(GroupTable.getDeleteTableSQLString());
		db.execSQL(RoomTable.getDeleteTableSQLString());
		onCreate(db);
	}
	
	@Override
	public synchronized void close() {
		if (mDB != null){
			mDB.close();
		}
		super.close();
	}
}
