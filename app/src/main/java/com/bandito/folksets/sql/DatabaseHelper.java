package com.bandito.folksets.sql;

import static com.bandito.folksets.util.Constants.*;

import static java.util.Objects.isNull;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.sql.mapper.CursorToSetEntityMapper;
import com.bandito.folksets.sql.mapper.CursorToSongEntityMapper;
import com.bandito.folksets.sql.mapper.SetEntityToContentValuesMapper;
import com.bandito.folksets.sql.mapper.SongEntityToContentValuesMapper;
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
    private static final String CREATE_TABLE_SONG =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SONG + " ("
                + SONG_ID                       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SONG_TITLES                   + " TEXT NOT NULL, "
                + SONG_TAGS                     + " TEXT, "
                + SONG_FILE_PATH                + " TEXT NOT NULL, "
                + SONG_FILE_TYPE                + " TEXT NOT NULL, "
                + SONG_COMPOSER                 + " TEXT, "
                + SONG_REGION_OF_ORIGIN         + " TEXT, "
                + SONG_KEY                      + " TEXT, "
                + SONG_INCIPIT                  + " TEXT, "
                + SONG_FORM                     + " TEXT, "
                + SONG_PLAYED_BY                + " TEXT, "
                + SONG_NOTE                     + " TEXT, "
                + SONG_FILE_CREATION_DATE       + " TEXT NOT NULL, "
                + SONG_LAST_CONSULTATION_DATE   + " TEXT, "
                + SONG_CONSULTATION_NUMBER      + " INTEGER NOT NULL"
                + ")";

    private static final String CREATE_TABLE_SET =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SET + " ("
                + SET_ID    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SET_NAME  + " TEXT NOT NULL, "
                + SET_SONGS + " TEXT"
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
        sqLiteDatabase.execSQL(CREATE_TABLE_SONG);
    }

    public void exportDatabase(Context context, String destinationFolder) throws FolkSetsException {
        IoUtilities.assertDirectoryExist(context, destinationFolder);
        File databaseFile = context.getDatabasePath(DATABASE_NAME);
        IoUtilities.assertFileExist(databaseFile);
        IoUtilities.copySourceFileToDestination(context, databaseFile, destinationFolder, DATABASE_NAME, "application/x-sqlite3");
        //Uri sourceUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", databaseFile);
        //Uri destinationUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", new File(destinationFolder));
        //DocumentsContract.copyDocument(context.getContentResolver(), sourceUri, destinationUri);
    }

    public void importDatabase(Context context, String sourceFolder) throws FolkSetsException {
        try {
            IoUtilities.assertDirectoryExist(context, sourceFolder);
        } catch (Exception exception) {
            return;
        }
        File databaseFile = context.getDatabasePath(DATABASE_NAME);
        IoUtilities.copySourceToDestinationFile(context, sourceFolder, databaseFile, DATABASE_NAME, "application/x-sqlite3");
    }

    public long insertSongInDatabase(SQLiteDatabase sqLiteDatabase, SongEntity songEntity) {
        ContentValues contentValues = SongEntityToContentValuesMapper.mapSongEntityToContentValues(songEntity);
        if (contentValues.containsKey(SONG_ID)) {
            contentValues.remove(SONG_ID);
        }
        return sqLiteDatabase.insert(TABLE_SONG, null, contentValues);
    }

    public void insertSongsInDatabase(SQLiteDatabase sqLiteDatabase, List<SongEntity> songEntityList) {
        sqLiteDatabase.beginTransaction();
        try {
            for (SongEntity songEntity : songEntityList) {
                sqLiteDatabase.insert(TABLE_SONG, null, SongEntityToContentValuesMapper.mapSongEntityToContentValues(songEntity));
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public int removeSongFromDatabase(SQLiteDatabase sqLiteDatabase, long songId) {
        removeSongFromSets(sqLiteDatabase, songId);
        return sqLiteDatabase.delete(TABLE_SONG, SONG_ID + " LIKE '%" + songId + "%'", new String[0]);
    }

    public void removeSongsFromDatabase(SQLiteDatabase sqLiteDatabase, List<Long> songIds) {
        for (Long songId : songIds) {
            removeSongFromSets(sqLiteDatabase, songId);
        }
        sqLiteDatabase.beginTransaction();
        try {
            for (Long songId : songIds) {
                sqLiteDatabase.delete(TABLE_SONG, SONG_ID + " LIKE '%" + songId + "%'", new String[0]);
            }
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    public int updateSongInDatabase(SQLiteDatabase sqLiteDatabase, SongEntity songEntity) {
        String whereClause = SONG_ID + " LIKE '%" + songEntity.songId + "%'";
        ContentValues contentValues = SongEntityToContentValuesMapper.mapSongEntityToContentValues(songEntity);
        contentValues.remove(SONG_ID);
        return sqLiteDatabase.update(TABLE_SONG, contentValues, whereClause, new String[0]);
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

    public List<SongEntity> findSongByIdInDatabase(SQLiteDatabase sqLiteDatabase, String fieldsNames, String songId, String sortOnField, String sortDirection) {
        fieldsNames = StringUtils.isNotBlank(fieldsNames) ? fieldsNames : "*";
        String query = "SELECT " + fieldsNames + " FROM " + TABLE_SONG + " WHERE " + SONG_ID + " = " + songId;
        query += getSortOptionString(sortOnField, sortDirection);
        Cursor cursor = sqLiteDatabase.rawQuery(query, new String[0]);
        return convertCursorToSongEntityList(cursor);
    }

    public List<SongEntity> findSongsWithValueInListInDatabase(SQLiteDatabase sqLiteDatabase, String fieldsNames, String fieldListName, String[] valueArray, String sortOnField, String sortDirection) {
        fieldsNames = StringUtils.isNotBlank(fieldsNames) ? fieldsNames : "*";
        StringBuilder query = new StringBuilder();
        query.append("SELECT ").append(fieldsNames).append(" FROM ").append(TABLE_SONG);
        if (!isNull(fieldListName) && !isNull(valueArray)) {
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
        return convertCursorToSongEntityList(cursor);
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

    public Pair<Integer, List<SetEntity>> findSetsWithSongsInDatabase(SQLiteDatabase sqLiteDatabase, String songTitles, String sortOnField, String sortDirection) {
        String[] songTitlesArray = StringUtils.split(songTitles, DEFAULT_SEPARATOR);
        List<SongEntity> songEntityList = findSongsWithValueInListInDatabase(sqLiteDatabase, SONG_ID, SONG_TITLES, songTitlesArray, null, null);
        if (songEntityList.isEmpty()) {
            return new Pair<>(0, new ArrayList<>());
        } else {
            Long[] songIdArray = songEntityList.stream().map(songEntity -> songEntity.songId).toArray(Long[]::new);
            return new Pair<>(songEntityList.size(), findSetsWithSongsInDatabase(sqLiteDatabase, songIdArray, sortOnField, sortDirection));
        }
    }

    public List<SetEntity> findSetsWithSongsInDatabase(SQLiteDatabase sqLiteDatabase, Long[] songIdArray, String sortOnField, String sortDirection) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM " + TABLE_SET + " WHERE");
        for (int i = 0, max = songIdArray.length; i < max; i++) {
            query.append(" ").append(SET_SONGS).append(" LIKE '%").append(songIdArray[i]).append("%'");
            if (i < max - 1) {
                query.append(" OR");
            }
        }
        query.append(getSortOptionString(sortOnField, sortDirection));
        Cursor cursor = sqLiteDatabase.rawQuery(query.toString(), new String[0]);
        List<SetEntity> setEntityList = convertCursorToSetEntityList(cursor);
        for (int i = setEntityList.size() - 1; i >= 0; i--) {
            String[] setSongArray = StringUtils.split(setEntityList.get(i).setSongs, DEFAULT_SEPARATOR);
            if (Arrays.stream(setSongArray)
                    .map(setSong -> Long.valueOf(setSong))
                    .noneMatch(setSongLong -> Arrays.asList(songIdArray).contains(setSongLong))) {
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

    private List<SongEntity> convertCursorToSongEntityList(Cursor cursor) {
        List<SongEntity> songEntityList = new ArrayList<>();
        while(cursor.moveToNext()) {
            songEntityList.add(CursorToSongEntityMapper.mapCursorToSongEntity(cursor));
        }
        return songEntityList;
    }

    private List<SetEntity> convertCursorToSetEntityList(Cursor cursor) {
        List<SetEntity> setEntityList = new ArrayList<>();
        while(cursor.moveToNext()) {
            setEntityList.add(CursorToSetEntityMapper.mapCursorToSetEntity(cursor));
        }
        return setEntityList;
    }

    public void removeSongFromSets(SQLiteDatabase sqLiteDatabase, long songId) {
        List<SetEntity> setEntityList = findSetsWithSongsInDatabase(sqLiteDatabase, new Long[]{songId}, null, null);
        for (SetEntity setEntity : setEntityList) {
            List<String> setSongsList = Arrays.asList(StringUtils.split(setEntity.setSongs, DEFAULT_SEPARATOR));
            setSongsList = setSongsList.stream().filter(songIdInSet -> !songIdInSet.equals(String.valueOf(songId))).collect(Collectors.toList());
            if (setSongsList.isEmpty()) {
                removeSetFromDatabase(sqLiteDatabase, setEntity.setId);
            } else {
                setEntity.setSongs = String.join(",", setSongsList);
                updateSetInDatabase(sqLiteDatabase, setEntity);
            }
        }
    }

    public Set<String> getAllTagsInSongTable(SQLiteDatabase sqLiteDatabase) {
        List<SongEntity> songEntityList = findSongsWithValueInListInDatabase(sqLiteDatabase, SONG_TAGS, null, null, null, null);
        Set<String> uniqueTagList = new HashSet<>();
        for (SongEntity songEntity : songEntityList) {
            uniqueTagList.addAll(Arrays.asList(StringUtils.split(songEntity.songTags, DEFAULT_SEPARATOR)));
        }
        return uniqueTagList;
    }

    public Set<String> getAllPlayersInSongTable(SQLiteDatabase sqLiteDatabase) {
        List<SongEntity> songEntityList = findSongsWithValueInListInDatabase(sqLiteDatabase, SONG_PLAYED_BY, null, null, null, null);
        Set<String> uniquePlayerList = new HashSet<>();
        for (SongEntity songEntity : songEntityList) {
            uniquePlayerList.addAll(Arrays.asList(StringUtils.split(songEntity.songPlayedBy, DEFAULT_SEPARATOR)));
        }
        return uniquePlayerList;
    }
}
