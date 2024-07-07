package com.bandito.folksets.sql.mapper;

import static com.bandito.folksets.util.Constants.*;

import android.database.Cursor;

import com.bandito.folksets.sql.entities.SetEntity;

public class CursorToSetEntityMapper {
    public static SetEntity mapCursorToSetEntity(Cursor cursor) {
        SetEntity setEntity = new SetEntity();
        int columnIndex = cursor.getColumnIndex(SET_ID);
        setEntity.setId = columnIndex >= 0 ? cursor.getLong(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SET_NAME);
        setEntity.setName = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        columnIndex = cursor.getColumnIndex(SET_TUNES);
        setEntity.setTunes = columnIndex >= 0 ? cursor.getString(columnIndex) : null;
        return setEntity;
    }
}
