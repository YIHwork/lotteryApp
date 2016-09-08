package com.hy.datas;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class LotteryBallTypeSQLiteOpenHelper extends SQLiteOpenHelper {

	public final String CACHE_TABLE_NAME = "cache_betting_table";
	private final String CREATE_TABLE_SQL = "create table " + CACHE_TABLE_NAME
			+ "(_id integer primary key autoincrement, " + "lottery_number text not null, " + "betting_count integer, "
			+ "betting_multiple integer, " + "betting_money text)";

	public LotteryBallTypeSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public LotteryBallTypeSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version,
			DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
