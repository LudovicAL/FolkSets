package com.bandito.folksets.sql.mapper;

import static com.bandito.folksets.util.Constants.*;
import android.database.Cursor;

import com.bandito.folksets.sql.entities.SongEntity;

public class CursorToSongEntityMapper {
    public static SongEntity mapCursorToSongEntity(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(SONG_TITLES);
        String songTitles = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_FILE_PATH);
        String songFilePath = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_FILE_TYPE);
        String songFileType = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_FILE_CREATION_DATE);
        String songFileCreatonDate = columnIndex >= 0 ? cursor.getString(columnIndex) : null;

        SongEntity songEntity = new SongEntity(songTitles, songFilePath, songFileType, songFileCreatonDate);

        columnIndex = cursor.getColumnIndex(SONG_ID);
        songEntity.songId = columnIndex >= 0 ? cursor.getLong(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_TAGS);
        songEntity.songTags = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_COMPOSER);
        songEntity.songComposer = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_REGION_OF_ORIGIN);
        songEntity.songRegionOfOrigin = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_KEY);
        songEntity.songKey = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_INCIPIT);
        songEntity.songIncipit = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_FORM);
        songEntity.songForm = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_PLAYED_BY);
        songEntity.songPlayedBy = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_NOTE);
        songEntity.songNote = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_LAST_CONSULTATION_DATE);
        songEntity.songLastConsultationDate = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SONG_CONSULTATION_NUMBER);
        songEntity.songConsultationNumber = columnIndex >= 0 ? cursor.getInt(columnIndex) : null;
        return songEntity;
    }
}
