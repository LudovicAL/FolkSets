package com.bandito.folksets.util;

import static com.bandito.folksets.util.Constants.PREFERENCES_NAME;
import static java.util.Objects.isNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.util.Pair;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.SongEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static void writeStringToSharedPreferences(Activity activity, String key, String value) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(key, value);
        sharedPreferencesEditor.commit();
    }

    public static String readStringFromSharedPreferences(Activity activity, String key, String defaultValue) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void assertObjectIsNotNull(String objectName, Object object) throws FolkSetsException {
        if (isNull(object)) {
            throw new FolkSetsException("The object " + objectName + " passed as parameter is null.", null);
        }
    }

    public static void loadActivity(Activity callingActivity, Context context, Class<?> calledActivityClass, Pair<String, ? extends Serializable>[] messages) {
        Intent intent = new Intent(context, calledActivityClass);
        Bundle bundle = new Bundle();
        if (!isNull(messages)) {
            for (Pair<String, ? extends Serializable> pair : messages) {
                bundle.putSerializable(pair.first, pair.second);
            }
        }
        intent.putExtras(bundle);
        callingActivity.startActivity(intent);
    }

    public static List<SongEntity> rearangeSongInSetOrder(List<SongEntity> unorderedSetSongEntityList, String[] songIdsInSetOrder) {
        List<SongEntity> orderedSetSongEntityList = new ArrayList<>();
        for (String songId : songIdsInSetOrder) {
            orderedSetSongEntityList.add(unorderedSetSongEntityList.stream().filter(songEntity -> songEntity.songId.equals(Long.valueOf(songId))).findFirst().get());
        }
        return orderedSetSongEntityList;
    }
}
