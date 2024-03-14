package gr.hua.dit.android.ergasiait2021042;

import android.content.Context;
import android.content.SharedPreferences;
public class SessionManager {
    private static final String PREF_NAME = "SessionPrefs";
    private static final String KEY_SESSION_ID = "sessionId";
    private static boolean sessionExpired = true;

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void startSession(Context context) {
        // Increment the session ID for each new session
        int currentSessionId = getCurrentSessionId(context) + 1;
        setCurrentSessionId(context, currentSessionId);

        // Reset the sessionExpired flag when starting a new session
        sessionExpired = false;
    }

    public static int getCurrentSessionId(Context context) {
        return getSharedPreferences(context).getInt(KEY_SESSION_ID, 0);
    }

    private static void setCurrentSessionId(Context context, int sessionId) {
        getSharedPreferences(context).edit().putInt(KEY_SESSION_ID, sessionId).apply();
    }

    public static void stopSession() {
        // Perform any cleanup or finalization tasks related to stopping the session

        // Set the sessionExpired flag to true to indicate that the session has been stopped
        sessionExpired = true;

        // Optionally, perform any other actions when stopping a session
    }

    public static boolean isSessionExpired() {
        return sessionExpired;
    }


    // You can add more methods related to session management if needed
}
