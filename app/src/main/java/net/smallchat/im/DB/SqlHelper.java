package net.smallchat.im.DB;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SqlHelper {
	@SuppressWarnings("rawtypes")
	public static String formCreateTableSqlString(String tableName, HashMap<String, String> columnNameAndType, String primary_key){
		StringBuffer sqlCreateTable = new StringBuffer("CREATE TABLE IF NOT EXISTS " + tableName + " (");

		Iterator iterator = columnNameAndType.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next(); 
			sqlCreateTable.append(" ");
			sqlCreateTable.append(entry.getKey());
			sqlCreateTable.append(" ");
			sqlCreateTable.append(entry.getValue());
			sqlCreateTable.append(",");
		}

		//sqlCreateTable.deleteCharAt(sqlCreateTable.lastIndexOf(","));
		sqlCreateTable.append(primary_key);

		sqlCreateTable.append(");");

		return sqlCreateTable.toString();
	}
	
	public static String formDeleteTableSqlString(String tableName) {
		return "DROP TABLE IF EXISTS " + tableName;
	}
}
