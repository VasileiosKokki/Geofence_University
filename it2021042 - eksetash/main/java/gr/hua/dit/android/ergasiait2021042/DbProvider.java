package gr.hua.dit.android.ergasiait2021042;

import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_1;
import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_2;
import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_3;
import static gr.hua.dit.android.ergasiait2021042.DbHelper.FIELD_4;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DbProvider extends ContentProvider {
    private UriMatcher uriMatcher;
    public static String AUTHORITY;

    private static DbHelper dbHelper;  // Singleton instance of DbHelper



    @Override
    public boolean onCreate() {
        AUTHORITY = getContext().getPackageName();
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,"points/#",1);
        uriMatcher.addURI(AUTHORITY,"points/centers/#",2);
        uriMatcher.addURI(AUTHORITY,"points/add",3);

        if (dbHelper == null) {
            dbHelper = new DbHelper(getContext());
        }
        // exoume memory leaks an den sigourepsoume oti mono ena dbhelper einai anoikto kathe fora (mas to deiksate lathos)

        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
//        DbHelper dbHelper = new DbHelper(getContext());
        Cursor cursor = null;
        String sId = uri.getLastPathSegment();
        int sessionId = Integer.parseInt(sId);
        switch(uriMatcher.match(uri)){
            case 1:
                cursor = dbHelper.getPointsBySessionId(sessionId);
                break;
            case 2:
                cursor = dbHelper.getCentersBySessionId(sessionId);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
//        DbHelper dbHelper = new DbHelper(getContext());
        switch(uriMatcher.match(uri)) {
            case 3:
                Integer sessionId = contentValues.getAsInteger(FIELD_1);
                String type = contentValues.getAsString(FIELD_2);
                Double longitude = contentValues.getAsDouble(FIELD_3);
                Double latitude = contentValues.getAsDouble(FIELD_4);


                if (sessionId != null && type != null && longitude != null && latitude != null) {
                    // Now you have the first name and last name, you can add the user
                    dbHelper.insertPoint(contentValues);
                } else {
                    // Handle the case where the values are missing or null
                }
                break;
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

}
