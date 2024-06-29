package com.bandito.folksets.services;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import androidx.documentfile.provider.DocumentFile;

import com.bandito.folksets.adapters.SongListRecyclerViewAdapter;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.IoUtilities;

import org.apache.commons.io.FilenameUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateDatabaseThread extends Thread {
    private final ProgressBar progressBar;
    private final Activity callingActivity;
    private final Context context;

    private final SongListRecyclerViewAdapter songListRecyclerViewAdapter;

    public UpdateDatabaseThread(Activity callingActivity, Context context, ProgressBar progressBar, SongListRecyclerViewAdapter songListRecyclerViewAdapter){
        this.callingActivity = callingActivity;
        this.context = context;
        this.progressBar = progressBar;
        this.songListRecyclerViewAdapter = songListRecyclerViewAdapter;
    }

    @Override
    public void run() {
        try {
            DatabaseManager.initializeDatabase(callingActivity.getBaseContext());
            callingActivity.runOnUiThread(() -> progressBar.setVisibility(View.VISIBLE));
            List<DocumentFile> documentFileList = IoUtilities.listPdfFilesFromStorage(context, callingActivity);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(3));
            List<SongEntity> songEntityList = DatabaseManager.findSongsInDatabase(Constants.SONG_ID + "," + Constants.SONG_FILE_PATH, null, null, null, null);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(4));
            //Remove deprecated songs
            List<Long> songIdToRemoveList = songEntityList.stream()
                    .filter(songEntity -> documentFileList.stream().
                            noneMatch(documentFile -> documentFile.getUri().toString().equals(songEntity.songFilePath)))
                    .map(songEntity -> songEntity.songId)
                    .collect(Collectors.toList());
            callingActivity.runOnUiThread(() -> progressBar.setProgress(5));
            DatabaseManager.removeSongsFromDatabase(songIdToRemoveList);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(6));
            //Insert new songs

            List<SongEntity> songEntityToAddList = documentFileList.stream()
                    .filter(documentFile -> songEntityList.stream()
                            .noneMatch(songEntity -> songEntity.songFilePath.equals(documentFile.getUri().toString())))
                    .map(documentFile -> new SongEntity(FilenameUtils.getBaseName(documentFile.getName()), documentFile.getUri().toString(), documentFile.getType(), OffsetDateTime.now().toString()))
                    .collect(Collectors.toList());
            callingActivity.runOnUiThread(() -> progressBar.setProgress(7));
            DatabaseManager.insertSongsInDatabase(songEntityToAddList);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(8));
            //Update song adapter
            SongListRecyclerViewAdapter.songEntityList = DatabaseManager.findSongsInDatabase(Constants.SONG_ID + "," + Constants.SONG_TITLES, null, null, null, null);
            callingActivity.runOnUiThread(() -> progressBar.setProgress(9));
            callingActivity.runOnUiThread(() -> songListRecyclerViewAdapter.notifyDataSetChanged());
            callingActivity.runOnUiThread(() -> progressBar.setProgress(10));
            //Linger a few more seconds
            sleep(3000L);
            callingActivity.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
        } catch (Exception e) {
            ExceptionManager.manageException(new FolkSetsException("An exception occured while executing the thread that updates the database from the storage directory content.", e));
        }
    }
}
