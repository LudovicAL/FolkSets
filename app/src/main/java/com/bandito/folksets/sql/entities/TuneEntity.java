package com.bandito.folksets.sql.entities;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;

import androidx.annotation.NonNull;

import com.bandito.folksets.exception.FolkSetsException;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class TuneEntity implements Serializable {
    public Long tuneId;
    public String tuneTitles;
    public String tuneTags;
    public String tuneComposer;
    public String tuneRegionOfOrigin;
    public String tuneKey;
    public String tuneIncipit;
    public String tuneForm;
    public String tunePlayedBy;
    public String tuneNote;
    public final String tuneFilePath;
    public final String tuneFileType;
    public final String tuneFileCreationDate;
    public String tuneLastConsultationDate;
    public Integer tuneConsultationNumber;

    public TuneEntity(String tuneTitles, String tuneFilePath, String tuneFileType, String tuneFileCreationDate) {
        this.tuneTitles = tuneTitles;
        this.tuneFilePath = tuneFilePath;
        this.tuneFileType = tuneFileType;
        this.tuneFileCreationDate = tuneFileCreationDate;
        this.tuneConsultationNumber = 0;
    }

    public String getFirstTitle() throws FolkSetsException {
        try {
            return StringUtils.split(this.tuneTitles, DEFAULT_SEPARATOR)[0];
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while trying to read the first title of a tune.", null);
        }
    }

    @NonNull
    public String toString() {
        try {
            return getFirstTitle();
        } catch (Exception e) {
            return tuneTitles;
        }
    }
}
