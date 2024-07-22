package com.bandito.folksets.services;

import static com.bandito.folksets.util.Constants.SET_ENTITY_LIST;
import static com.bandito.folksets.util.Constants.SET_NAME;
import static com.bandito.folksets.util.Constants.STORAGE_DIRECTORY_URI;
import static com.bandito.folksets.util.Constants.TUNE_ENTITY_LIST;
import static com.bandito.folksets.util.Constants.TUNE_FILE_PATH;
import static com.bandito.folksets.util.Constants.TUNE_ID;
import static com.bandito.folksets.util.Constants.TUNE_TITLES;
import static com.bandito.folksets.util.Constants.UNIQUE_VALUES;
import static com.bandito.folksets.util.Utilities.broadcastMessage;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.documentfile.provider.DocumentFile;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.IoUtilities;
import com.bandito.folksets.util.StaticData;
import com.bandito.folksets.util.Utilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateDatabaseThread extends Thread {
    private static final String TAG = UpdateDatabaseThread.class.getName();
    private final Activity callingActivity;
    private final Context context;

    public UpdateDatabaseThread(Activity callingActivity, Context context){
        this.callingActivity = callingActivity;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressVisibility, Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{View.VISIBLE, 0, "Loading storage directory"});
            DatabaseManager.initializeDatabase(callingActivity.getBaseContext());

            String storageDirectoryUri = Utilities.readStringFromSharedPreferences(callingActivity, STORAGE_DIRECTORY_URI, "");
            if (StringUtils.isEmpty(storageDirectoryUri)) {
                throw new FolkSetsException("The preference containing the storage directory uri is null or empty.", null);
            }
            IoUtilities.assertDirectoryExist(context, storageDirectoryUri);

            List<DocumentFile> documentFileList = IoUtilities.listPdfFilesFromStorage(context, callingActivity);
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{1, "Loading known tunes"});
            List<TuneEntity> tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID + "," + TUNE_FILE_PATH, null, null, null, null);
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{2, "Loading deleted tunes"});
            //Remove deprecated tunes
            List<Long> tuneIdToRemoveList = tuneEntityList.stream()
                    .filter(tuneEntity -> documentFileList.stream().
                            noneMatch(documentFile -> documentFile.getUri().toString().equals(tuneEntity.tuneFilePath)))
                    .map(tuneEntity -> tuneEntity.tuneId)
                    .collect(Collectors.toList());
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{3, "Loading tune deletions"});
            DatabaseManager.removeTunesFromDatabase(tuneIdToRemoveList);
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{4, "Loading new tunes"});
            //Insert new tunes
            List<TuneEntity> tuneEntityToAddList = documentFileList.stream()
                    .filter(documentFile -> tuneEntityList.stream()
                            .noneMatch(tuneEntity -> tuneEntity.tuneFilePath.equals(documentFile.getUri().toString())))
                    .map(documentFile -> new TuneEntity(FilenameUtils.getBaseName(documentFile.getName()), documentFile.getUri().toString(), documentFile.getType(), OffsetDateTime.now().toString()))
                    .collect(Collectors.toList());
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{5, "Loading tune insertions"});
            DatabaseManager.insertTunesInDatabase(tuneEntityToAddList);
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{6, "Loading final tune list"});
            //Get tune list
            if (StaticData.tuneEntityList == null || StaticData.tuneEntityList.isEmpty() || !tuneIdToRemoveList.isEmpty() || !tuneEntityToAddList.isEmpty()) {
                StaticData.tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID + "," + TUNE_TITLES, null, null, TUNE_TITLES, null);
                broadcastMessage(context, Constants.BroadcastName.staticDataUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.staticDataValue}, new String[]{TUNE_ENTITY_LIST});
            }
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{7, "Loading final set list"});
            //Get set list
            List<SetEntity> setEntityList = DatabaseManager.findAllSetsInDatabase("*", SET_NAME, null);
            if (StaticData.setEntityList == null || StaticData.setEntityList.isEmpty() || !setEntityList.equals(StaticData.setEntityList)) {
                StaticData.setEntityList = setEntityList;
                broadcastMessage(context, Constants.BroadcastName.staticDataUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.staticDataValue}, new String[]{SET_ENTITY_LIST});
            }
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{8, "Loading unique tune titles"});
            //Get unique values
            StaticData.uniqueTuneTitleArray = DatabaseManager.getAllUniqueTitleInTuneTable();
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{9, "Loading unique tune tags"});
            StaticData.uniqueTuneTagArray = DatabaseManager.getAllUniqueTagInTuneTable();
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{10, "Loading unique tune composers"});
            StaticData.uniqueTuneComposerArray = DatabaseManager.getAllUniqueComposerInTuneTable();
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{11, "Loading unique tune regions of origin"});
            StaticData.uniqueTuneRegionArray = DatabaseManager.getAllUniqueRegionInTuneTable();
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{12, "Loading unique tune keys"});
            StaticData.uniqueTuneKeyArray = DatabaseManager.getAllUniqueKeyInTuneTable();
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{13, "Loading unique tune incipits"});
            StaticData.uniqueTuneIncipitArray = DatabaseManager.getAllUniqueIncipitInTuneTable();
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{14, "Loading unique tune forms"});
            StaticData.uniqueTuneFormArray = DatabaseManager.getAllUniqueFormInTuneTable();
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{15, "Loading unique tune players"});
            StaticData.uniqueTunePlayedByArray = DatabaseManager.getAllUniquePlayedByInTuneTable();
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{16, "Loading unique tune notes"});
            StaticData.uniqueTuneNoteArray = DatabaseManager.getAllUniqueNoteInTuneTable();
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{17, "Loading unique set names"});
            StaticData.uniqueSetNameArray = DatabaseManager.getAllUniqueNameInSetTable();
            broadcastMessage(context, Constants.BroadcastName.staticDataUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.staticDataValue}, new String[]{UNIQUE_VALUES});
            broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{18, "Loading complete"});
            //Linger a few more seconds
        } catch (Exception e) {
            ExceptionManager.manageException(callingActivity, context, TAG, new FolkSetsException("An exception occured while executing the thread that updates the database from the storage directory content.", e));
        } finally {
            try {
                sleep(3000L);
                broadcastMessage(context, Constants.BroadcastName.mainActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressVisibility}, new Integer[]{View.GONE});
            } catch (Exception e2) {
                ExceptionManager.manageException(callingActivity, context, TAG, new FolkSetsException("An error occured while ending the UpdateDatabase thread.", e2));
            }
        }
    }
}
