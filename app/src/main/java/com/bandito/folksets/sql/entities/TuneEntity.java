package com.bandito.folksets.sql.entities;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;

import androidx.annotation.NonNull;

import com.bandito.folksets.exception.FolkSetsException;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public boolean hasTag(String tag) {
        if (tuneTags == null) {
            return false;
        }
        return Arrays.asList(StringUtils.split(tuneTags, DEFAULT_SEPARATOR)).contains(tag);
    }

    public boolean hasPlayer(String player) {
        if (tunePlayedBy == null) {
            return false;
        }
        return Arrays.asList(StringUtils.split(tunePlayedBy, DEFAULT_SEPARATOR)).contains(player);
    }

    public void addTag(String tag) {
        if (tuneTags == null) {
            tuneTags = tag;
        } else {
            String[] tagArray = StringUtils.split(tuneTags, DEFAULT_SEPARATOR);
            if (Arrays.stream(tagArray).noneMatch(t -> t.equals(tag))) {
                tuneTags = StringUtils.joinWith(DEFAULT_SEPARATOR, tuneTags, tag);
            }
        }
    }

    public void removeTag(String tag) {
        if (tuneTags != null) {
            List<String> tagList = Arrays.stream(StringUtils.split(tuneTags, DEFAULT_SEPARATOR)).collect(Collectors.toList());
            for (int i = tagList.size() - 1; i >= 0; i--) {
                if (tagList.get(i).equals(tag)) {
                    tagList.remove(i);
                }
            }
            tuneTags = String.join(DEFAULT_SEPARATOR, tagList);
        }
    }

    public void addPlayer(String player) {
        if (tunePlayedBy == null) {
            tunePlayedBy = player;
        } else {
            String[] playerArray = StringUtils.split(tunePlayedBy, DEFAULT_SEPARATOR);
            if (Arrays.stream(playerArray).noneMatch(t -> t.equals(player))) {
                tunePlayedBy = StringUtils.joinWith(DEFAULT_SEPARATOR, tunePlayedBy, player);
            }
        }
    }

    public void removePlayer(String player) {
        if (tunePlayedBy != null) {
            List<String> playerList = Arrays.stream(StringUtils.split(tunePlayedBy, DEFAULT_SEPARATOR)).collect(Collectors.toList());
            for (int i = playerList.size() - 1; i >= 0; i--) {
                if (playerList.get(i).equals(player)) {
                    playerList.remove(i);
                }
            }
            tunePlayedBy = String.join(DEFAULT_SEPARATOR, playerList);
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
