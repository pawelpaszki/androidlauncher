package com.pawelpaszki.launcher.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

public class MissedCallsCountRetriever {

    public static int getMissedCallsCount(Context context) {
        String[] projection = { CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE };
        String where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + "=1" ;
        Cursor c = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,where, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        int missedCallsCount = 0;
        if (c != null) {
            missedCallsCount = c.getCount();
        }
        if (c != null) {
            c.close();
        }
        return missedCallsCount;
    }

    public static int getUnreadMessagesCount(Context context) {
        final Uri SMS_INBOX = Uri.parse("content://sms/inbox");

        Cursor c = context.getContentResolver().query(SMS_INBOX, null, "read = 0", null, null);
        int unreadMessagesCount = 0;
        if (c != null) {
            unreadMessagesCount = c.getCount();
        }
        if (c != null) {
            c.close();
        }
        return unreadMessagesCount;
    }
}