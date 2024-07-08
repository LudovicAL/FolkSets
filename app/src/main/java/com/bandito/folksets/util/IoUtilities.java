package com.bandito.folksets.util;

import static com.bandito.folksets.util.Constants.STORAGE_DIRECTORY_URI;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.bandito.folksets.exception.FolkSetsException;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IoUtilities {

    private static final String TAG = IoUtilities.class.getName();
    public static void assertDirectoryExist(Context context, String uriFromTreeAsString) throws FolkSetsException {
        Utilities.assertObjectIsNotNull("uriFromTreeAsString", uriFromTreeAsString);
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, Uri.parse(uriFromTreeAsString));
        if (documentFile == null || !documentFile.exists()) {
            throw new FolkSetsException("The directory does not exist: " + uriFromTreeAsString, null);
        }
    }

    public static void assertFileExist(File file) throws FolkSetsException {
        Utilities.assertObjectIsNotNull("file" , file);
        if (!file.exists()) {
            throw new FolkSetsException("The file does not exist: " + file.getName(), null);
        }
    }

    public static void copySourceToDestinationFile(Context context, String sourceDirectoryName, File destinationFile, String sourceFileName) throws FolkSetsException {
        InputStream inputStream = null;
        FileChannel destinationFileChannel = null;
        try {
            DocumentFile sourceDirectory = DocumentFile.fromTreeUri(context, Uri.parse(sourceDirectoryName));
            DocumentFile sourceFile = sourceDirectory.findFile(sourceFileName);
            Uri sourceFileUri = sourceFile.getUri();
            inputStream = context.getContentResolver().openInputStream(sourceFileUri);
            destinationFileChannel = new FileOutputStream(destinationFile).getChannel();
            byte[] buffer = new byte[1024];
            while ((inputStream.read(buffer)) != -1) {
                destinationFileChannel.write(ByteBuffer.wrap(buffer));
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while exporting the database.", e);
        } finally {
            closeCloseable(destinationFileChannel);
            closeCloseable(inputStream);
        }
    }

    public static void copySourceFileToDestination(Context context, File sourceFile, String destinationDirectoryName, String destinationFileName, String destinationFileMimeType) throws FolkSetsException {
        FileChannel sourceFileChannel = null;
        OutputStream outputStream = null;
        try {
            DocumentFile destinationDirectory = DocumentFile.fromTreeUri(context, Uri.parse(destinationDirectoryName));
            DocumentFile destinationFile = destinationDirectory.findFile(destinationFileName);
            if (destinationFile != null) {
                destinationFile.delete();
            }
            destinationFile = destinationDirectory.createFile(destinationFileMimeType, destinationFileName);
            Uri destinationFileUri = destinationFile.getUri();
            outputStream = context.getContentResolver().openOutputStream(destinationFileUri);
            sourceFileChannel = new FileInputStream(sourceFile).getChannel();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = sourceFileChannel.read(ByteBuffer.wrap(buffer))) != -1) {
                outputStream.write(buffer, 0, read);
            }
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while copying source file to destination.", e);
        } finally {
            closeCloseable(sourceFileChannel);
            flushFlushable(outputStream);
            closeCloseable(outputStream);
        }
    }

    public static void flushFlushable(Flushable flushable) {
        try {
            if(flushable != null) {
                flushable.flush();
            }
        } catch (Exception e) {
            Log.e(TAG, "An error occured while flushing a Flushable: " + flushable.getClass(), e);
        }

    }

    public static void closeCloseable(Closeable closeable) {
        try {
            if(closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "An error occured while closing a Closable: " + closeable.getClass(), e);
        }
    }

    public static DocumentFile[] listFilesFromDirectory(Context context, String directory) throws FolkSetsException {
        try {
            DocumentFile documentFile = DocumentFile.fromTreeUri(context, Uri.parse(directory));
            return documentFile.listFiles();
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while listing files from directory.", e);
        }
    }

    public static List<DocumentFile> listPdfFilesFromStorage(Context context, Activity activity) throws FolkSetsException {
        try {
            DocumentFile[] documentFileArray = listFilesFromDirectory(context, Utilities.readStringFromSharedPreferences(activity, STORAGE_DIRECTORY_URI, ""));
            return Arrays.stream(documentFileArray).filter(documentFile -> "application/pdf".equals(documentFile.getType())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new FolkSetsException("An error occured withh listing pdf from storage.", e);
        }
    }
}
