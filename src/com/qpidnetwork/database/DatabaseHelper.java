package com.qpidnetwork.database;

import java.util.HashMap;

import com.qpidnetwork.framework.util.Log;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Max.Chiu
 * 数据库管理
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private class TableColumnInfo {
		String cid = "";
		String name = "";
		String type = "";
	};
	
	private static String DATABASE_NAME_PRE = "qpidnetwork_";
	private static String DATABASE_NAME_SUF = ".db";
	
	private static int DATABASE_VERSION = 1;
	private static DatabaseHelper databaseHelperInstent;

	static final String SELECT_ALL_TABLE_NAME = "SELECT name FROM sqlite_master WHERE type = 'table';";

	// sql format
	static final String SELECT_FORMAT = "SELECT %s FROM %s;";
	static final String INSERT_SELECT_FORMAT = "INSERT INTO %s (%s) SELECT %s FROM %s;";
	static final String DROP_TABLE_FORMAT = "DROP TABLE %s;";
	static final String ALERT_TABLE_FORMAT = "ALTER TABLE %s RENAME TO %s;";
	static final String PRAGMA_TABLE_INFO_FORMAT = "PRAGMA table_info('%s');";

	/* system table name */
	//static final String ANDROID_METADATA = "android_metadata";
	//static final String SQLITE_SEQUENCE = "sqlite_sequence";
	static final String ANDROID_METADATA 		 = "android_";
	static final String SQLITE_SEQUENCE			 = "sqlite_";
	static final String OLD_TABLE_NAME_SUFFIX 	 = "_bak";

	private DatabaseHelper(Context context, String databaseName) {
		super(context, databaseName, null, DATABASE_VERSION);
	}

	/**
	 * 获取数据实例
	 * @param context
	 * @param key			自定义数据库名字
	 * @return
	 */
	public static DatabaseHelper getInstance(Context context, String key) {
		if( databaseHelperInstent == null ) {
			databaseHelperInstent = new DatabaseHelper(context, DATABASE_NAME_PRE + key + DATABASE_NAME_SUF);
			return databaseHelperInstent;
		} else {
			return databaseHelperInstent;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.d("DatabaseHelper.onCreate", "onCreate");
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		Log.d("DatabaseHelper.onOpen", "onOpen");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.d("DatabaseHelper.onUpgrade", "onUpgrade");
		clearDatabase(db);
	}
	
	/**
	 * 新建表的时候调用，寻找与对应的旧表并做数据迁移
	 * 根据对应关系找到旧表 (如:新表为testtable，旧表则为testtable_bak)
	 * 新旧表数据迁移
	 * 删除旧表
	 * 
	 * @param db					数据库实例
	 * @param tableName				新表名
	 */
	public void updateTable(SQLiteDatabase db, String tableName) {
		Log.d("DatabaseHelper.updateTable", tableName);
		int table_name_index;
		String oldTableName = tableName + OLD_TABLE_NAME_SUFFIX;
		String selectTableName = "";
		Cursor cursor;
		cursor = db.rawQuery(SELECT_ALL_TABLE_NAME, null);
		if(null != cursor){
			if(cursor.moveToFirst()) {
				while(!cursor.isAfterLast()) {
					table_name_index = cursor.getColumnIndex("name");
					if(-1 != table_name_index) {
						selectTableName = cursor.getString(table_name_index);
						// 根据对应关系找到旧表(如:新表为testtable，旧表则为testtable_bak)
						if( selectTableName.equals(oldTableName) ) {
							Log.d("DatabaseHelper.updateTable", "new table:" + tableName + "[" + oldTableName + "]");
							// 新旧表数据迁移
							moveOldTableData2NewTable(db, tableName, oldTableName);
							// 删除旧表
							Log.d("DatabaseHelper.updateTable", "drop table:" + oldTableName);
							String sqlString = String.format(DROP_TABLE_FORMAT, oldTableName);
							db.execSQL(sqlString);
							break;
						}
					}
					cursor.moveToNext();
				}
			}
			cursor.close();
		}
	}
	
	/**
	 * 清空数据库表结构
	 * 获取数据库中所有的用户表表名,删除上一版本的旧表 (所有后缀为_bak的表)
	 * 将所有当前的版本的用户表表名添加后缀(_bak)
	 * 
	 * @param db		数据库实例
	 */
	private void clearDatabase(SQLiteDatabase db) {
		Log.d("DatabaseHelper.clearDatabase", "");
		int table_name_index;
		String tableName = "";
		Cursor cursor;
		cursor = db.rawQuery(SELECT_ALL_TABLE_NAME, null);
		if(null != cursor){
			if(cursor.moveToFirst()) {
				while(!cursor.isAfterLast()) {
					table_name_index = cursor.getColumnIndex("name");
					if(-1 != table_name_index) {
						tableName = cursor.getString(table_name_index);
						// 系统表 (所有android_和sqlite_开头的表)
						if( tableName.startsWith(ANDROID_METADATA) || tableName.startsWith(SQLITE_SEQUENCE) ) {
							Log.d("DatabaseHelper.clearDatabase", "sytem table:" + tableName);
						}
						// 删除上一版本的旧表 (所有后缀为_bak的表)
						else if(tableName.endsWith(OLD_TABLE_NAME_SUFFIX)) {
							Log.d("DatabaseHelper.clearDatabase", "drop old user table:" + tableName);
							String sqlString = String.format(DROP_TABLE_FORMAT, tableName);
							db.execSQL(sqlString);
						}
						// 重新命名当前的版本的用户表
						else {
							String bakTableName = tableName + OLD_TABLE_NAME_SUFFIX;
							Log.d("DatabaseHelper.clearDatabase", "alert current user table:" + tableName + "->" + bakTableName);
							String sqlString = String.format(ALERT_TABLE_FORMAT, tableName, bakTableName);
							db.execSQL(sqlString);
						}
					}
					cursor.moveToNext();
				}
			}
			cursor.close();
		}
	}

	/**
	 * 新旧表数据迁移
	 * @param db				数据库实例
	 * @param newTable			新表名
	 * @param oldTable			旧表名
	 */
	private void moveOldTableData2NewTable(SQLiteDatabase db, String newTable, String oldTable) {
		Log.d("DatabaseHelper.moveOldTableData2NewTable", "move " + oldTable + " data to " + newTable );
		HashMap<String, TableColumnInfo> newColumnsMap = getTableColumns(db, newTable);
		HashMap<String, TableColumnInfo> oldColumnsMap = getTableColumns(db, oldTable);

		String sqlSelectNameString = "";
		String sqlInsertNameString = "";
		int iColumnIndex = 0;
		for(String newColumnName : newColumnsMap.keySet()) {
			iColumnIndex++;
			// 新表字段在旧表里面
			if(oldColumnsMap.containsKey(newColumnName)) {
				Log.d("DatabaseHelper.moveOldTableData2NewTable", "oldColumnsMap containsKey:" + newColumnName );
				TableColumnInfo columnInfo = oldColumnsMap.get(newColumnName);
				sqlSelectNameString += columnInfo.name;
				if(iColumnIndex < newColumnsMap.size()){
					sqlSelectNameString += ",";
				}
			}
		}
		if( 0 < sqlSelectNameString.length()) {
			// 从旧表select指定字段 返回数据集，并将其插入到新表
			sqlInsertNameString = sqlSelectNameString;
			String sqlString = String.format(INSERT_SELECT_FORMAT, newTable, sqlInsertNameString, sqlSelectNameString, oldTable);
			try{
				db.execSQL(sqlString);
			}catch (Exception e) {
				// TODO: handle exception
			}
		}

	}
	
	/**
	 * 获取指定表的字段
	 * @param db				数据库实例
	 * @param tableName			表名
	 * @return
	 */
	private HashMap<String, TableColumnInfo> getTableColumns(SQLiteDatabase db, String tableName) {
		Log.d("DatabaseHelper.getTableColumns", "tableName:" + tableName );
		HashMap<String, TableColumnInfo> map = new HashMap<String, TableColumnInfo>();
		String selectAllColumnsInTable = String.format(PRAGMA_TABLE_INFO_FORMAT, tableName);
		String columnName = "";

		Cursor cursor = db.rawQuery(selectAllColumnsInTable, null);
		if(null != cursor){
			if(cursor.moveToFirst()) {
				while(!cursor.isAfterLast()) {
					TableColumnInfo columnInfo = parseColumnInfo(cursor);
					columnName = columnInfo.name;
					if( 0 < columnName.length()) {
						map.put(columnName, columnInfo);
					}
					cursor.moveToNext();
				}
			}
			cursor.close();
		}
		return map;
	}
	
	/**
	 * 获取指定行
	 * @param cursor		行指针
	 * @return				该行各字段属性
	 */
	private TableColumnInfo parseColumnInfo(Cursor cursor) {
		int column_index_cid = 0;
		int column_index_name = column_index_cid + 1;
		int column_index_type = column_index_name + 1;

		TableColumnInfo columnInfo = new TableColumnInfo();
		if(null != cursor){
			columnInfo.cid = cursor.getString(column_index_cid);
			columnInfo.name = cursor.getString(column_index_name);
			columnInfo.type = cursor.getString(column_index_type);
		}
		return columnInfo;
	}
}
