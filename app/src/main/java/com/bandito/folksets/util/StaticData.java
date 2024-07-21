package com.bandito.folksets.util;

import android.graphics.Bitmap;

import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;

import java.util.List;

public class StaticData {

    public static List<TuneEntity> tuneEntityList;
    public static List<SetEntity> setEntityList;
    public static String[] uniqueTuneTitleArray = new String[0];
    public static String[] uniqueTuneTagArray = new String[0];
    public static String[] uniqueTuneComposerArray = new String[0];
    public static String[] uniqueTuneRegionArray = new String[0];
    public static String[] uniqueTuneKeyArray = new String[0];
    public static String[] uniqueTuneIncipitArray = new String[0];
    public static String[] uniqueTuneFormArray = new String[0];
    public static String[] uniqueTunePlayedByArray = new String[0];
    public static String[] uniqueTuneNoteArray = new String[0];
    public static String[] uniqueSetNameArray = new String[0];
    public static List<SetEntity> setsWithTune;
    public static List<Bitmap> bitmapList = null;
    public static TuneEntity nextTune = null;
    public static TuneEntity previousTune = null;
}
