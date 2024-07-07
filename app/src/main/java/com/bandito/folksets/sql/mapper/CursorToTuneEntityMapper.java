package com.bandito.folksets.sql.mapper;

import static com.bandito.folksets.util.Constants.*;
import android.database.Cursor;

import com.bandito.folksets.sql.entities.TuneEntity;

public class CursorToTuneEntityMapper {
    public static TuneEntity mapCursorToTuneEntity(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(TUNE_TITLES);
        String tuneTitles = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_FILE_PATH);
        String tuneFilePath = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_FILE_TYPE);
        String tuneFileType = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_FILE_CREATION_DATE);
        String tuneFileCreatonDate = columnIndex >= 0 ? cursor.getString(columnIndex) : null;

        TuneEntity tuneEntity = new TuneEntity(tuneTitles, tuneFilePath, tuneFileType, tuneFileCreatonDate);

        columnIndex = cursor.getColumnIndex(TUNE_ID);
        tuneEntity.tuneId = columnIndex >= 0 ? cursor.getLong(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_TAGS);
        tuneEntity.tuneTags = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_COMPOSER);
        tuneEntity.tuneComposer = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_REGION_OF_ORIGIN);
        tuneEntity.tuneRegionOfOrigin = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_KEY);
        tuneEntity.tuneKey = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_INCIPIT);
        tuneEntity.tuneIncipit = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_FORM);
        tuneEntity.tuneForm = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_PLAYED_BY);
        tuneEntity.tunePlayedBy = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_NOTE);
        tuneEntity.tuneNote = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_LAST_CONSULTATION_DATE);
        tuneEntity.tuneLastConsultationDate = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(TUNE_CONSULTATION_NUMBER);
        tuneEntity.tuneConsultationNumber = columnIndex >= 0 ? cursor.getInt(columnIndex) : null;
        return tuneEntity;
    }
}
