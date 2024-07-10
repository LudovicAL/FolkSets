package com.bandito.folksets.services;

import static com.bandito.folksets.util.Constants.BITMAP_LIST;
import static com.bandito.folksets.util.Constants.PREVIOUS_AND_NEXT_TUNE;
import static com.bandito.folksets.util.Constants.TUNE_TITLES;
import static com.bandito.folksets.util.Utilities.broadcastMessage;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.bandito.folksets.TuneActivity;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.PdfUtilities;
import com.bandito.folksets.util.StaticData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RenderPdfAndGetPreviousAndNextTuneThread extends Thread {

    private static final String TAG = TuneActivity.class.getName();
    private final Context context;
    private final TuneEntity tuneEntity;
    private final SetEntity setEntity;
    private final int position;
    private final Constants.TuneOrSet tuneOrSet;
    public RenderPdfAndGetPreviousAndNextTuneThread(Context context, TuneEntity tuneEntity, SetEntity setEntity, int position, Constants.TuneOrSet tuneOrSet) {
        this.context = context;
        this.tuneEntity = tuneEntity;
        this.setEntity = setEntity;
        this.position = position;
        this.tuneOrSet = tuneOrSet;
    }

    @Override
    public void run() {
        try {
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressVisibility}, new Integer[]{View.VISIBLE});
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{0, "Converting pdf to bitmaps"});
            List<Bitmap> bitmapList = PdfUtilities.convertPdfToBitmapList(context, TAG, tuneEntity.tuneFilePath);
            int maxNumberOfSteps = bitmapList.size() + 2;
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressStepNumber, Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{maxNumberOfSteps, 1, "Cropping bitmaps"});
            int progressCurrentStep = 2;
            List<Bitmap> croppedBitmapList = new ArrayList<>();
            for (Bitmap bitmap : bitmapList) {
                croppedBitmapList.add(PdfUtilities.cropWhiteSpace(bitmap));
                broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue}, new Serializable[]{progressCurrentStep++});
            }
            StaticData.bitmapList = croppedBitmapList;
            broadcastMessage(context, Constants.BroadcastName.staticDataUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.staticDataValue}, new String[]{BITMAP_LIST});
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressHint}, new Serializable[]{"Loading previous and next tune"});
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
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{maxNumberOfSteps, "Loading complete"});
            sleep(3000L);
            broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressVisibility}, new Serializable[]{View.GONE});
        } catch (Exception e) {
            ExceptionManager.manageException(context, new FolkSetsException("An exception occured while executing the thread that renders Pdf and determines previous and next tunes.", e));
        }
    }
}
