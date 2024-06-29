package com.bandito.folksets.util;

public class Constants {
    public static final String STORAGE_DIRECTORY_URI = "storageDirectoryUri";

    public static final String PREFERENCES_NAME = "FolkSetsPreferences";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FolkSets.db";
    public static final String TABLE_SONG = "table_song";
    public static final String SONG_ID = "song_id";
    public static final String SONG_TITLES = "song_titles";
    public static final String SONG_TAGS = "song_tags";
    public static final String SONG_FILE_PATH = "song_file_path";
    public static final String SONG_FILE_TYPE = "song_file_type";
    public static final String SONG_COMPOSER = "song_composer";
    public static final String SONG_REGION_OF_ORIGIN = "song_region_of_origin";
    public static final String SONG_KEY = "song_key";
    public static final String SONG_INCIPIT = "song_incipit";
    public static final String SONG_FORM = "song_form";
    public static final String SONG_PLAYED_BY = "song_played_by";
    public static final String SONG_NOTE = "song_note";
    public static final String SONG_FILE_CREATION_DATE = "song_file_creation_date";
    public static final String SONG_LAST_CONSULTATION_DATE = "song_last_consultation_date";
    public static final String SONG_CONSULTATION_NUMBER = "song_consultation_number";
    public static final String TABLE_SET = "table_set";
    public static final String SET_ID = "set_id";
    public static final String SET_NAME = "set_name";
    public static final String SET_SONGS = "set_song_ids";
    public static final String DEFAULT_SEPARATOR = ";";
    public static final String SONG_ENTITY = "songEntity";
    public static final String SET_ENTITY = "setEntity";
    public static final String CLICK_TYPE = "clickType";
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";
    public enum ClickType {
        shortClick,
        longClick
    }
}
