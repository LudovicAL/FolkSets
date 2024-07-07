package com.bandito.folksets.sql.mapper;

import static com.bandito.folksets.util.Constants.*;

import android.content.ContentValues;

import com.bandito.folksets.sql.entities.SetEntity;

public class SetEntityToContentValuesMapper {
    public static ContentValues mapSetEntityToContentValues(SetEntity setEntity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SET_ID, setEntity.setId);
        contentValues.put(SET_NAME, setEntity.setName);
        contentValues.put(SET_TUNES, setEntity.setTunes);
        return contentValues;
    }
}
