package com.bandito.folksets.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PdfUtilities {

    public static List<Bitmap> convertPdfToBitmapList(Activity activity, Context context, String tag, String filePath) {
        List<Bitmap> bitmapList = new ArrayList<>();
        PdfRenderer pdfRenderer = null;
        ParcelFileDescriptor parcelFileDescriptor = null;
        PdfRenderer.Page page = null;
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(Uri.parse(filePath), "r");
            if (parcelFileDescriptor == null) {
                throw new FolkSetsException("Unable to create the ParcelFileDescriptor object.", null);
            }
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
            for (int i = 0, max = pdfRenderer.getPageCount(); i < max; i++) {
                page = pdfRenderer.openPage(i);
                int width = context.getResources().getDisplayMetrics().widthPixels;
                int height = Math.round(((float) width) / page.getWidth() * page.getHeight());
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                bitmapList.add(bitmap);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(activity, context, tag, new FolkSetsException("An error occured while converting a Pdf file to a list of bitmaps.", e));
        } finally {
            try {
                page.close();
            } catch (Exception e) {
                Log.w(tag, "An error occured while closing a page. This warning is expected.", e);
            }
            try {
                pdfRenderer.close();
            } catch (Exception e) {
                Log.e(tag, "An error occured while closing the PdfRenderer.", e);
            }
            try {
                parcelFileDescriptor.close();
            } catch (Exception e) {
                Log.e(tag, "An error occured while closing the ParcelFileDescriptor.", e);
            }
        }
        return bitmapList;
    }

    public static Bitmap cropWhiteSpace(Bitmap source, int strideSize) throws FolkSetsException {
        try {
            int width = source.getWidth();
            int height = source.getHeight();

            //Get left bound
            int leftBound = 0;
            int[] pixelArray = new int[height];
            for (int i = strideSize; i < width; i += strideSize) {
                source.getPixels(pixelArray, 0, 1, i, 0, 1, height);
                if (Arrays.stream(pixelArray).anyMatch(pixel -> pixel != -1 && pixel != 0)) {
                    break;
                } else {
                    leftBound = i;
                }
            }

            //Get the right bound
            int rightBound = width;
            for (int i = width - strideSize; i > leftBound; i -= strideSize) {
                source.getPixels(pixelArray, 0, 1, i, 0, 1, height);
                if (Arrays.stream(pixelArray).anyMatch(pixel -> pixel != -1 && pixel != 0)) {
                    break;
                } else {
                    rightBound = i;
                }
            }

            //Get the top bound
            int topBound = 0;
            pixelArray = new int[width];
            for (int i = strideSize; i < height; i += strideSize) {
                source.getPixels(pixelArray, 0, width, 0, i, width, 1);
                if (Arrays.stream(pixelArray).anyMatch(pixel -> pixel != -1 && pixel != 0)) {
                    break;
                } else {
                    topBound = i;
                }
            }

            //Get the bottom bound
            int bottomBound = height;
            for (int i = height - strideSize; i > topBound; i -= strideSize) {
                source.getPixels(pixelArray, 0, width, 0, i, width, 1);
                if (Arrays.stream(pixelArray).anyMatch(pixel -> pixel != -1 && pixel != 0)) {
                    break;
                } else {
                    bottomBound = i;
                }
            }

            //Create new cropped bitmap
            Bitmap bitmap = Bitmap.createBitmap(source, leftBound, topBound, rightBound - leftBound , bottomBound - topBound);
            source.recycle();
            return bitmap;
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while cropping white space for pdf rendering.", e);
        }
    }
}
