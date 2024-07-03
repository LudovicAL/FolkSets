package com.bandito.folksets.sql.entities;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;

import com.bandito.folksets.exception.FolkSetsException;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class SongEntity implements Serializable {
    public Long songId;
    public String songTitles;
    public String songTags;
    public String songComposer;
    public String songRegionOfOrigin;
    public String songKey;
    public String songIncipit;
    public String songForm;
    public String songPlayedBy;
    public String songNote;
    public String songFilePath;
    public String songFileType;
    public String songFileCreationDate;
    public String songLastConsultationDate;
    public Integer songConsultationNumber;

    public SongEntity(String songTitles, String songFilePath, String songFileType, String songFileCreationDate) {
        this.songTitles = songTitles;
        this.songFilePath = songFilePath;
        this.songFileType = songFileType;
        this.songFileCreationDate = songFileCreationDate;
        this.songConsultationNumber = 0;
    }

    public String getFirstTitle() throws FolkSetsException {
        try {
            return StringUtils.split(this.songTitles, DEFAULT_SEPARATOR)[0];
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while trying to read the first title of a song.", null);
        }
    }
}
