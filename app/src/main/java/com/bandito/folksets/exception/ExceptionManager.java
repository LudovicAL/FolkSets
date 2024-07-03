package com.bandito.folksets.exception;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ExceptionManager {

    private static final String TAG = ExceptionManager.class.getName();

    public static void manageException(Context context, Exception e) {
        if (e instanceof FolkSetsException) {
            FolkSetsException folkSetsException = (FolkSetsException) e;
            if (!folkSetsException.fatal) {
                try {
                    Log.e(TAG, "A non fatal exception occured.", folkSetsException);
                    Toast.makeText(context, "An error occured", Toast.LENGTH_SHORT).show();
                } catch (Exception e2) {
                    //Do nothing
                }
                return;
            }
        }
        throw new RuntimeException("A fatal exception occured", e);
    }
}
