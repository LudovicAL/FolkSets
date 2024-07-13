package com.bandito.folksets.exception;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.bandito.folksets.util.IoUtilities;

public class ExceptionManager {

    private static final String TAG = ExceptionManager.class.getName();

    public static void manageException(Activity activity, Context context, String tag, Exception exception) {
        try {
            Log.e(TAG, "A non fatal exception occured.", exception);
            IoUtilities.writeExceptionToLogFile(activity, context, tag, exception);
            if (exception instanceof FolkSetsException) {
                FolkSetsException folkSetsException = (FolkSetsException) exception;
                if (!folkSetsException.fatal) {
                    try {
                        Toast.makeText(context, "An error occured", Toast.LENGTH_SHORT).show();
                    } catch (Exception e2) {
                        //Do nothing
                    }
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured while managing an exception.", e);
        }
        throw new RuntimeException("A fatal exception occured", exception);
    }
}
