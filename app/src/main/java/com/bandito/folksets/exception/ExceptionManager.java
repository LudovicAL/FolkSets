package com.bandito.folksets.exception;

import android.util.Log;

public class ExceptionManager {
    public static void manageException(Exception e) {
        if (e instanceof FolkSetsException) {
            FolkSetsException folkSetsException = (FolkSetsException) e;
            if (!folkSetsException.fatal) {
                Log.e("EXCEPTION", "A non fatal exception occured.", folkSetsException);
                return;
            }
        }
        throw new RuntimeException("A fatal exception occured", e);
    }
}
