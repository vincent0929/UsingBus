package com.vincent.bus.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDataBase extends SQLiteOpenHelper {

	public static final String TABLE_NAME_BUSLINE = "busLine";
	public static final String TABLE_NAME_STATION="station";
	public static final String COLUMN_NAME_ID = "_id";
	public static final String COLUMN_NAME_BUSLINE_NAME = "name";
	public static final String COLUMN_NAME_BUSLINE_ORIGIN_STATION= "originStation";
	public static final String COLUMN_NAME_BUSLINE_TERMINAL_STATION = "terminalStation";
	public static final String COLUMN_NAME_STATION_NAME = "stationName";
	public static final String COLUMN_NAME_STATION_BUSLINES = "busLines";
	

	public MyDataBase(Context context) {
		super(context, "SearchDataBase", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "+TABLE_NAME_BUSLINE+"("+
		COLUMN_NAME_ID+" text primary key ,"+
		COLUMN_NAME_BUSLINE_NAME+" text not null default \"\","+
		COLUMN_NAME_BUSLINE_ORIGIN_STATION+" text not null default \"\","+
		COLUMN_NAME_BUSLINE_TERMINAL_STATION+" text not null default \"\""+")");
		db.execSQL("create table "+TABLE_NAME_STATION+"("+
				COLUMN_NAME_ID+" text primary key,"+
				COLUMN_NAME_STATION_NAME+" text not null default \"\","+
				COLUMN_NAME_STATION_BUSLINES+" text not null default \"\""+")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
