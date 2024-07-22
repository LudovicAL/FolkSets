package com.bandito.folksets;

import static android.view.View.GONE;
import static com.bandito.folksets.util.Constants.CROPPER_DEFAULT_ACTIVATION;
import static com.bandito.folksets.util.Constants.CROPPER_DEFAULT_VALUE;
import static com.bandito.folksets.util.Constants.CROPPER_PREFERED_ACTIVATION_KEY;
import static com.bandito.folksets.util.Constants.CROPPER_PREFERED_VALUE_KEY;
import static com.bandito.folksets.util.Constants.LOGFILE_DEFAULT_ACTIVATION;
import static com.bandito.folksets.util.Constants.LOGFILE_PREFERED_ACTIVATION_KEY;
import static com.bandito.folksets.util.Constants.OPERATION;
import static com.bandito.folksets.util.Constants.STORAGE_DIRECTORY_URI;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bandito.folksets.exception.ExceptionManager;
import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.services.ServiceSingleton;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.IntentLauncher;
import com.bandito.folksets.util.IoUtilities;
import com.bandito.folksets.util.Utilities;

import org.apache.commons.lang3.StringUtils;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SettingsFragment.class.getName();
    private TextView selectStorageDirectoryTextView;
    private ConstraintLayout cropperInnerConstraintLayout;
    private Button exportDatabaseButton;
    private Button importDatabaseButton;
    private ProgressBar exportDatabaseProgressBar;
    private ProgressBar importDatabaseProgressBar;
    private final SettingsFragment.MyBroadcastReceiver myBroadcastReceiver = new SettingsFragment.MyBroadcastReceiver();
    private final CompoundButton.OnCheckedChangeListener cropperActivationSwitchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                Utilities.writeBooleanToSharedPreferences(requireActivity(), CROPPER_PREFERED_ACTIVATION_KEY, isChecked);
                if (cropperInnerConstraintLayout != null) {
                    if (isChecked) {
                        cropperInnerConstraintLayout.setVisibility(View.VISIBLE);
                    } else {
                        cropperInnerConstraintLayout.setVisibility(GONE);
                    }
                }
            } catch (Exception e) {
                ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An error occured while processing cropper switch checked change.", e));
            }
        }
    };
    private final SeekBar.OnSeekBarChangeListener cropperSensitivitySeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try {
                Utilities.writeIntToSharedPreferences(requireActivity(), CROPPER_PREFERED_VALUE_KEY, progress);
            } catch (Exception e) {
                ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing cropper seekbar change.", e));
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private final IntentLauncher<Intent, ActivityResult> intentLauncher = IntentLauncher.registerActivityForResult(this);

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        try {
            selectStorageDirectoryTextView = view.findViewById(R.id.fragment_settings_selectstoragedirectory_textview);
            cropperInnerConstraintLayout = view.findViewById(R.id.fragment_setting_cropperinner_constraintlayout);
            exportDatabaseButton = view.findViewById(R.id.fragment_settings_exportdatabase_button);
            importDatabaseButton = view.findViewById(R.id.fragment_settings_importdatabase_button);
            exportDatabaseProgressBar = view.findViewById(R.id.fragment_settings_exportdatabase_progressbar);
            importDatabaseProgressBar = view.findViewById(R.id.fragment_settings_importdatabase_progressbar);
            exportDatabaseButton.setOnClickListener(this);
            importDatabaseButton.setOnClickListener(this);
            view.findViewById(R.id.fragment_settings_selectstoragedirectory_button).setOnClickListener(this);
            view.findViewById(R.id.fragment_settings_managetags_button).setOnClickListener(this);
            view.findViewById(R.id.fragment_settings_manageplayers_button).setOnClickListener(this);
            updateSelectStorageDirectoryTextView();
            SeekBar seekBar = view.findViewById(R.id.fragment_settings_pdfcropper_seekbar);
            seekBar.setProgress(Utilities.readIntFromSharedPreferences(requireActivity(), CROPPER_PREFERED_VALUE_KEY, CROPPER_DEFAULT_VALUE));
            seekBar.setOnSeekBarChangeListener(cropperSensitivitySeekBarListener);
            SwitchCompat cropperSwitchCompat = view.findViewById(R.id.fragment_setting_croppingactivation_switch);
            cropperSwitchCompat.setChecked(Utilities.readBooleanFromSharedPreferences(requireActivity(), CROPPER_PREFERED_ACTIVATION_KEY, CROPPER_DEFAULT_ACTIVATION));
            cropperSwitchCompat.setOnCheckedChangeListener(cropperActivationSwitchListener);
            cropperSwitchCompat.toggle();
            cropperSwitchCompat.toggle();
            SwitchCompat logFileSwitchCompat = view.findViewById(R.id.fragment_setting_logfile_switch);
            logFileSwitchCompat.setChecked(Utilities.readBooleanFromSharedPreferences(requireActivity(), LOGFILE_PREFERED_ACTIVATION_KEY, LOGFILE_DEFAULT_ACTIVATION));
            logFileSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try {
                        Utilities.writeBooleanToSharedPreferences(requireActivity(), LOGFILE_PREFERED_ACTIVATION_KEY, isChecked);
                        if (isChecked) {
                            IoUtilities.createNewLogFile(requireActivity(), requireContext());
                        }
                    } catch (Exception e) {
                        ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An error occured while processing an OnCheckedCanged event.", e));
                    }
                }
            });
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured during the OnCreateView step of class SettingsFragment.", e, true));
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(myBroadcastReceiver, new IntentFilter(Constants.BroadcastName.importExportUpdate.toString()));
            if (!ServiceSingleton.getInstance().isExportingOrImportingThreadAlive()) {
                exportDatabaseButton.setEnabled(true);
                exportDatabaseProgressBar.setVisibility(GONE);
                importDatabaseButton.setEnabled(true);
                importDatabaseProgressBar.setVisibility(GONE);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while resuming SettingsFragment.", e));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(myBroadcastReceiver);
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while pausing SettingsFragment.", e, true));
        }
    }

    @Override
    public void onDestroy() {
        try {
            ServiceSingleton.getInstance().interruptDatabaseExportingAndImporting();
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An error occured while destroying SettingsFragment.", e, true));
        }
        super.onDestroy();
    }

    private void updateSelectStorageDirectoryTextView() {
        try {
            String directory = Utilities.readStringFromSharedPreferences(requireActivity(), STORAGE_DIRECTORY_URI, null);
            if (StringUtils.isEmpty(directory)) {
                selectStorageDirectoryTextView.setVisibility(GONE);
                exportDatabaseButton.setEnabled(false);
                importDatabaseButton.setEnabled(false);
            } else {
                selectStorageDirectoryTextView.setText(directory);
                selectStorageDirectoryTextView.setVisibility(View.VISIBLE);
                exportDatabaseButton.setEnabled(true);
                importDatabaseButton.setEnabled(true);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while updating the selected storage directory textview.", e));
        }
    }

    public void selectStorageDirectory() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            intentLauncher.launch(intent, activityResult -> selectFolderCallback(activityResult));
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while selecting the storage directory.", e));
        }
    }

    private void selectFolderCallback(ActivityResult activityResult) {
        try {
            if (activityResult.getResultCode() == Activity.RESULT_OK && activityResult.getData() != null) {
                Intent resultData = activityResult.getData();
                Uri uriTree = resultData.getData();
                if (uriTree == null) {
                    Log.e(TAG, "The folder picking intent returned a null object.");
                } else {
                    final int modeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    requireActivity().getContentResolver().takePersistableUriPermission(uriTree, modeFlags);
                    Utilities.writeStringToSharedPreferences(requireActivity(), STORAGE_DIRECTORY_URI, uriTree.toString());
                    updateSelectStorageDirectoryTextView();
                }
            } else {
                throw new FolkSetsException("The folder picking intent failed.", null);
            }
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing the select folder intent callback.", e));
        }
    }

    public void exportDatabaseToStorage() {
        try {
            ServiceSingleton.getInstance().exportOrImportDatabase(requireContext(), requireActivity(), TAG, Constants.ExportOrImport.exportDatabase);
            exportDatabaseProgressBar.setVisibility(View.VISIBLE);
            exportDatabaseButton.setEnabled(false);
            importDatabaseButton.setEnabled(false);
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while exporting the database to storage.", e));
        }
    }

    public void importDatabaseFromStorage() {
        try {
            ServiceSingleton.getInstance().exportOrImportDatabase(requireContext(), requireActivity(), TAG, Constants.ExportOrImport.importDatabase);
            importDatabaseProgressBar.setVisibility(View.VISIBLE);
            exportDatabaseButton.setEnabled(false);
            importDatabaseButton.setEnabled(false);
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while importing the database from storage.", e));
        }
    }

    private void manageTags() throws FolkSetsException {
        Utilities.loadActivity(requireActivity(), requireContext(), ManagementActivity.class, new Pair[]{
                new Pair<>(OPERATION, Constants.ManagementOperation.manageTags)
        });
    }

    private void managePlayers() throws FolkSetsException {
        Utilities.loadActivity(requireActivity(), requireContext(), ManagementActivity.class, new Pair[]{
                new Pair<>(OPERATION, Constants.ManagementOperation.managePlayers)
        });
    }

    @Override
    public void onClick(View view) {
        try {
            if (view.getId() == R.id.fragment_settings_selectstoragedirectory_button) {
                selectStorageDirectory();
            } else if (view.getId() == R.id.fragment_settings_exportdatabase_button) {
                exportDatabaseToStorage();
            } else if (view.getId() == R.id.fragment_settings_importdatabase_button) {
                importDatabaseFromStorage();
            } else if (view.getId() == R.id.fragment_settings_managetags_button) {
                manageTags();
            } else if (view.getId() == R.id.fragment_settings_manageplayers_button) {
                managePlayers();
            }
        } catch (Exception e) {
            ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured while processing an OnClick event.", e));
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Bundle bundle = intent.getExtras();
                if (bundle == null) {
                    ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("The activity receive à broadcast with not extras.", null));
                    return;
                }
                if (bundle.containsKey(Constants.BroadcastKey.exportComplete.toString())) {
                    Toast.makeText(requireContext(), "Export complete", Toast.LENGTH_SHORT).show();
                    exportDatabaseProgressBar.setVisibility(GONE);
                    exportDatabaseButton.setEnabled(true);
                    importDatabaseButton.setEnabled(true);
                }
                if (bundle.containsKey(Constants.BroadcastKey.importComplete.toString())) {
                    Toast.makeText(requireContext(), "Import complete", Toast.LENGTH_SHORT).show();
                    importDatabaseProgressBar.setVisibility(GONE);
                    exportDatabaseButton.setEnabled(true);
                    importDatabaseButton.setEnabled(true);
                }
            } catch (Exception e) {
                ExceptionManager.manageException(requireActivity(), requireContext(), TAG, new FolkSetsException("An exception occured during an OnReceive event.", e));
            }
        }
    }
}