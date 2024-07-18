package com.bandito.folksets.util;

import static com.bandito.folksets.util.Constants.PREFERENCES_NAME;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.TuneEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static void writeBooleanToSharedPreferences(Activity activity, String key, boolean value) throws FolkSetsException {
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putBoolean(key, value);
            sharedPreferencesEditor.commit();
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while writing a boolean to Shared Preferences.", e);
        }
    }

    public static boolean readBooleanFromSharedPreferences(Activity activity, String key, boolean defaultValue) throws FolkSetsException {
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getBoolean(key, defaultValue);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while reading a boolean from Shared Preferences.", e);
        }
    }

    public static void writeIntToSharedPreferences(Activity activity, String key, int value) throws FolkSetsException {
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putInt(key, value);
            sharedPreferencesEditor.commit();
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while writing an int to Shared Preferences.", e);
        }
    }

    public static int readIntFromSharedPreferences(Activity activity, String key, int defaultValue) throws FolkSetsException {
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getInt(key, defaultValue);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while reading an int from Shared Preferences.", e);
        }
    }

    public static void writeStringToSharedPreferences(Activity activity, String key, String value) throws FolkSetsException {
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
            sharedPreferencesEditor.putString(key, value);
            sharedPreferencesEditor.commit();
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while writing a String to Shared Preferences.", e);
        }
    }

    public static String readStringFromSharedPreferences(Activity activity, String key, String defaultValue) throws FolkSetsException {
        try {
            SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(key, defaultValue);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while reading a String from Shared Preferences.", e);
        }
    }

    public static void assertObjectIsNotNull(String objectName, Object object) throws FolkSetsException {
        if (object == null) {
            throw new FolkSetsException("The object " + objectName + " passed as parameter is null.", null);
        }
    }

    public static void loadActivity(Activity callingActivity, Context context, Class<?> calledActivityClass, Pair<String, ? extends Serializable>[] messages) throws FolkSetsException {
        try {
            Intent intent = new Intent(context, calledActivityClass);
            Bundle bundle = new Bundle();
            if (messages != null) {
                for (Pair<String, ? extends Serializable> pair : messages) {
                    bundle.putSerializable(pair.first, pair.second);
                }
            }
            intent.putExtras(bundle);
            callingActivity.startActivity(intent);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while loading an Activity.", e);
        }
    }

    public static List<TuneEntity> rearangeTuneInSetOrder(List<TuneEntity> unorderedSetTuneEntityList, String[] tuneIdsInSetOrder) throws FolkSetsException {
        try {
            List<TuneEntity> orderedSetTuneEntityList = new ArrayList<>();
            for (String tuneId : tuneIdsInSetOrder) {
                orderedSetTuneEntityList.add(unorderedSetTuneEntityList.stream().filter(tuneEntity -> tuneEntity.tuneId.equals(Long.valueOf(tuneId))).findFirst().get());
            }
            return orderedSetTuneEntityList;
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while rearanging tunes in set order.", e);
        }
    }

    public static void broadcastMessage(Context context, Constants.BroadcastName intentName, Constants.BroadcastKey[] messageKey, Serializable[] messageValue) throws FolkSetsException {
        try {
            Intent intent = new Intent(intentName.toString());
            for (int i = 0, max = messageKey.length; i < max; i++) {
                intent.putExtra(messageKey[i].toString(), messageValue[i]);
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while broadcasting a message.", e);
        }
    }

    public static String convertExceptionToString(Exception exception) throws FolkSetsException {
        try {
            StringBuilder stringBuilder = new StringBuilder("\nException toString(): " + exception
                    + "\n----------------------------------------------------------------------"
                    + "\nException message: " + exception.getMessage()
                    + "\n----------------------------------------------------------------------"
                    + "\nException simple class name: " + exception.getClass().getSimpleName()
                    + "\n----------------------------------------------------------------------"
                    + "\nException cause: " + exception.getCause()
                    + "\n----------------------------------------------------------------------"
                    + "\nException stacktrace: ");
            StackTraceElement[] stackTrace = exception.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                stringBuilder.append("\n    ").append(stackTraceElement.toString());
            }
            stringBuilder.append("\n\n\n\n\n\n");
            return stringBuilder.toString();
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while converting an exception to String.", e);
        }
    }

    //The following strange bit of code make it so EditText loose the focus when we touch outside them.
    //It applies even in fragments that are "children" of this activity.
    public static void dispatchTouchEvent(MotionEvent motionEvent, View view, InputMethodManager inputMethodManager) throws FolkSetsException {
        try {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                if (view instanceof EditText) {
                    Rect outRect = new Rect();
                    view.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                        view.clearFocus();
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        } catch (Exception e) {
            throw new FolkSetsException("An exception occured while processing a dispatchTouchEvent demand.", e);
        }
    }
}
