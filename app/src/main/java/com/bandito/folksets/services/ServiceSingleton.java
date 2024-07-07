package com.bandito.folksets.services;

import static com.bandito.folksets.util.Constants.STORAGE_DIRECTORY_URI;

import android.app.Activity;
import android.content.Context;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceSingleton {

    private static ServiceSingleton INSTANCE;
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private UpdateDatabaseThread updateDatabaseThread;
    private RenderPdfThread renderPdfThread;

    private ServiceSingleton() {
    }

    public static ServiceSingleton getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ServiceSingleton();
        }
        return INSTANCE;
    }

    public void UpdateDatabase(Activity activity, Context context) throws FolkSetsException {
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
            if (updateDatabaseThread != null) {
                updateDatabaseThread.interrupt();
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while interrupting the database update thread.", e);
        }
    }

    public void renderPdf(Context context, TuneEntity tuneEntity) throws FolkSetsException {
        try {
            if (renderPdfThread == null || !renderPdfThread.isAlive()) {
                renderPdfThread = new RenderPdfThread(context, tuneEntity);
                executorService.execute(renderPdfThread);

            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while starting the pdf rendering thread.", e);
        }
    }

    public void interruptPdfRendering() throws FolkSetsException {
        try {
            if (renderPdfThread != null) {
                renderPdfThread.interrupt();
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while interrupting the pdf rendering thread.", e);
        }
    }
}
