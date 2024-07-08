package com.bandito.folksets.util;

import static com.bandito.folksets.util.Constants.PREFERENCES_NAME;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.TuneEntity;

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
        if (object == null) {
            throw new FolkSetsException("The object " + objectName + " passed as parameter is null.", null);
        }
    }

    public static void loadActivity(Activity callingActivity, Context context, Class<?> calledActivityClass, Pair<String, ? extends Serializable>[] messages) {
        Intent intent = new Intent(context, calledActivityClass);
        Bundle bundle = new Bundle();
        if (messages != null) {
            for (Pair<String, ? extends Serializable> pair : messages) {
                bundle.putSerializable(pair.first, pair.second);
            }
        }
        intent.putExtras(bundle);
        callingActivity.startActivity(intent);
    }

    public static List<TuneEntity> rearangeTuneInSetOrder(List<TuneEntity> unorderedSetTuneEntityList, String[] tuneIdsInSetOrder) {
        List<TuneEntity> orderedSetTuneEntityList = new ArrayList<>();
        for (String tuneId : tuneIdsInSetOrder) {
            orderedSetTuneEntityList.add(unorderedSetTuneEntityList.stream().filter(tuneEntity -> tuneEntity.tuneId.equals(Long.valueOf(tuneId))).findFirst().get());
        }
        return orderedSetTuneEntityList;
    }

    public static void broadcastMessage(Context context, Constants.BroadcastName intentName, Constants.BroadcastKey[] messageKey, Serializable[] messageValue) {
        Intent intent = new Intent(intentName.toString());
        for (int i = 0, max = messageKey.length; i < max; i++) {
            intent.putExtra(messageKey[i].toString(), messageValue[i]);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
