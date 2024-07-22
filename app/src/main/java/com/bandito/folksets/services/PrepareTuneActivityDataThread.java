package com.bandito.folksets.services;

import static com.bandito.folksets.util.Constants.BITMAP_LIST;
import static com.bandito.folksets.util.Constants.CROPPER_DEFAULT_ACTIVATION;
import static com.bandito.folksets.util.Constants.CROPPER_DEFAULT_VALUE;
import static com.bandito.folksets.util.Constants.CROPPER_PREFERED_ACTIVATION_KEY;
import static com.bandito.folksets.util.Constants.CROPPER_PREFERED_VALUE_KEY;
import static com.bandito.folksets.util.Constants.PREVIOUS_AND_NEXT_TUNE;
import static com.bandito.folksets.util.Constants.SETS_WITH_TUNE;
import static com.bandito.folksets.util.Constants.SET_NAME;
import static com.bandito.folksets.util.Constants.TUNE_TITLES;
import static com.bandito.folksets.util.Utilities.broadcastMessage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.PdfUtilities;
import com.bandito.folksets.util.StaticData;
import com.bandito.folksets.util.Utilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PrepareTuneActivityDataThread extends Thread {

    private static final String TAG = PrepareTuneActivityDataThread.class.getName();
    private final Activity activity;
    private final Context context;
    private final TuneEntity tuneEntity;
    private final SetEntity setEntity;
    private final int position;
    private final Constants.TuneOrSet tuneOrSet;
    private final boolean isCropperActivated;
    private int cropperStrideSize = CROPPER_DEFAULT_VALUE;

    public PrepareTuneActivityDataThread(Context context, Activity activity, TuneEntity tuneEntity, SetEntity setEntity, int position, Constants.TuneOrSet tuneOrSet) throws FolkSetsException {
        try {
            this.context = context;
            this.activity = activity;
            this.tuneEntity = tuneEntity;
            this.setEntity = setEntity;
            this.position = position;
            this.tuneOrSet = tuneOrSet;
            this.isCropperActivated = Utilities.readBooleanFromSharedPreferences(activity, CROPPER_PREFERED_ACTIVATION_KEY, CROPPER_DEFAULT_ACTIVATION);
            if (isCropperActivated) {
                this.cropperStrideSize = Utilities.readIntFromSharedPreferences(activity, CROPPER_PREFERED_VALUE_KEY, CROPPER_DEFAULT_VALUE);
            }
        } catch (Exception e ) {
            throw new FolkSetsException("An exception occured while constructing a PrepareTuneActivityDataThread object.", e);
        }
    }

    @Override
    public void run() {
        try {
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressVisibility, Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{View.VISIBLE, 0, "Converting pdf to bitmaps"});
            List<Bitmap> bitmapList = PdfUtilities.convertPdfToBitmapList(activity, context, TAG, tuneEntity.tuneFilePath);
            int maxNumberOfSteps = bitmapList.size() + 4;
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressStepNumber, Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{maxNumberOfSteps, 1, "Cropping bitmaps"});
            int progressCurrentStep = 2;
            if (isCropperActivated) {
                List<Bitmap> croppedBitmapList = new ArrayList<>();
                for (Bitmap bitmap : bitmapList) {
                    croppedBitmapList.add(PdfUtilities.cropWhiteSpace(bitmap, cropperStrideSize));
                }
                StaticData.bitmapList = croppedBitmapList;
            } else {
                StaticData.bitmapList = bitmapList;
            }
            broadcastMessage(context, Constants.BroadcastName.staticDataUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.staticDataValue}, new String[]{BITMAP_LIST});
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{progressCurrentStep++, "Loading previous and next tune"});
            if (tuneOrSet == Constants.TuneOrSet.set) {
                if (position > 0) {
                    StaticData.previousTune = DatabaseManager.findTuneByIdInDatabase("*", setEntity.getTune(position - 1), null, null).get(0);
                } else {
                    StaticData.previousTune = null;
                }
                if (position < setEntity.getTuneCount() - 1) {
                    StaticData.nextTune = DatabaseManager.findTuneByIdInDatabase("*", setEntity.getTune(position + 1), null, null).get(0);
                } else {
                    StaticData.nextTune = null;
                }
            } else {
                List<TuneEntity> tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase("*", null, null, TUNE_TITLES, null);
                int tuneEntityListSize = tuneEntityList.size();
                int i = 0;
                for (; i < tuneEntityListSize; i++) {
                    if (tuneEntityList.get(i).tuneId.equals(tuneEntity.tuneId)) {
                        break;
                    }
                }
                if (i > 0) {
                    StaticData.previousTune = tuneEntityList.get(i - 1);
                } else {
                    StaticData.previousTune = null;
                }
                if (i < tuneEntityListSize - 1) {
                    StaticData.nextTune = tuneEntityList.get(i + 1);
                } else {
                    StaticData.nextTune = null;
                }
            }
            broadcastMessage(context, Constants.BroadcastName.staticDataUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.staticDataValue}, new String[]{PREVIOUS_AND_NEXT_TUNE});
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{progressCurrentStep++, "Loading sets with tune"});
            StaticData.setsWithTune = DatabaseManager.findSetsWithTunesInDatabase(new Long[]{tuneEntity.tuneId}, SET_NAME, null);
            broadcastMessage(context, Constants.BroadcastName.staticDataUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.staticDataValue}, new String[]{SETS_WITH_TUNE});
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{progressCurrentStep++, "Loading consultation update"});
            tuneEntity.tuneConsultationNumber++;
            DatabaseManager.updateTuneInDatabase(tuneEntity);
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{maxNumberOfSteps, "Loading complete"});
            sleep(3000L);
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressVisibility}, new Serializable[]{View.GONE});
        } catch (Exception e) {
            ExceptionManager.manageException(activity, context, TAG, new FolkSetsException("An exception occured while executing the thread that renders Pdf and determines previous and next tunes.", e));
        }
    }
}
