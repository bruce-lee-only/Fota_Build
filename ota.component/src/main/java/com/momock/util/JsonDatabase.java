/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.momock.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public abstract class JsonDatabase {
	static final int IDX_ROW = 0;
	static final int IDX_ID = 1;
	static final int IDX_NAME = 2;
	static final int IDX_JSON = 3;

	public static class Document {
		Collection col;
		String id;
		JSONObject jo;

		Document(Collection col, String id, JSONObject jo) {
			this.col = col;
			this.id = id;
			this.jo = jo;
		}

		public String getId() {
			return id;
		}

		public JSONObject getData() {
			if (jo == null) {
				jo = col.get(id);
			}
			return jo;
		}
	}

	public static interface IFilter {
		boolean check(String id, JSONObject doc);
	}

	public static class Collection {
		MySQLiteOpenHelper helper;
		String table;
		String name;
		JsonDatabase jdb;
		boolean cachable = false;
		HashMap<String, Document> cachedDocs = null;
        Collection(JsonDatabase db, MySQLiteOpenHelper helper, String name, String table) {
			this.jdb = db;
			this.helper = helper;
			this.name = name;
			this.table = table;
		}

		public boolean clear() {
			SQLiteDatabase db = jdb.getNativeDatabase();
			try{
				if(null != db) {
					db.delete(table, "name=?", new String[]{name});
					if (cachable) {
						cachedDocs.clear();
					}
					return true;
				}
			} catch(Exception e) {
				Logger.error(e);
			}
			return false;
		}

		public JSONObject get(String id) {
			if (id == null) return null;
			if (cachable){
				Document doc = cachedDocs.get(id);
				return doc == null ? null : doc.getData();
			}
			JSONObject jo = null;
            //String sql = "select * from data where id=? and name=?;";
			String sql = "select * from " + table + " where id=? and name=?;";
			SQLiteDatabase db = jdb.getNativeDatabase();
			if (db == null) return null;
			Cursor cursor = null;
			try{
				cursor = db.rawQuery(sql, new String[] { id, name });
				if (cursor != null && cursor.getCount() == 1) {
					cursor.moveToNext();
					String json = cursor.getString(IDX_JSON - 1);
					jo = parse(json);
				}
			} catch(Exception e) {
				Logger.error(e);
			} finally {
				if (cursor != null){
					cursor.close();
					cursor = null;
				}
			}
			return jo;
		}

		public String set(String id, JSONObject jo) {
			SQLiteDatabase db = jdb.getNativeDatabase();
			if (db == null) return null;
			Cursor cursor = null;
			try{
				if (id == null)
					id = UUID.randomUUID().toString();
				if (jo == null) {
                    //db.delete("data", "id=? and name=?", new String[] { id, name });
					db.delete(table, "id=? and name=?", new String[] { id, name });
					if (cachable){
						cachedDocs.remove(id);
					}
				} else {
                    //String sql = "select * from data where id=? and name=?;";
					String sql = "select * from "  + table + " where id=? and name=?;";
					cursor = db.rawQuery(sql, new String[] { id, name });
					String json = jo.toString();
					boolean exists = cursor != null && cursor.getCount() == 1;
					Logger.debug("exists " + exists + ":" + cursor.getCount());
					if (exists) {
						ContentValues values = new ContentValues();
						values.put("json", json);
                        db.update(table, values, "id=? and name=?", new String[]{id, name });
					} else {
						ContentValues values = new ContentValues();
						values.put("id", id);
						values.put("name", name);
						values.put("json", json);
                        db.insert(table, "", values);
					}
					if (cachable){
						cachedDocs.put(id, new Document(this, id, jo));
					}
				}
			} catch(Exception e) {
				Logger.error(e);
			} finally {
				if (cursor != null){
					cursor.close();
					cursor = null;
				}
			}
			return id;
		}
		public int size() {
			return cachable ? cachedDocs.size() : list().size();
		}
		public List<Document> list() {
			return list(null, false, 0);
		}

		@Deprecated
		public List<Document> list(IFilter filter, boolean delayLoad, int max, String order) {
        	return list(filter, delayLoad, max);
		}

		public List<Document> list(IFilter filter, boolean delayLoad, int max) {
			List<Document> rows = new ArrayList<Document>();
			if (cachable){
				for(Document doc : cachedDocs.values()){
					String id = doc.getId();
					if (filter == null) {
						rows.add(doc);
					} else {
						if (filter.check(id, doc.getData())) {
							rows.add(doc);
						}
					}
					if (max > 0 && rows.size() >= max) break;
				}
				return rows;
			}
            //String sql = "select rowid, * from data where name=? ORDER BY rowid ASC";
			String sql = "select rowid, * from " + table + " where name=? ORDER BY rowid ASC;";
			SQLiteDatabase db = jdb.getNativeDatabase();
			if (db == null) return rows;
			Cursor cursor = null;
			try{
				cursor = db.rawQuery(sql, new String[] { name });
				if (cursor != null) {
					while (cursor.moveToNext()) {
						String id = cursor.getString(IDX_ID);
						if (filter == null) {
							rows.add(new Document(this, id, delayLoad ? null
									: parse(cursor.getString(IDX_JSON))));
						} else {
							String json = cursor.getString(IDX_JSON);
							JSONObject jo = parse(json);
							if (filter.check(id, jo)) {
								rows.add(new Document(this, id, delayLoad ? null : jo));
							}
						}
						if (max > 0 && rows.size() >= max) break;
					}
				}
			} catch(Exception e) {
				Logger.error(e);
			} finally {
				if (cursor != null){
					cursor.close();
					cursor = null;
				}
			}
			return rows;
		}

		public boolean isCachable() {
			return cachable;
		}

		public void setCachable(boolean cachable) {
			if (this.cachable != cachable){
				if (cachable) {
					cachedDocs = new HashMap<String, Document>();
					List<Document> docs = list();
					for(Document doc : docs){
						cachedDocs.put(doc.getId(), doc);
					}
				}
				this.cachable = cachable;
			}			
		}
	}

	private static class MySQLiteOpenHelper extends SQLiteOpenHelper {

    	public static final String TABLE_DATA = "data";
    	public static final String TABLE_CACHE = "cache";

		public MySQLiteOpenHelper(Context context, String name) {
			super(context, name, null, 1);
		}

		private void initPayloadTable(SQLiteDatabase db, String table) {
			String data = "create table " + table + "(id text,name text,json text, primary key(name, id));";
			db.execSQL(data);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Logger.debug("Create Json Database");
			initPayloadTable(db, TABLE_DATA);
			initPayloadTable(db, TABLE_CACHE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

	}
		
	abstract SQLiteDatabase getNativeDatabase();
	public abstract Collection getCacheCollection(String name);
    public abstract Collection getCollection(String name);
	public abstract void forceClose();

	public static JsonDatabase get(Context context) {
		return get(context, null);
	}

	public static JsonDatabase get(final Context context, final String dbname) {
		return new JsonDatabase() {
			Map<String, Collection> cols = new HashMap<String, Collection>();
			SQLiteDatabase db = null;
			MySQLiteOpenHelper helper = new MySQLiteOpenHelper(context, dbname);

			private Collection getCollection(String name, String table) {
				String key = name + "-" + table;
				if (!cols.containsKey(key)) {
					cols.put(key, new Collection(this, helper, name, table));
				}
				return cols.get(key);
			}

			@Override
			public Collection getCacheCollection(String name) {
				return getCollection(name, MySQLiteOpenHelper.TABLE_CACHE);
			}

			@Override
            public Collection getCollection(String name) {
				return getCollection(name, MySQLiteOpenHelper.TABLE_DATA);
			}

			@Override
			public void forceClose() {
				if (db != null){
					db.close();
					db = null;
				}
			}

			@Override
			SQLiteDatabase getNativeDatabase() {
				if (db == null || !db.isOpen()) {
					try{
						db = helper.getWritableDatabase();
					}catch(Exception e){
						Logger.error(e);
						Logger.debug("Database Path :" + context.getDatabasePath(dbname).getPath());
						try{
							helper = new MySQLiteOpenHelper(context, null);
							db = helper.getWritableDatabase();
						}catch(Exception ex){
							Logger.error(ex);
							db = null;
						}
					}
				}
				return db;
			}

		};
	}

	static JSONObject parse(String json) {
		JSONTokener tokener = new JSONTokener(json);
		Object root;
		try {
			root = tokener.nextValue();
		} catch (JSONException e) {
			Logger.error(e);
			return null;
		}
		return (JSONObject) root;
	}
}
