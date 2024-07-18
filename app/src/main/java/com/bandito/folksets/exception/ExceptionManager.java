package com.bandito.folksets.exception;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.bandito.folksets.util.IoUtilities;
import com.bandito.folksets.util.Utilities;

public class ExceptionManager {

    private static final String TAG = ExceptionManager.class.getName();

    public static void manageException(Activity activity, Context context, String tag, Exception exception) {
        try {
            Log.e(TAG, "A non fatal exception occured.", exception);
            Toast.makeText(context, "An error occured", Toast.LENGTH_SHORT).show();
            try {
                Uri uri = IoUtilities.getLogFileUri(activity, context);
                if (uri != null) {
                    IoUtilities.appendTextToFile(context, tag, uri, Utilities.convertExceptionToString(exception));
                }
            } catch (Exception e2) {
                Log.e(tag, "An exception occured while appending a text file during an exception management.", e2);
            }
            if (exception instanceof FolkSetsException) {
                FolkSetsException folkSetsException = (FolkSetsException) exception;
                if (!folkSetsException.fatal) {
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured while managing an exception.", e);
        }
        throw new RuntimeException("A fatal exception occured", exception);
    }
}
