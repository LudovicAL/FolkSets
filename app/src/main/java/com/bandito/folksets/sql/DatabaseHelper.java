package com.bandito.folksets.sql;

import static com.bandito.folksets.util.Constants.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.sql.mapper.CursorToSetEntityMapper;
import com.bandito.folksets.sql.mapper.CursorToTuneEntityMapper;
import com.bandito.folksets.sql.mapper.SetEntityToContentValuesMapper;
import com.bandito.folksets.sql.mapper.TuneEntityToContentValuesMapper;
import com.bandito.folksets.util.IoUtilities;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE_TUNE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TUNE + " ("
                + TUNE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TUNE_TITLES + " TEXT NOT NULL, "
                + TUNE_TAGS + " TEXT, "
                + TUNE_FILE_PATH + " TEXT NOT NULL, "
                + TUNE_FILE_TYPE + " TEXT NOT NULL, "
                + TUNE_COMPOSER + " TEXT, "
                + TUNE_REGION_OF_ORIGIN + " TEXT, "
                + TUNE_KEY + " TEXT, "
                + TUNE_INCIPIT + " TEXT, "
                + TUNE_FORM + " TEXT, "
                + TUNE_PLAYED_BY + " TEXT, "
                + TUNE_NOTE + " TEXT, "
                + TUNE_FILE_CREATION_DATE + " TEXT NOT NULL, "
                + TUNE_LAST_CONSULTATION_DATE + " TEXT, "
                + TUNE_CONSULTATION_NUMBER + " INTEGER NOT NULL"
                + ")";

    private static final String CREATE_TABLE_SET =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SET + " ("
                + SET_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SET_NAME  + " TEXT NOT NULL, "
                + SET_TUNES + " TEXT"
                + ")";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        initializeDatabase(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onUpgrade(sqLiteDatabase, oldVersion, newVersion);
    }

    public void initializeDatabase(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_SET);
        sqLiteDatabase.execSQL(CREATE_TABLE_TUNE);
    }

    public void exportDatabase(Context context, String destinationFolder) throws FolkSetsException {
        IoUtilities.assertDirectoryExist(context, destinationFolder);
        File databaseFile = context.getDatabasePath(DATABASE_NAME);
        IoUtilities.assertFileExist(databaseFile);
        IoUtilities.copySourceFileToDestination(context, databaseFile, destinationFolder, DATABASE_NAME, "application/x-sqlite3");
    }

    public void importDatabase(Context context, String sourceFolder) throws FolkSetsException {
        try {
            IoUtilities.assertDirectoryExist(context, sourceFolder);
        } catch (Exception exception) {
            return;
        }
        File databaseFile = context.getDatabasePath(DATABASE_NAME);
        IoUtilities.copySourceToDestinationFile(context, sourceFolder, databaseFile, DATABASE_NAME);
    }

    public long insertTuneInDatabase(SQLiteDatabase sqLiteDatabase, TuneEntity tuneEntity) {
        ContentValues contentValues = TuneEntityToContentValuesMapper.mapTuneEntityToContentValues(tuneEntity);
        if (contentValues.containsKey(TUNE_ID)) {
            contentValues.remove(TUNE_ID);
        }
        return sqLiteDatabase.insert(TABLE_TUNE, null, contentValues);
    }

    public void insertTunesInDatabase(SQLiteDatabase sqLiteDatabase, List<TuneEntity> tuneEntityList) {
        sqLiteDatabase.beginTransaction();
        try {
            for (TuneEntity tuneEntity : tuneEntityList) {
                sqLiteDatabase.insert(TABLE_TUNE, null, TuneEntityToContentValuesMapper.mapTuneEntityToContentValues(tuneEntity));
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public int removeTuneFromDatabase(SQLiteDatabase sqLiteDatabase, long tuneId) {
        removeTuneFromSets(sqLiteDatabase, tuneId);
        return sqLiteDatabase.delete(TABLE_TUNE, TUNE_ID + " LIKE '%" + tuneId + "%'", new String[0]);
    }

    public void removeTunesFromDatabase(SQLiteDatabase sqLiteDatabase, List<Long> tuneIds) {
        for (Long tuneId : tuneIds) {
            removeTuneFromSets(sqLiteDatabase, tuneId);
        }
        sqLiteDatabase.beginTransaction();
        try {
            for (Long tuneId : tuneIds) {
                sqLiteDatabase.delete(TABLE_TUNE, TUNE_ID + " LIKE '%" + tuneId + "%'", new String[0]);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public int updateTuneInDatabase(SQLiteDatabase sqLiteDatabase, TuneEntity tuneEntity) {
        String whereClause = TUNE_ID + " LIKE '%" + tuneEntity.tuneId + "%'";
        ContentValues contentValues = TuneEntityToContentValuesMapper.mapTuneEntityToContentValues(tuneEntity);
        contentValues.remove(TUNE_ID);
        return sqLiteDatabase.update(TABLE_TUNE, contentValues, whereClause, new String[0]);
    }

    public long insertSetInDatabase(SQLiteDatabase sqLiteDatabase, SetEntity setEntity) {
        ContentValues contentValues = SetEntityToContentValuesMapper.mapSetEntityToContentValues(setEntity);
        contentValues.remove(SET_ID);
        return sqLiteDatabase.insert(TABLE_SET, null, contentValues);
    }

    public int removeSetFromDatabase(SQLiteDatabase sqLiteDatabase, long setId) {
        String whereClause = SET_ID + " LIKE '%" + setId + "%'";
        return sqLiteDatabase.delete(TABLE_SET, whereClause, new String[0]);
    }

    public int updateSetInDatabase(SQLiteDatabase sqLiteDatabase, SetEntity setEntity) {
        String whereClause = SET_ID + " LIKE '%" + setEntity.setId + "%'";
        ContentValues contentValues = SetEntityToContentValuesMapper.mapSetEntityToContentValues(setEntity);
        contentValues.remove(SET_ID);
        return sqLiteDatabase.update(TABLE_SET, contentValues, whereClause, new String[0]);
    }

    public List<TuneEntity> findTunesByIdInDatabase(SQLiteDatabase sqLiteDatabase, String fieldsNames, String[] tuneIdArray, String sortOnField, String sortDirection) {
        fieldsNames = StringUtils.isNotBlank(fieldsNames) ? fieldsNames : "*";
        StringBuilder query = new StringBuilder("SELECT " + fieldsNames + " FROM " + TABLE_TUNE + " WHERE ");
        for (int i = 0, max = tuneIdArray.length; i < max; i++) {
            query.append(TUNE_ID + " = ").append(tuneIdArray[i]);
            if (i < max - 1) {
                query.append(" OR ");
            }
        }
        query.append(getSortOptionString(sortOnField, sortDirection));
        Cursor cursor = sqLiteDatabase.rawQuery(query.toString(), new String[0]);
        return convertCursorToTuneEntityList(cursor);
    }

    public List<TuneEntity> findTunesWithValueInListInDatabase(SQLiteDatabase sqLiteDatabase, String fieldsNames, String fieldListName, String[] valueArray, String sortOnField, String sortDirection) {
        fieldsNames = StringUtils.isNotBlank(fieldsNames) ? fieldsNames : "*";
        StringBuilder query = new StringBuilder();
        query.append("SELECT ").append(fieldsNames).append(" FROM ").append(TABLE_TUNE);
        if (fieldListName != null && valueArray != null) {
            for (int i = 0, max = valueArray.length; i < max; i++) {
                if (i == 0) {
                    query.append(" WHERE ");
                }
                query.append(fieldListName).append(" LIKE '%").append(valueArray[i]).append("%'");
                if (i < max - 1) {
                    query.append(" AND ");
                }
            }
        }
        query.append(getSortOptionString(sortOnField, sortDirection));
        Cursor cursor = sqLiteDatabase.rawQuery(query.toString(), new String[0]);
        return convertCursorToTuneEntityList(cursor);
    }

    public List<SetEntity> findSetByIdInDatabase(SQLiteDatabase sqLiteDatabase, String fieldsNames, String setId, String sortOnField, String sortDirection) {
        fieldsNames = StringUtils.isNotBlank(fieldsNames) ? fieldsNames : "*";
        String query = "SELECT " + fieldsNames + " FROM " + TABLE_SET + " WHERE " + SET_ID + " = " + setId;
        query += getSortOptionString(sortOnField, sortDirection);
        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[0]);
        return convertCursorToSetEntityList(cursor);
    }

    public List<SetEntity> findSetsByNameInDatabase(SQLiteDatabase sqLiteDatabase, String fieldsNames, String setName, String sortOnField, String sortDirection) {
        fieldsNames = StringUtils.isNotBlank(fieldsNames) ? fieldsNames : "*";
        String query = "SELECT " + fieldsNames + " FROM " + TABLE_SET + " WHERE " + SET_NAME + " LIKE '%" + setName + "%'";
        query += getSortOptionString(sortOnField, sortDirection);
        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[0]);
        return convertCursorToSetEntityList(cursor);
    }

    public List<SetEntity> findAllSetsInDatabase(SQLiteDatabase sqLiteDatabase, String fieldsNames, String sortOnField, String sortDirection) {
        fieldsNames = StringUtils.isNotBlank(fieldsNames) ? fieldsNames : "*";
        String query = "SELECT " + fieldsNames + " FROM " + TABLE_SET;
        query += getSortOptionString(sortOnField, sortDirection);
        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[0]);
        return convertCursorToSetEntityList(cursor);
    }

    public Pair<Integer, List<SetEntity>> findSetsWithTunesInDatabase(SQLiteDatabase sqLiteDatabase, String tuneTitles, String sortOnField, String sortDirection) {
        String[] tuneTitlesArray = StringUtils.split(tuneTitles, DEFAULT_SEPARATOR);
        List<TuneEntity> tuneEntityList = findTunesWithValueInListInDatabase(sqLiteDatabase, TUNE_ID, TUNE_TITLES, tuneTitlesArray, null, null);
        if (tuneEntityList.isEmpty()) {
            return new Pair<>(0, new ArrayList<>());
        } else {
            Long[] tuneIdArray = tuneEntityList.stream().map(tuneEntity -> tuneEntity.tuneId).toArray(Long[]::new);
            return new Pair<>(tuneEntityList.size(), findSetsWithTunesInDatabase(sqLiteDatabase, tuneIdArray, sortOnField, sortDirection));
        }
    }

    public List<SetEntity> findSetsWithTunesInDatabase(SQLiteDatabase sqLiteDatabase, Long[] tuneIdArray, String sortOnField, String sortDirection) {
        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_SET + " WHERE");
        for (int i = 0, max = tuneIdArray.length; i < max; i++) {
            query.append(" ").append(SET_TUNES).append(" LIKE '%").append(tuneIdArray[i]).append("%'");
            if (i < max - 1) {
                query.append(" OR");
            }
        }
        query.append(getSortOptionString(sortOnField, sortDirection));
        Cursor cursor = sqLiteDatabase.rawQuery(query.toString(), new String[0]);
        List<SetEntity> setEntityList = convertCursorToSetEntityList(cursor);
        for (int i = setEntityList.size() - 1; i >= 0; i--) {
            String[] setTuneArray = StringUtils.split(setEntityList.get(i).setTunes, DEFAULT_SEPARATOR);
            if (Arrays.stream(setTuneArray)
                    .map(setTune -> Long.valueOf(setTune))
                    .noneMatch(setTuneLong -> Arrays.asList(tuneIdArray).contains(setTuneLong))) {
                setEntityList.remove(i);
            }
        }
        return setEntityList;
    }

    public void truncateTable(SQLiteDatabase sqLiteDatabase, String tableName) {
        sqLiteDatabase.execSQL("DELETE FROM " + tableName);
    }

    private String getSortOptionString(String sortOnField, String sortDirection) {
        if (StringUtils.isEmpty(sortOnField)) {
            return "";
        }
        String sortOption = " ORDER BY " + sortOnField;
        if (!StringUtils.isEmpty(sortDirection)) {
            sortOption += " " + sortDirection;
        }
        return sortOption;
    }

    private List<TuneEntity> convertCursorToTuneEntityList(Cursor cursor) {
        List<TuneEntity> tuneEntityList = new ArrayList<>();
        while(cursor.moveToNext()) {
            tuneEntityList.add(CursorToTuneEntityMapper.mapCursorToTuneEntity(cursor));
        }
        return tuneEntityList;
    }

    private List<SetEntity> convertCursorToSetEntityList(Cursor cursor) {
        List<SetEntity> setEntityList = new ArrayList<>();
        while(cursor.moveToNext()) {
            setEntityList.add(CursorToSetEntityMapper.mapCursorToSetEntity(cursor));
        }
        return setEntityList;
    }

    private List<String> convertCursorToStringList(Cursor cursor, String field) {
        List<String> stringList = new ArrayList<>();
        boolean result = cursor.moveToNext();
        int columnIndex = cursor.getColumnIndex(field);
        if (result && columnIndex >= 0) {
            do {
                stringList.add(cursor.getString(columnIndex));
            } while (cursor.moveToNext());
        }
        return stringList;
    }

    public void removeTuneFromSets(SQLiteDatabase sqLiteDatabase, long tuneId) {
        List<SetEntity> setEntityList = findSetsWithTunesInDatabase(sqLiteDatabase, new Long[]{tuneId}, null, null);
        for (SetEntity setEntity : setEntityList) {
            List<String> setTunesList = Arrays.asList(StringUtils.split(setEntity.setTunes, DEFAULT_SEPARATOR));
            setTunesList = setTunesList.stream().filter(tuneIdInSet -> !tuneIdInSet.equals(String.valueOf(tuneId))).collect(Collectors.toList());
            if (setTunesList.isEmpty()) {
                removeSetFromDatabase(sqLiteDatabase, setEntity.setId);
            } else {
                setEntity.setTunes = String.join(",", setTunesList);
                updateSetInDatabase(sqLiteDatabase, setEntity);
            }
        }
    }

    public String[] getAllUniqueValueInTuneTable(SQLiteDatabase sqLiteDatabase, String field) {
        String query = "SELECT DISTINCT " + field + " FROM " + TABLE_TUNE + " WHERE " + field + " IS NOT NULL";
        query += getSortOptionString(field, null);
        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[0]);
        return convertCursorToStringList(cursor, field).toArray(new String[0]);
    }

    public String[] getAllUniqueTitleInTuneTable(SQLiteDatabase sqLiteDatabase) {
        String[] tuneTitlesArray = getAllUniqueValueInTuneTable(sqLiteDatabase, TUNE_TITLES);
        Set<String> uniqueTitleSet = new HashSet<>();
        for (String tuneTitles : tuneTitlesArray) {
            String[] titleArray = StringUtils.split(tuneTitles, DEFAULT_SEPARATOR);
            if (titleArray != null) {
                uniqueTitleSet.addAll(Arrays.asList(titleArray));
            }
        }
        return uniqueTitleSet.toArray(new String[0]);
    }

    public String[] getAllUniqueTagInTuneTable(SQLiteDatabase sqLiteDatabase) {
        String[] tagsArray = getAllUniqueValueInTuneTable(sqLiteDatabase, TUNE_TAGS);
        Set<String> uniqueTagSet = new HashSet<>();
        for (String tags : tagsArray) {
            String[] tagArray = StringUtils.split(tags, DEFAULT_SEPARATOR);
            if (tagArray != null) {
                uniqueTagSet.addAll(Arrays.asList(tagArray));
            }
        }
        return uniqueTagSet.toArray(new String[0]);
    }

    public String[] getAllUniquePlayedByInTuneTable(SQLiteDatabase sqLiteDatabase) {
        String[] playersArray = getAllUniqueValueInTuneTable(sqLiteDatabase, TUNE_PLAYED_BY);
        Set<String> uniquePlayerSet = new HashSet<>();
        for (String players : playersArray) {
            String[] playerArray = StringUtils.split(players, DEFAULT_SEPARATOR);
            if (playerArray != null) {
                uniquePlayerSet.addAll(Arrays.asList(playerArray));
            }
        }
        return uniquePlayerSet.toArray(new String[0]);
    }

    public String[] getAllUniqueNameInSetTable(SQLiteDatabase sqLiteDatabase) {
        String query = "SELECT DISTINCT " + SET_NAME + " FROM " + TABLE_SET + " WHERE " + SET_NAME + " IS NOT NULL";
        query += getSortOptionString(SET_NAME, null);
        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[0]);
        return convertCursorToStringList(cursor, SET_NAME).toArray(new String[0]);
    }
}
