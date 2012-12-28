/*
 * Copyright (C) 2011 readyState Software Ltd, 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tadevelop.sdk.database.sqlite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tadevelop.sdk.BuildConfig;

/**
 * A helper class to manage database creation and version management using an
 * application's raw asset files.
 * 
 * This class provides developers with a simple way to ship their Android app
 * with an existing SQLite database (which may be pre-populated with data) and
 * to manage its initial creation and any upgrades required with subsequent
 * version releases.
 * 
 * <p>
 * This class makes it easy for {@link android.content.ContentProvider}
 * implementations to defer opening and upgrading the database until first use,
 * to avoid blocking application startup with long-running database upgrades.
 * 
 * <p>
 * For examples see <a
 * href="https://github.com/jgilfelt/android-sqlite-asset-helper">
 * https://github.com/jgilfelt/android-sqlite-asset-helper</a>
 * 
 * <p class="note">
 * <strong>Note:</strong> this class assumes monotonically increasing version
 * numbers for upgrades. Also, there is no concept of a database downgrade;
 * installing a new version of your app which uses a lower version number than a
 * previously-installed version will result in undefined behavior.
 * </p>
 */
public class SQLiteAssetHelper extends SQLiteOpenHelper {

	private static final String TAG = SQLiteAssetHelper.class.getSimpleName();
	private static final String ASSET_DB_PATH = "databases";
	public static final int FROM_ASSET_SQLSCRIPT = 1;
	public static final int FROM_ASSET_FILE = 2;

	private final Context mContext;
	private final String mName;
	private final CursorFactory mFactory;
	private final int mNewVersion;

	private SQLiteDatabase mDatabase = null;
	private static boolean mIsInitializing = false;
	private static Object initializeLockObject = new Object();

	private String mDatabasePath;
	private String mArchivePath;
	private String mUpgradePathFormat;
	private String mCteatePathFormat;

	private int mForcedUpgradeVersion = 0;
	private int mDatabaseCreateFrom = 0;

	/**
	 * Create a helper object to create, open, and/or manage a database in a
	 * specified location. This method always returns very quickly. The database
	 * is not actually created or opened until one of
	 * {@link #getWritableDatabase} or {@link #getReadableDatabase} is called.
	 * 
	 * @param context to use to open or create the database
	 * @param name of the database file
	 * @param storageDirectory to store the database file upon creation; caller
	 *            must ensure that the specified absolute path is available and
	 *            can be written to
	 * @param factory to use for creating cursor objects, or null for the
	 *            default
	 * @param version number of the database (starting at 1); if the database is
	 *            older, SQL file(s) contained within the application assets
	 *            folder will be used to upgrade the database
	 */
	public SQLiteAssetHelper(Context context, String name, String storageDirectory, CursorFactory factory, int version) {
		super(context, name, factory, version);

		if (version < 1)
			throw new IllegalArgumentException("Version must be >= 1, was " + version);
		if (name == null)
			throw new IllegalArgumentException("Databse name cannot be null");

		mContext = context;
		mName = name;
		mFactory = factory;
		mNewVersion = version;

		mArchivePath = ASSET_DB_PATH + "/" + name + ".zip";
		if (storageDirectory != null) {
			mDatabasePath = storageDirectory;
		} else {
			mDatabasePath = context.getApplicationInfo().dataDir + "/databases";
		}
		mUpgradePathFormat = ASSET_DB_PATH + "/" + name + "_Upgrade_%s-%s.sql";
		mCteatePathFormat = ASSET_DB_PATH + "/" + name + "_Create_%s.sql";
	}

	/**
	 * Create a helper object to create, open, and/or manage a database in the
	 * application's default private data directory. This method always returns
	 * very quickly. The database is not actually created or opened until one of
	 * {@link #getWritableDatabase} or {@link #getReadableDatabase} is called.
	 * 
	 * @param context to use to open or create the database
	 * @param name of the database file
	 * @param factory to use for creating cursor objects, or null for the
	 *            default
	 * @param version number of the database (starting at 1); if the database is
	 *            older, SQL file(s) contained within the application assets
	 *            folder will be used to upgrade the database
	 */
	public SQLiteAssetHelper(Context context, String name, CursorFactory factory, int version) {
		this(context, name, null, factory, version);
	}

	/**
	 * Create and/or open a database that will be used for reading and writing.
	 * The first time this is called, the database will be extracted and copied
	 * from the application's assets folder.
	 * 
	 * <p>
	 * Once opened successfully, the database is cached, so you can call this
	 * method every time you need to write to the database. (Make sure to call
	 * {@link #close} when you no longer need the database.) Errors such as bad
	 * permissions or a full disk may cause this method to fail, but future
	 * attempts may succeed if the problem is fixed.
	 * </p>
	 * 
	 * <p class="caution">
	 * Database upgrade may take a long time, you should not call this method
	 * from the application main thread, including from
	 * {@link android.content.ContentProvider#onCreate
	 * ContentProvider.onCreate()}.
	 * 
	 * @throws SQLiteException if the database cannot be opened for writing
	 * @return a read/write database object valid until {@link #close} is called
	 */
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		synchronized (initializeLockObject) {
			if (mIsInitializing) {
				try {
					initializeLockObject.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
			return mDatabase; // The database is already open for business
		}

		// If we have a read-only database open, someone could be using it
		// (though they shouldn't), which would cause a lock to be held on
		// the file, and our attempts to open the database read-write would
		// fail waiting for the file lock. To prevent that, we acquire the
		// lock on the read-only database, which shuts out other users.

		boolean success = false;
		SQLiteDatabase db = null;
		synchronized (initializeLockObject) {
			try {
				mIsInitializing = true;
				// if (mName == null) {
				// db = SQLiteDatabase.create(null);
				// } else {
				// db = mContext.openOrCreateDatabase(mName, 0, mFactory);
				// }
				db = createOrOpenDatabase(false);

				int version = db.getVersion();

				// do force upgrade
				if (version != 0 && version < mForcedUpgradeVersion) {
					db = createOrOpenDatabase(true);
					db.setVersion(mNewVersion);
					version = db.getVersion();
				}

				if (version != mNewVersion) {
					db.beginTransaction();
					try {
						if (version == 0) {
							onCreate(db);
						} else {
							if (version > mNewVersion) {
								Log.w(TAG, "Can't downgrade read-only database from version " + version + " to "
										+ mNewVersion + ": " + db.getPath());
							}
							onUpgrade(db, version, mNewVersion);
						}
						db.setVersion(mNewVersion);
						db.setTransactionSuccessful();
					} finally {
						db.endTransaction();
					}
				}

				onOpen(db);
				success = true;
				return db;
			} finally {
				mIsInitializing = false;
				if (success) {
					if (mDatabase != null) {
						try {
							mDatabase.close();
						} catch (Exception e) {
						}
						// mDatabase.unlock();
					}
					mDatabase = db;
				} else {
					// if (mDatabase != null) mDatabase.unlock();
					if (db != null)
						db.close();
				}
				initializeLockObject.notifyAll();
			}
		}
	}

	/**
	 * Create and/or open a database. This will be the same object returned by
	 * {@link #getWritableDatabase} unless some problem, such as a full disk,
	 * requires the database to be opened read-only. In that case, a read-only
	 * database object will be returned. If the problem is fixed, a future call
	 * to {@link #getWritableDatabase} may succeed, in which case the read-only
	 * database object will be closed and the read/write object will be returned
	 * in the future.
	 * 
	 * <p class="caution">
	 * Like {@link #getWritableDatabase}, this method may take a long time to
	 * return, so you should not call it from the application main thread,
	 * including from {@link android.content.ContentProvider#onCreate
	 * ContentProvider.onCreate()}.
	 * 
	 * @throws SQLiteException if the database cannot be opened
	 * @return a database object valid until {@link #getWritableDatabase} or
	 *         {@link #close} is called.
	 */
	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		synchronized (initializeLockObject) {
			if (mIsInitializing) {
				try {
					initializeLockObject.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (mDatabase != null && mDatabase.isOpen()) {
			return mDatabase; // The database is already open for business
		}

		try {
			return getWritableDatabase();
		} catch (SQLiteException e) {
			if (mName == null)
				throw e; // Can't open a temp database read-only!
			Log.e(TAG, "Couldn't open " + mName + " for writing (will try read-only):", e);
		}

		SQLiteDatabase db = null;
		synchronized (initializeLockObject) {
			try {
				mIsInitializing = true;
				String path = mContext.getDatabasePath(mName).getPath();
				db = SQLiteDatabase.openDatabase(path, mFactory, SQLiteDatabase.OPEN_READONLY);
				if (db.getVersion() != mNewVersion) {
					throw new SQLiteException("Can't upgrade read-only database from version " + db.getVersion()
							+ " to " + mNewVersion + ": " + path);
				}

				onOpen(db);
				Log.w(TAG, "Opened " + mName + " in read-only mode");
				mDatabase = db;
				return mDatabase;
			} finally {
				mIsInitializing = false;
				if (db != null && db != mDatabase)
					db.close();
				initializeLockObject.notifyAll();
			}
		}
	}

	/**
	 * Close any open database object.
	 */
	@Override
	public synchronized void close() {
		synchronized (initializeLockObject) {
			if (mIsInitializing) {
				try {
					initializeLockObject.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (mDatabase != null && mDatabase.isOpen()) {
			mDatabase.close();
			mDatabase = null;
		}
	}

	@Override
	public final void onCreate(SQLiteDatabase db) {
		// do nothing - createOrOpenDatabase() is called in
		// getWritableDatabase() to handle database creation.
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		synchronized (initializeLockObject) {
			mIsInitializing = true;
			try {
				Log.w(TAG, "Upgrading database " + mName + " from version " + oldVersion + " to " + newVersion + "...");

				ArrayList<String> paths = new ArrayList<String>();
				getUpgradeFilePaths(oldVersion, newVersion - 1, newVersion, paths);

				if (paths.isEmpty()) {
					Log.e(TAG, "no upgrade script path from " + oldVersion + " to " + newVersion);
					throw new SQLiteAssetException("no upgrade script path from " + oldVersion + " to " + newVersion);
				}

				Collections.sort(paths);
				for (String path : paths) {
					try {
						Log.w(TAG, "processing upgrade: " + path);
						InputStream is = mContext.getAssets().open(path);
						String sql = convertStreamToString(is);
						exeSQL(sql, db);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Log.w(TAG, "Successfully upgraded database " + mName + " from version " + oldVersion + " to "
						+ newVersion);
			} finally {
				mIsInitializing = false;
				initializeLockObject.notifyAll();
			}
		}
	}

	public void setForcedUpgradeVersion(int version) {
		mForcedUpgradeVersion = version;
	}

	public void setDatabaseCreateFrom(int mode) {
		mDatabaseCreateFrom = mode;
	}

	private SQLiteDatabase createOrOpenDatabase(boolean force) throws SQLiteAssetException {
		SQLiteDatabase db = returnDatabase();
		if (db != null) {
			// database already exists
			if (force) {
				Log.w(TAG, "forcing database upgrade!");
				createDatebaseFromAssets(mDatabaseCreateFrom);
				db = returnDatabase();
			}
			return db;
		} else {
			// database does not exist, copy it from assets and return it
			createDatebaseFromAssets(mDatabaseCreateFrom);
			db = returnDatabase();
			return db;
		}
	}

	private SQLiteDatabase returnDatabase() {
		try {
			if (BuildConfig.DEBUG) {
				Log.i(TAG, "SQLiteAssetHelper.returnDatabase() mDatabasePath:" + mDatabasePath);
			}
			SQLiteDatabase db = SQLiteDatabase.openDatabase(mDatabasePath + "/" + mName, mFactory, SQLiteDatabase.OPEN_READWRITE);
			Log.i(TAG, "successfully opened database " + mName);
			return db;
		} catch (SQLiteException e) {
			Log.w(TAG, "could not open database " + mName + " - " + e.getMessage());
			return null;
		}
	}

	/**
	 * 使用Assets提供的资源创建数据库, 该方法优先使用Assets的SQL脚本创建数据库,
	 * 如果使用SQL脚本创建数据库则会尝试直接复制Assets的数据文件, 如果二者全部失败,抛出异常
	 * 
	 * @return
	 */
	private void createDatebaseFromAssets() {
		try {
			exeCreateScriptFromAssets();
		} catch (SQLiteAssetException e) {
			copyDatabaseFromAssets();
		}

	}

	/**
	 * 使用指定的资源数据库文件
	 * 
	 * @param from 0 使用SQL脚本,1 使用Asset中的数据库文件
	 */
	private void createDatebaseFromAssets(int from) {
		switch (from) {
		case 1:
			exeCreateScriptFromAssets();
			break;
		case 2:
			copyDatabaseFromAssets();
			break;
		default:
			createDatebaseFromAssets();
			break;
		}
	}

	/**
	 * 使用Assets提供的最新版本的数据库创建脚本创建数据库
	 * 
	 * @throws SQLiteAssetException 如果数据库创建脚本不存在或者执行创建脚本时遇到错误
	 */
	private void exeCreateScriptFromAssets() throws SQLiteAssetException {
		Log.i(TAG, "SQLiteAssetHelper.createDatebaseFromAssets()");
		try {
			int version = findLatestVersionCreateSQLFile();
			InputStream is = getCreateSQLStream(version);
			String sql = convertStreamToString(is);
			File f = new File(mDatabasePath + "/");
			if (!f.exists()) {
				f.mkdirs();
			}
			File dbFile = new File(f, mName);
			if (!f.exists()) { // 文件不存在返回false
				try {
					f.createNewFile();// 创建文件
				} catch (IOException e) {
					Log.i(TAG, "SQLiteAssetHelper.exeCreateScriptFromAssets() Failed to create database files");
				}
			}
			if (sql != null) {
				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, mFactory);
				exeSQL(sql, db);
			}
		} catch (FileNotFoundException e) {
			throw new SQLiteAssetException("Did not find any database creation script");
		}
	}

	/**
	 * 从Assets复制数据库文件
	 * 
	 * @throws SQLiteAssetException
	 */
	private void copyDatabaseFromAssets() throws SQLiteAssetException {
		Log.w(TAG, "copying database from assets...");

		try {
			InputStream zipFileStream = mContext.getAssets().open(mArchivePath);
			File f = new File(mDatabasePath + "/");
			if (!f.exists()) {
				f.mkdirs();
			}

			ZipInputStream zis = getFileFromZip(zipFileStream);
			if (zis == null) {
				throw new SQLiteAssetException("Archive is missing a SQLite database file");
			}
			writeExtractedFileToDisk(zis, new FileOutputStream(mDatabasePath + "/" + mName));

			Log.w(TAG, "database copy complete");

		} catch (FileNotFoundException fe) {
			SQLiteAssetException se = new SQLiteAssetException("Missing " + mArchivePath + " file in assets or target folder not writable");
			se.setStackTrace(fe.getStackTrace());
			throw se;
		} catch (IOException e) {
			SQLiteAssetException se = new SQLiteAssetException("Unable to extract " + mArchivePath + " to data directory");
			se.setStackTrace(e.getStackTrace());
			throw se;
		}
	}

	private InputStream getUpgradeSQLStream(int oldVersion, int newVersion) {
		String path = String.format(mUpgradePathFormat, oldVersion, newVersion);
		try {
			InputStream is = mContext.getAssets().open(path);
			return is;
		} catch (IOException e) {
			Log.w(TAG, "missing database upgrade script: " + path);
			return null;
		}
	}

	/**
	 * 返回给定版本号的数据脚本输入流
	 * 
	 * @param version 大于等于0的版本号
	 * @return 该版本的数据库创建脚本输入流,或者Null,如果该版本的数据创建脚本不存在
	 */
	private InputStream getCreateSQLStream(int version) {
		String path = String.format(mCteatePathFormat, version);
		try {
			InputStream is = mContext.getAssets().open(path);
			return is;
		} catch (IOException e) {
			Log.w(TAG, "missing database create script: " + path);
			return null;
		}
	}

	/**
	 * 查找最新版本的数据库创建脚本
	 * 
	 * @return 最新的数据库版本号
	 * @throws FileNotFoundException 没有找到任何数据库创建脚本
	 */
	private int findLatestVersionCreateSQLFile() throws FileNotFoundException {
		int i = 1;
		InputStream is = getCreateSQLStream(i);
		while (is != null) {
			is = getCreateSQLStream(++i);
		}
		if (--i < 1) {
			throw new FileNotFoundException();
		}
		return i;
	}

	private void getUpgradeFilePaths(int baseVersion, int start, int end, ArrayList<String> paths) {

		int a;
		int b;

		InputStream is = getUpgradeSQLStream(start, end);
		if (is != null) {
			String path = String.format(mUpgradePathFormat, start, end);
			paths.add(path);
			// Log.d(TAG, "found script: " + path);
			a = start - 1;
			b = start;
			is = null;
		} else {
			a = start - 1;
			b = end;
		}

		if (a < baseVersion) {
			return;
		} else {
			getUpgradeFilePaths(baseVersion, a, b, paths); // recursive call
		}

	}

	private void writeExtractedFileToDisk(ZipInputStream zin, OutputStream outs) throws IOException {
		byte[] buffer = new byte[1024];
		int length;
		while ((length = zin.read(buffer)) > 0) {
			outs.write(buffer, 0, length);
		}
		outs.flush();
		outs.close();
		zin.close();
	}

	private ZipInputStream getFileFromZip(InputStream zipFileStream) throws FileNotFoundException, IOException {
		ZipInputStream zis = new ZipInputStream(zipFileStream);
		ZipEntry ze = null;
		while ((ze = zis.getNextEntry()) != null) {
			Log.w(TAG, "extracting file: '" + ze.getName() + "'...");
			return zis;
		}
		return null;
	}

	private String convertStreamToString(InputStream is) {
		return new Scanner(is).useDelimiter("\\A").next();
	}

	private void exeSQL(String sql, SQLiteDatabase db) {
		// 处理SQL语句
		List<String> sqlList =  preExeSQL(sql);
		//
		try {
			for (String cmd : sqlList) {
				Log.d(TAG, "cmd=" + cmd);
				if (cmd.trim().length() > 0) {
					try {
						db.execSQL(cmd);
					} catch (SQLException e) {
						SQLiteAssetException se = new SQLiteAssetException("The execution SQL statement error at: "
								+ cmd);
						throw se;
					}
				}
			}
		} catch (SQLiteAssetException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	private static List<String> preExeSQL(String sql) {
		List<String> sqlList = new LinkedList<String>();
		String[] temp = sql.split(";");
		String t = null;
		for (int i = 0; i < temp.length; i++) {
			if (t == null) {
				t = temp[i];
			} else {
				t = t + ";" + temp[i];
			}
			System.out.println(t);
			if (t.matches("[\\s\\S]*[\\W]+BEGIN[\\W]+[\\s\\S]*")) {
				if (t.matches("[\\s\\S]*[\\W]+END[\\W]*[\\s\\S]*")) {
					sqlList.add(t.trim());
					t = null;
				}
			} else {
				sqlList.add(t.trim());
				t = null;
			}
		}
		return sqlList;
	}
}
