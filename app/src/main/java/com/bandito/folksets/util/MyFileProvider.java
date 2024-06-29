package com.bandito.folksets.util;

import androidx.core.content.FileProvider;

import com.bandito.folksets.R;

public class MyFileProvider extends FileProvider {
    public MyFileProvider() {
        super(R.xml.file_paths);
    }
}
