package com.bandito.folksets.util;

import java.util.regex.Pattern;

public class Constants {
    public static final String UNIQUE_VALUES = "Unique values";
    public static final String TUNE_ENTITY_LIST = "tuneEntityList";
    public static final String SET_ENTITY_LIST = "setEntityList";
    public static final String BITMAP_LIST = "bitmapList";
    public static final String STORAGE_DIRECTORY_URI = "storageDirectoryUri";

    public static final String PREFERENCES_NAME = "FolkSetsPreferences";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FolkSets.db";
    public static final String TABLE_TUNE = "table_tune";
    public static final String TUNE_ID = "tune_id";
    public static final String TUNE_TITLES = "tune_titles";
    public static final String TUNE_TAGS = "tune_tags";
    public static final String TUNE_FILE_PATH = "tune_file_path";
    public static final String TUNE_FILE_TYPE = "tune_file_type";
    public static final String TUNE_COMPOSER = "tune_composer";
    public static final String TUNE_REGION_OF_ORIGIN = "tune_region_of_origin";
    public static final String TUNE_KEY = "tune_key";
    public static final String TUNE_INCIPIT = "tune_incipit";
    public static final String TUNE_FORM = "tune_form";
    public static final String TUNE_PLAYED_BY = "tune_played_by";
    public static final String TUNE_NOTE = "tune_note";
    public static final String TUNE_FILE_CREATION_DATE = "tune_file_creation_date";
    public static final String TUNE_LAST_CONSULTATION_DATE = "tune_last_consultation_date";
    public static final String TUNE_CONSULTATION_NUMBER = "tune_consultation_number";
    public static final String TABLE_SET = "table_set";
    public static final String SET_ID = "set_id";
    public static final String SET_NAME = "set_name";
    public static final String SET_TUNES = "set_tune_ids";
    public static final String DEFAULT_SEPARATOR = ";";
    public static final String TUNE_ENTITY = "tuneEntity";
    public static final String SET_ENTITY = "setEntity";
    public static final String OPERATION = "operation";
    public static final String CLICK_TYPE = "clickType";
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";
    public static final String POSITION = "Position";
    public static final Pattern DELIMITER_INPUT_PATTERN = Pattern.compile("([\\n;])$", Pattern.CASE_INSENSITIVE);
    public enum ClickType {
        shortClick,
        longClick
    }

    public enum SetOperation {
        createSet,
        editSet
    }

    public enum TuneOrSet {
        tune,
        set
    }

    public enum BroadcastName {
        mainActivityProgressUpdate,
        tuneActivityProgressUpdate,
        staticDataUpdate
    }

    public enum BroadcastKey {
        progressVisibility,
        progressHint,
        progressStepNumber,
        progressValue,
        staticDataValue
    }
}
