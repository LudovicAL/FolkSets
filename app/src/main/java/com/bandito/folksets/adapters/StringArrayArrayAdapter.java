package com.bandito.folksets.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

public class StringArrayArrayAdapter extends ArrayAdapter<String> {

    public StringArrayArrayAdapter(@NonNull Context context, int resource, @NonNull String[] stringList) {
        super(context, resource, stringList);
    }
}
