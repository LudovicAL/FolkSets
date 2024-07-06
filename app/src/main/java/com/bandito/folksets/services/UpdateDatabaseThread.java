package com.bandito.folksets.services;

import static com.bandito.folksets.util.Constants.PROGRESS_UPDATE;
import static com.bandito.folksets.util.Constants.PROGRESS_VALUE;
import static com.bandito.folksets.util.Constants.PROGRESS_VISIBILITY;
import static com.bandito.folksets.util.Constants.SET_ENTITY_LIST;
import static com.bandito.folksets.util.Constants.SET_NAME;
import static com.bandito.folksets.util.Constants.SONG_ENTITY_LIST;
import static com.bandito.folksets.util.Constants.SONG_FILE_PATH;
import static com.bandito.folksets.util.Constants.SONG_ID;
import static com.bandito.folksets.util.Constants.SONG_TITLES;
import static com.bandito.folksets.util.Constants.STATICDATA_UPDATE;
import static com.bandito.folksets.util.Constants.UNIQUE_VALUES;
import static com.bandito.folksets.util.Constants.VALUE_UPDATED;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.documentfile.provider.DocumentFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.IoUtilities;
import com.bandito.folksets.util.StaticData;

import org.apache.commons.io.FilenameUtils;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateDatabaseThread extends Thread {
    private final Activity callingActivity;
    private final Context context;

    public UpdateDatabaseThread(Activity callingActivity, Context context){
        this.callingActivity = callingActivity;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VISIBILITY, View.VISIBLE);
            DatabaseManager.initializeDatabase(callingActivity.getBaseContext());
            List<DocumentFile> documentFileList = IoUtilities.listPdfFilesFromStorage(context, callingActivity);
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 1);
            List<SongEntity> songEntityList = DatabaseManager.findSongsWithValueInListInDatabase(SONG_ID + "," + SONG_FILE_PATH, null, null, null, null);
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 2);
            //Remove deprecated songs
            List<Long> songIdToRemoveList = songEntityList.stream()
                    .filter(songEntity -> documentFileList.stream().
                            noneMatch(documentFile -> documentFile.getUri().toString().equals(songEntity.songFilePath)))
                    .map(songEntity -> songEntity.songId)
                    .collect(Collectors.toList());
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 3);
            DatabaseManager.removeSongsFromDatabase(songIdToRemoveList);
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 4);
            //Insert new songs
            List<SongEntity> songEntityToAddList = documentFileList.stream()
                    .filter(documentFile -> songEntityList.stream()
                            .noneMatch(songEntity -> songEntity.songFilePath.equals(documentFile.getUri().toString())))
                    .map(documentFile -> new SongEntity(FilenameUtils.getBaseName(documentFile.getName()), documentFile.getUri().toString(), documentFile.getType(), OffsetDateTime.now().toString()))
                    .collect(Collectors.toList());
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 5);
            DatabaseManager.insertSongsInDatabase(songEntityToAddList);
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 6);
            //Get song list
            StaticData.songEntityList = DatabaseManager.findSongsWithValueInListInDatabase(SONG_ID + "," + SONG_TITLES, null, null, SONG_TITLES, null);
            broadcastMessage(context, STATICDATA_UPDATE, VALUE_UPDATED, SONG_ENTITY_LIST);
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 7);
            //Get set list
            StaticData.setEntityList = DatabaseManager.findAllSetsInDatabase("*", SET_NAME, null);
            broadcastMessage(context, STATICDATA_UPDATE, VALUE_UPDATED, SET_ENTITY_LIST);
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 8);
            //Get unique values
            StaticData.uniqueSongTitleArray = DatabaseManager.getAllUniqueTitleInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 9);
            StaticData.uniqueSongTagArray = DatabaseManager.getAllUniqueTagInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 10);
            StaticData.uniqueSongComposerArray = DatabaseManager.getAllUniqueComposerInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 11);
            StaticData.uniqueSongRegionArray = DatabaseManager.getAllUniqueRegionInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 12);
            StaticData.uniqueSongKeyArray = DatabaseManager.getAllUniqueKeyInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 13);
            StaticData.uniqueSongIncipitArray = DatabaseManager.getAllUniqueIncipitInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 14);
            StaticData.uniqueSongFormArray = DatabaseManager.getAllUniqueFormInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 15);
            StaticData.uniqueSongPlayedByArray = DatabaseManager.getAllUniquePlayedByInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 16);
            StaticData.uniqueSongNoteArray = DatabaseManager.getAllUniqueNoteInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 17);
            StaticData.uniqueSetNameArray = DatabaseManager.getAllUniqueNameInSetTable();
            broadcastMessage(context, STATICDATA_UPDATE, VALUE_UPDATED, UNIQUE_VALUES);
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VALUE, 18);
            //Linger a few more seconds
            sleep(3000L);
            broadcastMessage(context, PROGRESS_UPDATE, PROGRESS_VISIBILITY, View.GONE);
        } catch (Exception e) {
            ExceptionManager.manageException(context, new FolkSetsException("An exception occured while executing the thread that updates the database from the storage directory content.", e));
        }
    }

    private void broadcastMessage(Context context, String intentName, String messageKey, Serializable messageValue) {
        Intent intent = new Intent(intentName);
        intent.putExtra(messageKey, messageValue);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
