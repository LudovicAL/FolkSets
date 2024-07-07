package com.bandito.folksets.services;

import static com.bandito.folksets.util.Constants.BITMAP_LIST;
import static com.bandito.folksets.util.Utilities.broadcastMessage;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.bandito.folksets.TuneActivity;
import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.PdfUtilities;
import com.bandito.folksets.util.StaticData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RenderPdfThread  extends Thread {

    private static final String TAG = TuneActivity.class.getName();
    private final Context context;
    private final TuneEntity tuneEntity;
    public RenderPdfThread(Context context, TuneEntity tuneEntity) {
        this.tuneEntity = tuneEntity;
        this.context = context;
    }

    @Override
    public void run() {
        broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressVisibility}, new Integer[]{View.VISIBLE});
        broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{0, "Converting pdf to bitmaps"});
        List<Bitmap> bitmapList = PdfUtilities.convertPdfToBitmapList(context, TAG, tuneEntity.tuneFilePath);
        broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressStepNumber, Constants.BroadcastKey.progressValue, Constants.BroadcastKey.progressHint}, new Serializable[]{bitmapList.size() + 1, 1, "Cropping bitmaps"});
        int progressCurrentStep = 2;
        List<Bitmap> croppedBitmapList = new ArrayList<>();
        try {
            for (Bitmap bitmap : bitmapList) {
                croppedBitmapList.add(PdfUtilities.cropWhiteSpace(bitmap));
                broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressValue}, new Serializable[]{progressCurrentStep++});
            }
        } catch (Exception e) {
            ExceptionManager.manageException(context, e);
        }
        StaticData.bitmapList = croppedBitmapList;
        broadcastMessage(context, Constants.BroadcastName.staticDataUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.staticDataValue}, new String[]{BITMAP_LIST});
        broadcastMessage(context, Constants.BroadcastName.tuneActivityProgressUpdate, new Constants.BroadcastKey[]{Constants.BroadcastKey.progressVisibility}, new Integer[]{View.GONE});
    }
}
