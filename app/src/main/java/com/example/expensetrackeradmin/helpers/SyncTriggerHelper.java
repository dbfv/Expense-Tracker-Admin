package com.example.expensetrackeradmin.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public final class SyncTriggerHelper {

    private SyncTriggerHelper() {
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (capabilities == null) {
            return false;
        }

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
    }

    public static void attemptSyncIfOnline(Context context) {
        attemptSyncIfOnline(context, null);
    }

    public static void attemptSyncIfOnline(Context context, Runnable onComplete) {
        attemptSyncIfOnline(context, false, onComplete);
    }

    public static void attemptSyncIfOnline(Context context, boolean forceFullPush, Runnable onComplete) {
        if (!isNetworkAvailable(context)) {
            return;
        }

        SyncHelper syncHelper = new SyncHelper(context.getApplicationContext());
        syncHelper.syncAllPushThenPull(forceFullPush, onComplete);
    }
}
