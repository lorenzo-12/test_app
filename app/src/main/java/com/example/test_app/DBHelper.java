package com.example.test_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public Context context;
    public static final String DATABASE_NAME = "mydatabase_testapp";
    public static final int DATABASE_VERSION = 1;

    public static final String STEPCOUNTER_TABLE = "stepcounter_table";
    public static final String DAY_STEPCOUNTER = "day";
    public static final String STEPS_STEPCOUNTER = "steps";

    public static final String CREATE_STEPCOUNTER_TABLE = "CREATE TABLE "+STEPCOUNTER_TABLE+" (  "  +DAY_STEPCOUNTER+" TEXT PRIMARY KEY, "
                                                                                                    +STEPS_STEPCOUNTER+" INTEGER NOT NULL);";

    public DBHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STEPCOUNTER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ STEPCOUNTER_TABLE);
    }

    public Boolean addSteps(String day, int steps){
        Boolean exist =findSteps(day);
        if (exist == true) return modifySteps(day,steps);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DAY_STEPCOUNTER,day.toLowerCase());
        cv.put(STEPS_STEPCOUNTER,steps);

        long result = db.insert(STEPCOUNTER_TABLE,null,cv);
        if (result==-1) return false;
        return true;
    }

    public Boolean findSteps(String day){
        String query = "SELECT * FROM "+STEPCOUNTER_TABLE+" WHERE day=?";
        SQLiteDatabase db = this.getReadableDatabase();
        if (db == null) return false;
        Cursor cursor = db.rawQuery(query,new String[]{day});
        if (cursor.getCount() == 0) return false;
        return true;
    }

    public Boolean modifySteps(String day, int steps){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(DAY_STEPCOUNTER,day);
        cv.put(STEPS_STEPCOUNTER,steps);

        Cursor cursor = db.rawQuery("SELECT * FROM "+STEPCOUNTER_TABLE+" WHERE day=?", new String[]{day});
        if (cursor.getCount() > 0){
            long result = db.update(STEPCOUNTER_TABLE,cv,"day=?", new String[]{day});
            if (result == -1) return false;
            return true;
        }
        else return false;
    }

    public Cursor readAllSteps(){
        String query = "SELECT * FROM "+STEPCOUNTER_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if( db != null){
            cursor = db.rawQuery(query,null);
        }
        return cursor;
    }

    public String viewSteps(){
        Cursor cursor = readAllSteps();
        if(cursor.getCount() == 0) return "No Data";
        String res = "";
        while (cursor.moveToNext()){
            res += cursor.getString(0)+" "+cursor.getString(1)+"\n";
        }
        return res;
    }

    public Boolean deleteAllSteps(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + STEPCOUNTER_TABLE, new String[]{});
        if (cursor.getCount() > 0) {
            long result = db.delete(STEPCOUNTER_TABLE,"", new String[]{});
            if (result == -1) return false;
            return true;
        }
        else {
            return false;
        }
    }


}
