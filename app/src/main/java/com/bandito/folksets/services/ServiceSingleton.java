package com.bandito.folksets.services;

import static com.bandito.folksets.util.Constants.STORAGE_DIRECTORY_URI;

import android.app.Activity;
import android.content.Context;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.Utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceSingleton {

    private static ServiceSingleton INSTANCE;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private UpdateDatabaseThread updateDatabaseThread;
    private PrepareTuneActivityDataThread prepareTuneActivityDataThread;
    private ExportImportDatabaseThread exportImportDatabaseThread;

    private ServiceSingleton() {
    }

    public static ServiceSingleton getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ServiceSingleton();
        }
        return INSTANCE;
    }

    public void UpdateDatabase(final Activity activity, final Context context) throws FolkSetsException {
        try {
            if (updateDatabaseThread == null || !updateDatabaseThread.isAlive()) {
                String storageDirectoryUri = Utilities.readStringFromSharedPreferences(activity, STORAGE_DIRECTORY_URI, null);
                if (storageDirectoryUri != null) {
                    updateDatabaseThread = new UpdateDatabaseThread(activity, context);
                    executorService.execute(updateDatabaseThread);
                }
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while starting the database update thread.", e);
        }
    }

    public void interruptDatabaseUpdate() throws FolkSetsException {
        try {
            if (updateDatabaseThread != null && updateDatabaseThread.isAlive()) {
                updateDatabaseThread.interrupt();
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while interrupting the database update thread.", e);
        }
    }

    public void prepareTuneActivityData(final Context context, final Activity activity, final TuneEntity tuneEntity, final SetEntity setEntity, final int position, final Constants.TuneOrSet tuneOrSet) throws FolkSetsException {
        try {
            if (prepareTuneActivityDataThread == null || !prepareTuneActivityDataThread.isAlive()) {
                prepareTuneActivityDataThread = new PrepareTuneActivityDataThread(context, activity, tuneEntity, setEntity, position, tuneOrSet);
                executorService.execute(prepareTuneActivityDataThread);

            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while starting the pdf rendering thread.", e);
        }
    }

    public void interruptTuneActivityDataRetrieval() throws FolkSetsException {
        try {
            if (prepareTuneActivityDataThread != null && prepareTuneActivityDataThread.isAlive()) {
                prepareTuneActivityDataThread.interrupt();
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while interrupting the pdf rendering thread.", e);
        }
    }

    public void exportOrImportDatabase(final Context context, final Activity activity, final String tag, final Constants.ExportOrImport exportOrImport) throws FolkSetsException {
        try {
            if (exportImportDatabaseThread == null || !exportImportDatabaseThread.isAlive()) {
                exportImportDatabaseThread = new ExportImportDatabaseThread(activity, context, tag, exportOrImport);
                executorService.execute(exportImportDatabaseThread);

            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while starting the exporting and importing thread.", e);
        }
    }

    public void interruptDatabaseExportingAndImporting() throws FolkSetsException {
        try {
            if (exportImportDatabaseThread != null && exportImportDatabaseThread.isAlive()) {
                exportImportDatabaseThread.interrupt();
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while interrupting the exporting and importing thread.", e);
        }
    }

    public boolean isExportingOrImportingThreadAlive() throws FolkSetsException {
        try {
            if (exportImportDatabaseThread != null) {
                return exportImportDatabaseThread.isAlive();
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while determing if the exporting and importing thread is alive.", e);
        }
    }
}
