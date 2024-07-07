package com.bandito.folksets.services;

import static com.bandito.folksets.util.Constants.PROGRESS_HINT;
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
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VISIBILITY}, new Integer[]{View.VISIBLE});
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{0, "Loading storage directory"});
            DatabaseManager.initializeDatabase(callingActivity.getBaseContext());
            List<DocumentFile> documentFileList = IoUtilities.listPdfFilesFromStorage(context, callingActivity);
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{1, "Loading known tunes"});
            List<SongEntity> songEntityList = DatabaseManager.findSongsWithValueInListInDatabase(SONG_ID + "," + SONG_FILE_PATH, null, null, null, null);
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{2, "Loading deleted tunes"});
            //Remove deprecated songs
            List<Long> songIdToRemoveList = songEntityList.stream()
                    .filter(songEntity -> documentFileList.stream().
                            noneMatch(documentFile -> documentFile.getUri().toString().equals(songEntity.songFilePath)))
                    .map(songEntity -> songEntity.songId)
                    .collect(Collectors.toList());
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{3, "Loading tune deletions"});
            DatabaseManager.removeSongsFromDatabase(songIdToRemoveList);
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{4, "Loading new tunes"});
            //Insert new songs
            List<SongEntity> songEntityToAddList = documentFileList.stream()
                    .filter(documentFile -> songEntityList.stream()
                            .noneMatch(songEntity -> songEntity.songFilePath.equals(documentFile.getUri().toString())))
                    .map(documentFile -> new SongEntity(FilenameUtils.getBaseName(documentFile.getName()), documentFile.getUri().toString(), documentFile.getType(), OffsetDateTime.now().toString()))
                    .collect(Collectors.toList());
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{5, "Loading tune insertions"});
            DatabaseManager.insertSongsInDatabase(songEntityToAddList);
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{6, "Loading final tune list"});
            //Get song list
            StaticData.songEntityList = DatabaseManager.findSongsWithValueInListInDatabase(SONG_ID + "," + SONG_TITLES, null, null, SONG_TITLES, null);
            broadcastMessage(context, STATICDATA_UPDATE, new String[]{VALUE_UPDATED}, new String[]{SONG_ENTITY_LIST});
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{7, "Loading final set list"});
            //Get set list
            StaticData.setEntityList = DatabaseManager.findAllSetsInDatabase("*", SET_NAME, null);
            broadcastMessage(context, STATICDATA_UPDATE, new String[]{VALUE_UPDATED}, new String[]{SET_ENTITY_LIST});
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{8, "Loading unique tune titles"});
            //Get unique values
            StaticData.uniqueSongTitleArray = DatabaseManager.getAllUniqueTitleInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{9, "Loading unique tune tags"});
            StaticData.uniqueSongTagArray = DatabaseManager.getAllUniqueTagInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{10, "Loading unique tune composers"});
            StaticData.uniqueSongComposerArray = DatabaseManager.getAllUniqueComposerInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{11, "Loading unique tune regions of origin"});
            StaticData.uniqueSongRegionArray = DatabaseManager.getAllUniqueRegionInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{12, "Loading unique tune keys"});
            StaticData.uniqueSongKeyArray = DatabaseManager.getAllUniqueKeyInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{13, "Loading unique tune incipits"});
            StaticData.uniqueSongIncipitArray = DatabaseManager.getAllUniqueIncipitInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{14, "Loading unique tune forms"});
            StaticData.uniqueSongFormArray = DatabaseManager.getAllUniqueFormInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{15, "Loading unique tune players"});
            StaticData.uniqueSongPlayedByArray = DatabaseManager.getAllUniquePlayedByInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{16, "Loading unique tune notes"});
            StaticData.uniqueSongNoteArray = DatabaseManager.getAllUniqueNoteInSongTable();
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{17, "Loading unique set names"});
            StaticData.uniqueSetNameArray = DatabaseManager.getAllUniqueNameInSetTable();
            broadcastMessage(context, STATICDATA_UPDATE, new String[]{VALUE_UPDATED}, new String[]{UNIQUE_VALUES});
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VALUE, PROGRESS_HINT}, new Serializable[]{18, "Loading complete"});
            //Linger a few more seconds
            sleep(3000L);
            broadcastMessage(context, PROGRESS_UPDATE, new String[]{PROGRESS_VISIBILITY}, new Integer[]{View.GONE});
        } catch (Exception e) {
            ExceptionManager.manageException(context, new FolkSetsException("An exception occured while executing the thread that updates the database from the storage directory content.", e));
        }
    }

    private void broadcastMessage(Context context, String intentName, String[] messageKey, Serializable[] messageValue) {
        Intent intent = new Intent(intentName);
        for (int i = 0, max = messageKey.length; i < max; i++) {
            intent.putExtra(messageKey[i], messageValue[i]);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
