package com.worldline.android.loginexttest.application;

import android.support.multidex.MultiDexApplication;
import com.worldline.android.loginexttest.commons.database.SQLiteDatabaseManager;

public class MyApplication extends MultiDexApplication
{

	@Override
	public void onCreate()
	{
		super.onCreate();

		SQLiteDatabaseManager.getInstance(this).createLocationTable();
	}
}
