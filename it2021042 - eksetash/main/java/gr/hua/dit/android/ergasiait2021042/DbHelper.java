package gr.hua.dit.android.ergasiait2021042;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;



public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "POINTS_DB";

    public static final int DB_VERSION = 1;

    public static final String POINTS_TABLE = "POINTS";

    public static final String FIELD_1 = "sessionId";

    public static final String FIELD_2 = "type";

    public static final String FIELD_3 = "longitude";

    public static final String FIELD_4 = "lattitude";

    public DbHelper(@Nullable Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+POINTS_TABLE+" ("+FIELD_1+" integer, "+FIELD_2+" text, "+FIELD_3+" real, "+FIELD_4+" real);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public long insertPoint(ContentValues values){
        SQLiteDatabase database = this.getWritableDatabase();
        long result = database.insert(POINTS_TABLE, null, values);
        return result;
    }


//    public Cursor getPoints(){
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query(DbHelper.POINTS_TABLE,null,null,
//                null, null, null, null);
//        return cursor;
//    }



    public Cursor getPointsBySessionId(int sessionId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(POINTS_TABLE,null,"sessionId=?",new String[]{sessionId+""},null,null,null);
        return cursor;
    }

    public Cursor getCentersBySessionId(int sessionId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(POINTS_TABLE,null,"sessionId=? AND type=?",new String[]{sessionId+"","Center"},null,null,null);
        return cursor;
    }





}
