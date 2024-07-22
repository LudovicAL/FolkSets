package com.bandito.folksets.services;

import static com.bandito.folksets.util.Constants.PREVIOUS_AND_NEXT_TUNE;
import static com.bandito.folksets.util.Utilities.broadcastMessage;

import android.app.Activity;
import android.content.Context;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.util.Constants;

public class ExportImportDatabaseThread extends Thread {
    private final Activity callingActivity;
    private final Context context;
    private final String tag;
    private final Constants.ExportOrImport exportOrImport;

    public ExportImportDatabaseThread(Activity callingActivity, Context context, String tag, Constants.ExportOrImport exportOrImport){
        this.callingActivity = callingActivity;
        this.context = context;
        this.tag = tag;
        this.exportOrImport = exportOrImport;
    }

    @Override
    public void run() {
        try {
            if (exportOrImport == Constants.ExportOrImport.exportDatabase) {
                DatabaseManager.exportDatabase(context, callingActivity);
                broadcastMessage(context, Constants.BroadcastName.importExportUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.exportComplete}, new String[]{Constants.BroadcastKey.exportComplete.toString()});
            } else {
                DatabaseManager.importDatabase(context, callingActivity);
                broadcastMessage(context, Constants.BroadcastName.importExportUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.importComplete}, new String[]{Constants.BroadcastKey.importComplete.toString()});
            }
        } catch (Exception e) {
            ExceptionManager.manageException(callingActivity, context, tag, new FolkSetsException("An exception occured while executing the thread that exports or imports the database.", e));
        }
    }
}
