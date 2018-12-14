package com.worldline.android.loginexttest.commons.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by prasad.mukne on 7/24/2017.
 */

public class SQLiteDatabaseManager
{
	private static SQLiteDatabaseManager sqLiteDatabaseManager;

	private SQLiteDatabase sqLiteDatabase;

	public static final String LOCATION_TABLE = "Locationtable";

	public static final String ROW_ID = "_id";

	public static final String LATITUDE = "latitude";

	public static final String LONGITUDE = "longitude";

	public static final String TIMESTAMP = "timestamp";

	private SQLiteDatabaseManager(Context context)
	{
		sqLiteDatabase=new DataBaseOpenHelper(context).getWritableDatabase();
	}

	public static synchronized SQLiteDatabaseManager getInstance(Context context)
	{
		if(null==sqLiteDatabaseManager)
		{
			sqLiteDatabaseManager=new SQLiteDatabaseManager(context);
		}
		return sqLiteDatabaseManager;
	}

	public void createLocationTable()
	{
		String[] requestTableColumnNames = {SQLiteDatabaseManager.TIMESTAMP,SQLiteDatabaseManager.LATITUDE,SQLiteDatabaseManager.LONGITUDE};
		String[] requestTableColumnDataTypes = {"Text", "Text","Text"};
		createTable(SQLiteDatabaseManager.LOCATION_TABLE, requestTableColumnNames, requestTableColumnDataTypes);

	}

	public void createTable(String tableName, String[] columnNames, String[] dataTypes)
	{
		try
		{
			StringBuilder queryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName+ "(_id INTEGER primary key autoincrement");
			for (int i = 0; i < columnNames.length; i++)
			{
				queryBuilder.append(" ,");
				queryBuilder.append(columnNames[i]);
				queryBuilder.append(" ");
				queryBuilder.append(dataTypes[i]);
			}
			queryBuilder.append(");");
			sqLiteDatabase.execSQL(queryBuilder.toString());

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean isTableExists(String tableName)
	{
		boolean isTableExists = false;
		Cursor cursor = getAllTableNames();
		if (cursor != null)
		{
			for (int i = 0; i < cursor.getCount(); i++)
			{
				String str = cursor.getString(0);

				if ((str != null) && (str.equals(tableName)))
				{
					isTableExists = true;
					break;
				}
				else
				{
					cursor.moveToNext();
				}
			}
			cursor.close();
		}
		return isTableExists;
	}

	public Cursor getAllTableNames()
	{
		Cursor mCursor = sqLiteDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table' and name not like 'sqlite_sequence'  and name not like 'android_metadata';",null);
		if (mCursor != null)
		{
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public void insert(String tableName, ContentValues selectionArgs)
	{
		Log.d("database ","insert"+selectionArgs);
		sqLiteDatabase.insert(tableName, null, selectionArgs);
	}

	public boolean update(String tableName, String[] data, int rowId, String[] columnNames)
	{
		ContentValues updateValues = createContentValues(data, columnNames);
		return sqLiteDatabase.update(tableName, updateValues, "_id" + "=" + rowId, null) > 0;
	}

	public boolean update(String tableName, ContentValues updateValues, String updateQuery)
	{
		return sqLiteDatabase.update(tableName, updateValues,updateQuery, null) > 0;
	}

	public boolean delete(String tableName, String WhereClause, String[] selectionArgs)
	{
		return sqLiteDatabase.delete(tableName, WhereClause, selectionArgs)> 0;
	}

	public boolean deleteOnRowId(String tableName, int rowId)
	{
		return sqLiteDatabase.delete(tableName, "_id" + "=" + rowId, null) > 0;
	}

	public Cursor search(String rawQuery)
	{
		return sqLiteDatabase.rawQuery(rawQuery, null);
	}

	public void drop(String entity)
	{
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + entity + " ;");
	}


	public void alterTableName(String oldName, String newName)
	{
		sqLiteDatabase.execSQL("ALTER TABLE " + oldName + " RENAME TO " + newName + ";");
	}

	public void clearTable(String tableName)
	{

		String queryString = " DELETE FROM " + tableName + " ; ";
		sqLiteDatabase.execSQL(queryString);
	}

	public Cursor rawQuery(String query, String[] selectionArgs)
	{
		return sqLiteDatabase.rawQuery(query, selectionArgs);
	}

	private ContentValues createContentValues(String[] data, String[] columnNames)
	{
		ContentValues values = new ContentValues();
		try
		{
			for (int i = 0; i < columnNames.length; i++)
			{
				values.put(columnNames[i], data[i]);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return values;
	}
}
