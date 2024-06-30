package com.bandito.folksets.exception;

import android.util.Log;

import com.bandito.folksets.SetActivity;

public class ExceptionManager {

    private static final String TAG = ExceptionManager.class.getName();

    public static void manageException(Exception e) {
        if (e instanceof FolkSetsException) {
            FolkSetsException folkSetsException = (FolkSetsException) e;
            if (!folkSetsException.fatal) {
                Log.e(TAG, "A non fatal exception occured.", folkSetsException);
                return;
            }
        }
        throw new RuntimeException("A fatal exception occured", e);
    }
}
