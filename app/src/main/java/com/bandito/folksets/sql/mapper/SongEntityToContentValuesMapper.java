package com.bandito.folksets.sql.mapper;

import static com.bandito.folksets.util.Constants.*;
import android.content.ContentValues;

import com.bandito.folksets.sql.entities.SongEntity;

public class SongEntityToContentValuesMapper {
    public static ContentValues mapSongEntityToContentValues(SongEntity songEntity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SONG_ID, songEntity.songId);
        contentValues.put(SONG_TITLES, songEntity.songTitles);
        contentValues.put(SONG_TAGS, songEntity.songTags);
        contentValues.put(SONG_FILE_PATH, songEntity.songFilePath);
        contentValues.put(SONG_FILE_TYPE, songEntity.songFileType);
        contentValues.put(SONG_COMPOSER, songEntity.songComposer);
        contentValues.put(SONG_REGION_OF_ORIGIN, songEntity.songRegionOfOrigin);
        contentValues.put(SONG_KEY, songEntity.songKey);
        contentValues.put(SONG_INCIPIT, songEntity.songIncipit);
        contentValues.put(SONG_FORM, songEntity.songForm);
        contentValues.put(SONG_PLAYED_BY, songEntity.songPlayedBy);
        contentValues.put(SONG_NOTE, songEntity.songNote);
        contentValues.put(SONG_FILE_CREATION_DATE, songEntity.songFileCreationDate);
        contentValues.put(SONG_LAST_CONSULTATION_DATE, songEntity.songLastConsultationDate);
        contentValues.put(SONG_CONSULTATION_NUMBER, songEntity.songConsultationNumber);
        return contentValues;
    }
}
