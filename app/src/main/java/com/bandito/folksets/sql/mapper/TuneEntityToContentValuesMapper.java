package com.bandito.folksets.sql.mapper;

import static com.bandito.folksets.util.Constants.*;
import android.content.ContentValues;

import com.bandito.folksets.sql.entities.TuneEntity;

public class TuneEntityToContentValuesMapper {
    public static ContentValues mapTuneEntityToContentValues(TuneEntity tuneEntity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TUNE_ID, tuneEntity.tuneId);
        contentValues.put(TUNE_TITLES, tuneEntity.tuneTitles);
        contentValues.put(TUNE_TAGS, tuneEntity.tuneTags);
        contentValues.put(TUNE_FILE_PATH, tuneEntity.tuneFilePath);
        contentValues.put(TUNE_FILE_TYPE, tuneEntity.tuneFileType);
        contentValues.put(TUNE_COMPOSER, tuneEntity.tuneComposer);
        contentValues.put(TUNE_REGION_OF_ORIGIN, tuneEntity.tuneRegionOfOrigin);
        contentValues.put(TUNE_KEY, tuneEntity.tuneKey);
        contentValues.put(TUNE_INCIPIT, tuneEntity.tuneIncipit);
        contentValues.put(TUNE_FORM, tuneEntity.tuneForm);
        contentValues.put(TUNE_PLAYED_BY, tuneEntity.tunePlayedBy);
        contentValues.put(TUNE_NOTE, tuneEntity.tuneNote);
        contentValues.put(TUNE_FILE_CREATION_DATE, tuneEntity.tuneFileCreationDate);
        contentValues.put(TUNE_LAST_CONSULTATION_DATE, tuneEntity.tuneLastConsultationDate);
        contentValues.put(TUNE_CONSULTATION_NUMBER, tuneEntity.tuneConsultationNumber);
        return contentValues;
    }
}
