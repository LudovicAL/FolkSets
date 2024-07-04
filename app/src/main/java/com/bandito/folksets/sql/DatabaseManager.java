package com.bandito.folksets.sql;

import static com.bandito.folksets.util.Constants.SONG_COMPOSER;
import static com.bandito.folksets.util.Constants.SONG_FORM;
import static com.bandito.folksets.util.Constants.SONG_INCIPIT;
import static com.bandito.folksets.util.Constants.SONG_KEY;
import static com.bandito.folksets.util.Constants.SONG_NOTE;
import static com.bandito.folksets.util.Constants.SONG_REGION_OF_ORIGIN;
import static com.bandito.folksets.util.Constants.STORAGE_DIRECTORY_URI;
import static java.util.Objects.isNull;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.SongEntity;
import com.bandito.folksets.util.Utilities;
import android.app.Activity;

import androidx.core.util.Pair;

import java.util.List;

public class DatabaseManager {

    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase sqLiteDatabase;

    public static void initializeDatabase(Context context) throws FolkSetsException {
        try {
            if (isNull(databaseHelper)) {
                databaseHelper = new DatabaseHelper(context);
            }
            if (isNull(sqLiteDatabase) || !sqLiteDatabase.isOpen()) {
                sqLiteDatabase = databaseHelper.getWritableDatabase();
                databaseHelper.initializeDatabase(sqLiteDatabase);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while initializing the database.", e, true);
        }
    }

    public static void closeDatabase() {
        databaseHelper.close();
    }

    public static void importDatabase(Context context, Activity activity) throws FolkSetsException {
        try {
            String selectedFolder = Utilities.readStringFromSharedPreferences(activity, STORAGE_DIRECTORY_URI, null);
            if (!isNull(selectedFolder)) {
                databaseHelper.importDatabase(context, selectedFolder);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while importing the database.", e);
        }
    }

    public static void exportDatabase(Context context, Activity activity) throws FolkSetsException {
        try {
            String selectedFolder = Utilities.readStringFromSharedPreferences(activity, STORAGE_DIRECTORY_URI, null);
            if (!isNull(selectedFolder)) {
                databaseHelper.exportDatabase(context, selectedFolder);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while exporting the database.", e);
        }
    }

    public static long insertSongInDatabase(SongEntity songEntity) throws FolkSetsException {
        try {
            long result = databaseHelper.insertSongInDatabase(sqLiteDatabase, songEntity);
            if (result == -1) {
                throw new FolkSetsException("The insert function failed.", null);
            }
            return result;
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while inserting a song in the database.", e);
        }
    }

    public static void insertSongsInDatabase(List<SongEntity> songEntityList) throws FolkSetsException {
        try {
            if (!songEntityList.isEmpty()) {
                databaseHelper.insertSongsInDatabase(sqLiteDatabase, songEntityList);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while inserting songs in the database.", e);
        }
    }

    public static int removeSongFromDatabase(long songId) throws FolkSetsException {
        try {
            return databaseHelper.removeSongFromDatabase(sqLiteDatabase, songId);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while removing a song from the database.", e);
        }
    }

    public static void removeSongsFromDatabase(List<Long> songIds) throws FolkSetsException {
        try {
            if (!songIds.isEmpty()) {
                databaseHelper.removeSongsFromDatabase(sqLiteDatabase, songIds);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while removing songs from the database.", e);
        }
    }

    public static int updateSongInDatabase(SongEntity songEntity) throws FolkSetsException {
        try {
            return databaseHelper.updateSongInDatabase(sqLiteDatabase, songEntity);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while updating a song in the database.", e);
        }
    }

    public static long insertSetInDatabase(SetEntity setEntity) throws FolkSetsException {
        try {
            long result = databaseHelper.insertSetInDatabase(sqLiteDatabase, setEntity);
            if (result == -1) {
                throw new FolkSetsException("The insert function failed.", null);
            }
            return result;
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while inserting a set in the database.", e);
        }
    }

    public static int removeSetFromDatabase(long setId) throws FolkSetsException {
        try {
            return databaseHelper.removeSetFromDatabase(sqLiteDatabase, setId);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while removing a set from the database.", e);
        }
    }

    public static int updateSetInDatabase(SetEntity setEntity) throws FolkSetsException {
        try {
            return databaseHelper.updateSetInDatabase(sqLiteDatabase, setEntity);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while updating a set in the database.", e);
        }
    }

    public static List<SongEntity> findSongByIdInDatabase(String fieldsNames, Long songId, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSongsByIdInDatabase(sqLiteDatabase, fieldsNames, new String[]{String.valueOf(songId)}, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for songs in the database.", e);
        }
    }

    public static List<SongEntity> findSongByIdInDatabase(String fieldsNames, String songId, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSongsByIdInDatabase(sqLiteDatabase, fieldsNames, new String[]{songId}, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for songs in the database.", e);
        }
    }

    public static List<SongEntity> findSongsByIdInDatabase(String fieldsNames, String[] songIdArray, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSongsByIdInDatabase(sqLiteDatabase, fieldsNames, songIdArray, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for songs in the database.", e);
        }
    }

    public static List<SongEntity> findSongsWithValueInListInDatabase(String fieldsNames, String fieldListName, String[] valueArray, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSongsWithValueInListInDatabase(sqLiteDatabase, fieldsNames, fieldListName, valueArray, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for songs with tags in the database.", e);
        }
    }

    public static List<SetEntity> findSetByIdInDatabase(String fieldsNames, Long setId, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSetByIdInDatabase(sqLiteDatabase, fieldsNames, String.valueOf(setId), sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for sets in the database.", e);
        }
    }

    public static List<SetEntity> findSetByIdInDatabase(String fieldsNames, String setId, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSetByIdInDatabase(sqLiteDatabase, fieldsNames, setId, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for sets in the database.", e);
        }
    }

    public static List<SetEntity> findSetsByNameInDatabase(String fieldsNames, String setName, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSetsByNameInDatabase(sqLiteDatabase, fieldsNames, setName, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for sets in the database.", e);
        }
    }

    public static List<SetEntity> findAllSetsInDatabase(String fieldsNames, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findAllSetsInDatabase(sqLiteDatabase, fieldsNames, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for sets in the database.", e);
        }
    }

    public static Pair<Integer, List<SetEntity>> findSetsWithSongsInDatabase(String songTitles, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSetsWithSongsInDatabase(sqLiteDatabase, songTitles, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for sets with a songs title in the database.", e);
        }
    }

    public static List<SetEntity> findSetsWithSongsInDatabase(Long[] songId, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSetsWithSongsInDatabase(sqLiteDatabase, songId, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for sets with a songs id in the database.", e);
        }
    }

    public static void truncateTable(String tableName) throws FolkSetsException {
        try {
            databaseHelper.truncateTable(sqLiteDatabase, tableName);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while truncating table " + tableName + ".", e);
        }
    }

    public static void removeSongFromSets(long songId) throws FolkSetsException {
        try {
            databaseHelper.removeSongFromSets(sqLiteDatabase, songId);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while removing a song from sets.", e);
        }
    }

    public static String[] getAllUniqueTitleInSongTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueTitleInSongTable(sqLiteDatabase);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique titles from the song table", e);
        }
    }

    public static String[] getAllUniqueTagInSongTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueTagInSongTable(sqLiteDatabase);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique tags from the song table", e);
        }
    }

    public static String[] getAllUniqueComposerInSongTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInSongTable(sqLiteDatabase, SONG_COMPOSER);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique composers from the song table", e);
        }
    }

    public static String[] getAllUniqueRegionInSongTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInSongTable(sqLiteDatabase, SONG_REGION_OF_ORIGIN);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique regions of origin from the song table", e);
        }
    }

    public static String[] getAllUniqueKeyInSongTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInSongTable(sqLiteDatabase, SONG_KEY);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique keys from the song table", e);
        }
    }

    public static String[] getAllUniqueIncipitInSongTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInSongTable(sqLiteDatabase, SONG_INCIPIT);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique incipits from the song table", e);
        }
    }

    public static String[] getAllUniqueFormInSongTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInSongTable(sqLiteDatabase, SONG_FORM);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique forms from the song table", e);
        }
    }

    public static String[] getAllUniquePlayedByInSongTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniquePlayedByInSongTable(sqLiteDatabase);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique players from the song table", e);
        }
    }

    public static String[] getAllUniqueNoteInSongTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInSongTable(sqLiteDatabase, SONG_NOTE);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique notes from the song table", e);
        }
    }

    public static String[] getAllUniqueNameInSetTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueNameInSetTable(sqLiteDatabase);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique name from the set table", e);
        }
    }
}
