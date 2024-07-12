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
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private UpdateDatabaseThread updateDatabaseThread;
    private RenderPdfAndGetPreviousAndNextTuneThread renderPdfAndGetPreviousAndNextTuneThread;

    private ServiceSingleton() {
    }

    public static ServiceSingleton getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ServiceSingleton();
        }
        return INSTANCE;
    }

    public void UpdateDatabase(final Activity activity, final Context context, final String tag) throws FolkSetsException {
        try {
            if (updateDatabaseThread == null || !updateDatabaseThread.isAlive()) {
                String storageDirectoryUri = Utilities.readStringFromSharedPreferences(activity, STORAGE_DIRECTORY_URI, null);
                if (storageDirectoryUri != null) {
                    updateDatabaseThread = new UpdateDatabaseThread(activity, context, tag);
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

    public void renderPdfAndGetPreviousAndNextTune(Context context, TuneEntity tuneEntity, SetEntity setEntity, int position, Constants.TuneOrSet tuneOrSet) throws FolkSetsException {
        try {
            if (renderPdfAndGetPreviousAndNextTuneThread == null || !renderPdfAndGetPreviousAndNextTuneThread.isAlive()) {
                renderPdfAndGetPreviousAndNextTuneThread = new RenderPdfAndGetPreviousAndNextTuneThread(context, tuneEntity, setEntity, position, tuneOrSet);
                executorService.execute(renderPdfAndGetPreviousAndNextTuneThread);

            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while starting the pdf rendering thread.", e);
        }
    }

    public void interruptPdfRendering() throws FolkSetsException {
        try {
            if (renderPdfAndGetPreviousAndNextTuneThread != null && renderPdfAndGetPreviousAndNextTuneThread.isAlive()) {
                renderPdfAndGetPreviousAndNextTuneThread.interrupt();
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while interrupting the pdf rendering thread.", e);
        }
    }
}
