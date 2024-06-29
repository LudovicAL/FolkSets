package com.bandito.folksets;

import static java.util.Objects.isNull;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.IntentLauncher;
import com.bandito.folksets.util.Utilities;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private TextView storageDirectorySelectionTextView;

    protected final IntentLauncher<Intent, ActivityResult> intentLauncher = IntentLauncher.registerActivityForResult(this);

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        view.findViewById(R.id.settingsSelectStorageDirectoryButton).setOnClickListener(this);
        view.findViewById(R.id.settingExportDatabaseButton).setOnClickListener(this);
        view.findViewById(R.id.settingImportDatabaseButton).setOnClickListener(this);
        storageDirectorySelectionTextView = view.findViewById(R.id.settingStorageDirectoryTextView);
        updateSelectStorageDirectoryTextView(null);
        return view;
    }

    private void updateSelectStorageDirectoryTextView(String text) {
        if (isNull(text)) {
            text = Utilities.readStringFromSharedPreferences(requireActivity(), Constants.STORAGE_DIRECTORY_URI, getString (R.string.selectStorageDirectory));
        }
        storageDirectorySelectionTextView.setText(text);
    }

    public void selectStorageDirectory() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intentLauncher.launch(intent, activityResult -> selectFolderCallback(activityResult));
    }

    private void selectFolderCallback(ActivityResult activityResult) {
        if (activityResult.getResultCode() == Activity.RESULT_OK && activityResult.getData() != null) {
            Intent resultData = activityResult.getData();
            Uri uriTree = resultData.getData();
            if (isNull(uriTree)) {
                Log.e("FolderPicking", "The folder picking intent returned a null object.");
            } else {
                final int modeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                requireActivity().getContentResolver().takePersistableUriPermission(uriTree, modeFlags);
                Utilities.writeStringToSharedPreferences(requireActivity(), Constants.STORAGE_DIRECTORY_URI, uriTree.toString());
                updateSelectStorageDirectoryTextView(null);
            }
        } else {
            Log.e("FolderPicking", "The folder picking intent failed.");
        }
    }

    public void exportDatabaseToStorage() {
        try {
            DatabaseManager.exportDatabase(requireContext(), requireActivity());
            Toast.makeText(requireContext(), "Exportation complete", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "An error occured", Toast.LENGTH_SHORT).show();
            Log.e("Export", "An error occured when exporting the database to storage.", e);
        }
    }

    public void importDatabaseFromStorage() {
        try {
            DatabaseManager.importDatabase(requireContext(), requireActivity());
            Toast.makeText(requireContext(), "Importation complete", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "An error occured", Toast.LENGTH_SHORT).show();
            Log.e("Import", "An error occured when importing the database from storage.", e);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() ==  R.id.settingsSelectStorageDirectoryButton) {
            selectStorageDirectory();
        } else if (view.getId() ==  R.id.settingExportDatabaseButton) {
            exportDatabaseToStorage();
        } else if (view.getId() ==  R.id.settingImportDatabaseButton) {
            importDatabaseFromStorage();
        }
    }
}