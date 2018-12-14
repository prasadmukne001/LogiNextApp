package com.worldline.android.loginexttest.commons.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by prasad.mukne on 7/24/2017.
 */

public class DataBaseOpenHelper extends SQLiteOpenHelper
{

	private static final String DATABASE_NAME = "LogiNext.db";

	private static final int DATABASE_VERSION = 1;

	public DataBaseOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		onCreate(db);
	}

}