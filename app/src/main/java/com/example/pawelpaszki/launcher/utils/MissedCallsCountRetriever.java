package com.example.pawelpaszki.launcher.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

public class MissedCallsCountRetriever {

    public static int getMissedCallsCount(Context context) {
        String[] projection = { CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE };
        String where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + "=1" ;
        Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,where, null, null);
        c.moveToFirst();
        return c.getCount();
    }

    public static int getUnreadMessagesCount(Context context) {
        final Uri SMS_INBOX = Uri.parse("content://sms/inbox");

        Cursor c = context.getContentResolver().query(SMS_INBOX, null, "read = 0", null, null);
        int unreadMessagesCount = c.getCount();
        c.close();
        return unreadMessagesCount;
    }
}