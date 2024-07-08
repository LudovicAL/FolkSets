package com.bandito.folksets.sql;

import static com.bandito.folksets.util.Constants.TUNE_COMPOSER;
import static com.bandito.folksets.util.Constants.TUNE_FORM;
import static com.bandito.folksets.util.Constants.TUNE_INCIPIT;
import static com.bandito.folksets.util.Constants.TUNE_KEY;
import static com.bandito.folksets.util.Constants.TUNE_NOTE;
import static com.bandito.folksets.util.Constants.TUNE_REGION_OF_ORIGIN;
import static com.bandito.folksets.util.Constants.STORAGE_DIRECTORY_URI;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Utilities;
import android.app.Activity;

import androidx.core.util.Pair;

import java.util.List;

public class DatabaseManager {

    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase sqLiteDatabase;

    public static void initializeDatabase(Context context) throws FolkSetsException {
        try {
            if (databaseHelper == null) {
                databaseHelper = new DatabaseHelper(context);
            }
            if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
                sqLiteDatabase = databaseHelper.getWritableDatabase();
                databaseHelper.initializeDatabase(sqLiteDatabase);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while initializing the database.", e, true);
        }
    }

    public static void closeDatabase() throws FolkSetsException {
        try {
            databaseHelper.close();
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while closing the database.", e);
        }
    }

    public static void importDatabase(Context context, Activity activity) throws FolkSetsException {
        try {
            String selectedFolder = Utilities.readStringFromSharedPreferences(activity, STORAGE_DIRECTORY_URI, null);
            if (selectedFolder != null) {
                databaseHelper.importDatabase(context, selectedFolder);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while importing the database.", e);
        }
    }

    public static void exportDatabase(Context context, Activity activity) throws FolkSetsException {
        try {
            String selectedFolder = Utilities.readStringFromSharedPreferences(activity, STORAGE_DIRECTORY_URI, null);
            if (selectedFolder != null) {
                databaseHelper.exportDatabase(context, selectedFolder);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while exporting the database.", e);
        }
    }

    public static long insertTuneInDatabase(TuneEntity tuneEntity) throws FolkSetsException {
        try {
            long result = databaseHelper.insertTuneInDatabase(sqLiteDatabase, tuneEntity);
            if (result == -1) {
                throw new FolkSetsException("The insert function failed.", null);
            }
            return result;
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while inserting a tune in the database.", e);
        }
    }

    public static void insertTunesInDatabase(List<TuneEntity> tuneEntityList) throws FolkSetsException {
        try {
            if (!tuneEntityList.isEmpty()) {
                databaseHelper.insertTunesInDatabase(sqLiteDatabase, tuneEntityList);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while inserting tunes in the database.", e);
        }
    }

    public static int removeTuneFromDatabase(long tuneId) throws FolkSetsException {
        try {
            return databaseHelper.removeTuneFromDatabase(sqLiteDatabase, tuneId);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while removing a tune from the database.", e);
        }
    }

    public static void removeTunesFromDatabase(List<Long> tuneIds) throws FolkSetsException {
        try {
            if (!tuneIds.isEmpty()) {
                databaseHelper.removeTunesFromDatabase(sqLiteDatabase, tuneIds);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while removing tunes from the database.", e);
        }
    }

    public static int updateTuneInDatabase(TuneEntity tuneEntity) throws FolkSetsException {
        try {
            return databaseHelper.updateTuneInDatabase(sqLiteDatabase, tuneEntity);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while updating a tune in the database.", e);
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

    public static List<TuneEntity> findTuneByIdInDatabase(String fieldsNames, Long tuneId, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findTunesByIdInDatabase(sqLiteDatabase, fieldsNames, new String[]{String.valueOf(tuneId)}, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for tunes in the database.", e);
        }
    }

    public static List<TuneEntity> findTuneByIdInDatabase(String fieldsNames, String tuneId, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findTunesByIdInDatabase(sqLiteDatabase, fieldsNames, new String[]{tuneId}, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for tunes in the database.", e);
        }
    }

    public static List<TuneEntity> findTunesByIdInDatabase(String fieldsNames, String[] tuneIdArray, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findTunesByIdInDatabase(sqLiteDatabase, fieldsNames, tuneIdArray, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for tunes in the database.", e);
        }
    }

    public static List<TuneEntity> findTunesWithValueInListInDatabase(String fieldsNames, String fieldListName, String[] valueArray, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findTunesWithValueInListInDatabase(sqLiteDatabase, fieldsNames, fieldListName, valueArray, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for tunes with tags in the database.", e);
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

    public static Pair<Integer, List<SetEntity>> findSetsWithTunesInDatabase(String tuneTitles, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSetsWithTunesInDatabase(sqLiteDatabase, tuneTitles, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for sets with a tunes title in the database.", e);
        }
    }

    public static List<SetEntity> findSetsWithTunesInDatabase(Long[] tuneId, String sortOnField, String sortDirection) throws FolkSetsException {
        try {
            return databaseHelper.findSetsWithTunesInDatabase(sqLiteDatabase, tuneId, sortOnField, sortDirection);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while looking for sets with a tunes id in the database.", e);
        }
    }

    public static void truncateTable(String tableName) throws FolkSetsException {
        try {
            databaseHelper.truncateTable(sqLiteDatabase, tableName);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while truncating table " + tableName + ".", e);
        }
    }

    public static void removeTuneFromSets(long tuneId) throws FolkSetsException {
        try {
            databaseHelper.removeTuneFromSets(sqLiteDatabase, tuneId);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while removing a tune from sets.", e);
        }
    }

    public static String[] getAllUniqueTitleInTuneTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueTitleInTuneTable(sqLiteDatabase);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique titles from the tune table", e);
        }
    }

    public static String[] getAllUniqueTagInTuneTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueTagInTuneTable(sqLiteDatabase);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique tags from the tune table", e);
        }
    }

    public static String[] getAllUniqueComposerInTuneTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInTuneTable(sqLiteDatabase, TUNE_COMPOSER);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique composers from the tune table", e);
        }
    }

    public static String[] getAllUniqueRegionInTuneTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInTuneTable(sqLiteDatabase, TUNE_REGION_OF_ORIGIN);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique regions of origin from the tune table", e);
        }
    }

    public static String[] getAllUniqueKeyInTuneTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInTuneTable(sqLiteDatabase, TUNE_KEY);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique keys from the tune table", e);
        }
    }

    public static String[] getAllUniqueIncipitInTuneTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInTuneTable(sqLiteDatabase, TUNE_INCIPIT);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique incipits from the tune table", e);
        }
    }

    public static String[] getAllUniqueFormInTuneTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInTuneTable(sqLiteDatabase, TUNE_FORM);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique forms from the tune table", e);
        }
    }

    public static String[] getAllUniquePlayedByInTuneTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniquePlayedByInTuneTable(sqLiteDatabase);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique players from the tune table", e);
        }
    }

    public static String[] getAllUniqueNoteInTuneTable() throws FolkSetsException {
        try {
            return databaseHelper.getAllUniqueValueInTuneTable(sqLiteDatabase, TUNE_NOTE);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while retrieving all unique notes from the tune table", e);
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
