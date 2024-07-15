package com.bandito.folksets.util;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;
import static com.bandito.folksets.util.Constants.DELIMITER_INPUT_PATTERN;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class ChipGroupUtilities {

    public static void addChipsToChipGroup(Context context, String[] chipContentArray, ChipGroup chipGroup) {
        if (chipContentArray == null) {
            return;
        }
        for (String chipContent : chipContentArray) {
            Chip chip = new Chip(context);
            chip.setText(chipContent);
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chipGroup.addView(chip);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chipGroup.removeView(view);
                }
            });
        }
    }

    public static String retrieveChipsFromChipGroup(ChipGroup chipGroup) {
        StringBuilder stringBuilder = new StringBuilder();
        List<CharSequence> tagList = new ArrayList<>();
        for (int i = 0, max = chipGroup.getChildCount(); i < max; i++) {
            CharSequence chipValue = ((Chip)chipGroup.getChildAt(i)).getText();
            if (!tagList.contains(chipValue)) {
                tagList.add(chipValue);
                stringBuilder.append(chipValue);
                if (i < max - 1) {
                    stringBuilder.append(DEFAULT_SEPARATOR);
                }
            }
        }
        return stringBuilder.toString();
    }


    public static class CustomTextWatcher implements TextWatcher {
        private final Context context;
        private final AutoCompleteTextView autoCompleteTextView;
        private final ChipGroup chipGroup;
        public CustomTextWatcher(Context context, AutoCompleteTextView autoCompleteTextView, ChipGroup chipGroup) {
            this.context = context;
            this.autoCompleteTextView = autoCompleteTextView;
            this.chipGroup = chipGroup;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (DELIMITER_INPUT_PATTERN.matcher(charSequence).find()) {
                String sanitizedString = charSequence.toString().replace(DEFAULT_SEPARATOR, "");
                if (!sanitizedString.isEmpty()) {
                    addChipsToChipGroup(context, new String[]{sanitizedString}, chipGroup);
                }
                autoCompleteTextView.setText("");
            }
        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public static class CustomOnItemClickListener implements AdapterView.OnItemClickListener {
        private Context context;
        private AutoCompleteTextView autoCompleteTextView;
        private ChipGroup chipGroup;
        public CustomOnItemClickListener(Context context, AutoCompleteTextView autoCompleteTextView, ChipGroup chipGroup) {
            this.context = context;
            this.autoCompleteTextView = autoCompleteTextView;
            this.chipGroup = chipGroup;
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            addChipsToChipGroup(context, new String[]{(String)parent.getAdapter().getItem(position)}, chipGroup);
            autoCompleteTextView.setText("");
        }
    }
}
