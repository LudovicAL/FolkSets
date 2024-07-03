package com.bandito.folksets.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;

import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.IoUtilities;
import com.bandito.folksets.util.StaticData;

import org.apache.commons.io.FilenameUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateDatabaseThread extends Thread {
    private final ProgressBar progressBar;
    private final Activity callingActivity;
    private final Context context;

    public UpdateDatabaseThread(Activity callingActivity, Context context, ProgressBar progressBar){
        this.callingActivity = callingActivity;
        this.context = context;
        this.progressBar = progressBar;
    }

    @Override
    public void run() {
        try {
            progressBar.setMax(19);
            progressBar.setProgress(0);
            callingActivity.runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
            DatabaseManager.initializeDatabase(callingActivity.getBaseContext());
            List<DocumentFile> documentFileList = IoUtilities.listPdfFilesFromStorage(context, callingActivity);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(1));
            List<SongEntity> songEntityList = DatabaseManager.findSongsWithValueInListInDatabase(Constants.SONG_ID + "," + Constants.SONG_FILE_PATH, null, null, null, null);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(2));
            //Remove deprecated songs
            List<Long> songIdToRemoveList = songEntityList.stream()
                    .filter(songEntity -> documentFileList.stream().
                            noneMatch(documentFile -> documentFile.getUri().toString().equals(songEntity.songFilePath)))
                    .map(songEntity -> songEntity.songId)
                    .collect(Collectors.toList());
            callingActivity.runOnUiThread(() -> progressBar.setProgress(3));
            DatabaseManager.removeSongsFromDatabase(songIdToRemoveList);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(4));
            //Insert new songs
            List<SongEntity> songEntityToAddList = documentFileList.stream()
                    .filter(documentFile -> songEntityList.stream()
                            .noneMatch(songEntity -> songEntity.songFilePath.equals(documentFile.getUri().toString())))
                    .map(documentFile -> new SongEntity(FilenameUtils.getBaseName(documentFile.getName()), documentFile.getUri().toString(), documentFile.getType(), OffsetDateTime.now().toString()))
                    .collect(Collectors.toList());
            callingActivity.runOnUiThread(() -> progressBar.setProgress(5));
            DatabaseManager.insertSongsInDatabase(songEntityToAddList);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(6));
            //Get song list
            StaticData.songEntityList = DatabaseManager.findSongsWithValueInListInDatabase(Constants.SONG_ID + "," + Constants.SONG_TITLES, null, null, Constants.SONG_TITLES, null);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(7));
            broadcastMessage(context, Constants.STATICDATA_UPDATE, Constants.VALUE_UPDATED, Constants.SONG_ENTITY_LIST);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(8));
            //Get set list
            StaticData.setEntityList = DatabaseManager.findAllSetsInDatabase("*", Constants.SET_NAME, null);
            broadcastMessage(context, Constants.STATICDATA_UPDATE, Constants.VALUE_UPDATED, Constants.SET_ENTITY_LIST);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(9));
            //Get unique values
            StaticData.uniqueSongTitleArray = DatabaseManager.getAllUniqueTitleInSongTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(10));
            StaticData.uniqueSongTagArray = DatabaseManager.getAllUniqueTagInSongTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(11));
            StaticData.uniqueSongComposerArray = DatabaseManager.getAllUniqueComposerInSongTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(12));
            StaticData.uniqueSongRegionArray = DatabaseManager.getAllUniqueRegionInSongTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(13));
            StaticData.uniqueSongKeyArray = DatabaseManager.getAllUniqueKeyInSongTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(14));
            StaticData.uniqueSongIncipitArray = DatabaseManager.getAllUniqueIncipitInSongTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(15));
            StaticData.uniqueSongFormArray = DatabaseManager.getAllUniqueFormInSongTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(16));
            StaticData.uniqueSongPlayedByArray = DatabaseManager.getAllUniquePlayedByInSongTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(17));
            StaticData.uniqueSongNoteArray = DatabaseManager.getAllUniqueNoteInSongTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(18));
            StaticData.uniqueSetNameArray = DatabaseManager.getAllUniqueNameInSetTable();
            callingActivity.runOnUiThread(() -> progressBar.setProgress(19));
            //Linger a few more seconds
            sleep(3000L);
            callingActivity.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
        } catch (Exception e) {
            ExceptionManager.manageException(context, new FolkSetsException("An exception occured while executing the thread that updates the database from the storage directory content.", e));
        }
    }

    private void broadcastMessage(Context context, String intentName, String messageKey, String messageValue) {
        Intent intent = new Intent(intentName);
        intent.putExtra(messageKey, messageValue);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
