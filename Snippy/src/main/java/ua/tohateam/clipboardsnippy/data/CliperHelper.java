package ua.tohateam.clipboardsnippy.data;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.util.*;
import ua.tohateam.clipboardsnippy.*;
import ua.tohateam.clipboardsnippy.services.*;

public class CliperHelper {
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CLIPTITLE = "title";
	public static final String KEY_CLIPBODY = "body";
	public static final String KEY_CLIPFIX = "fixed";

	private static final String TAG = "ClipsDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "ClipboardMonitor.db";
	private static final String SQLITE_TABLE = "History";
	private static final int DATABASE_VERSION = 1;

	private final Context mCtx;

	private int maxNotes = ClipboardMonitorService.mHistorySize;

	private static final String CREATE_CLIPS_TABLE = "CREATE TABLE if not exists " 
	+ SQLITE_TABLE + "(" 
	+ KEY_ROWID + " INTEGER PRIMARY KEY autoincrement," 
	+ KEY_CLIPTITLE + " TEXT," 
	+ KEY_CLIPBODY + " TEXT," 
	+ KEY_CLIPFIX + " INTEGER DEFAULT 0" 
	+ ")";

	private static final String[] DATABASE_FIELDS = {
		KEY_ROWID,
		KEY_CLIPTITLE,
		KEY_CLIPBODY,
		KEY_CLIPFIX
	};

	public CliperHelper(Context ctx) {
		this.mCtx = ctx;
	}

	public CliperHelper open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	/*********************************************************************
	 * Создание/Обновление записей
	 *********************************************************************/
	 
	// Добавить новую запись
	public long createClip(String clipBody, int fixed) {
		String title = "";
		if (clipBody.length() > 50) {
			title = clipBody.substring(0, 50);
		} else {
			title = clipBody;
		}
		title = title.trim();
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CLIPTITLE, title);
		initialValues.put(KEY_CLIPBODY, clipBody);
		initialValues.put(KEY_CLIPFIX, fixed);

		String selection = "fixed = 0";
		Cursor mCursor = mDb.query(SQLITE_TABLE, null, selection, null, null, null, KEY_CLIPFIX + " DESC");
		// если записей больше числа, то удаляем первую
		if (mCursor.getCount() > maxNotes)
			deleteFiersClip();

		return mDb.insert(SQLITE_TABLE, null, initialValues);
	}

	// обновить запись
	public long updateClip(int id, String title, String clipBody, int fixed) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CLIPTITLE, title);
		initialValues.put(KEY_CLIPBODY, clipBody);
		initialValues.put(KEY_CLIPFIX, fixed);

		return mDb.update(SQLITE_TABLE, initialValues, "_id=" + id, null);
	}

	/*********************************************************************
	 * Удаление записей
	 *********************************************************************/
	 
	// удалить все
	public boolean deleteAllClips() {
		int doneDelete = 0;
		doneDelete = mDb.delete(SQLITE_TABLE, null , null);
		Log.w(TAG, Integer.toString(doneDelete));
		return doneDelete > 0;

	}

	// Удалить все незаблокированные записи
	public int deleteAllNoBlocked() {
		int count = mDb.delete(SQLITE_TABLE, "fixed = ?", new String[] {"0"});
		return count;
	}

	// Удаление первой записи
	public void deleteFiersClip() {
		String selection = "fixed = 0";
		Cursor cursor = mDb.query(SQLITE_TABLE, null, selection, null, null, null, null);

		if (cursor.moveToFirst()) {
			String rowId = cursor.getString(cursor.getColumnIndex(KEY_ROWID));
			mDb.delete(SQLITE_TABLE, KEY_ROWID + "=?", new String[]{rowId});
		}
	}

	public void deleteClip(int rowId) {
		mDb.delete(SQLITE_TABLE, KEY_ROWID + "=" + rowId, null);
	}

	/*********************************************************************
	 * Извлечение записей
	 *********************************************************************/
	 
	// извлечь все записи
	public Cursor fetchAllClips() {
		String orderBy = "fixed DESC, _id DESC";
		Cursor mCursor = mDb.query(SQLITE_TABLE, DATABASE_FIELDS, 
			null, null, null, null, orderBy);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// извлечь одну записи
	public Cursor fetchIdClips(int id) {		
		Cursor mCursor = mDb.query(SQLITE_TABLE, DATABASE_FIELDS, 
			"_id = " + id, null, null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/*********************************************************************
	 * Database Helper
	 *********************************************************************/
	 
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_CLIPS_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
			onCreate(db);
		}
	}

}
