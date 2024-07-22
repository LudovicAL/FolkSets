package com.bandito.folksets.sql.entities;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;

import com.bandito.folksets.exception.FolkSetsException;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

public class SetEntity implements Serializable {
    public Long setId;
    public String setName;
    public String setTunes;

    public String getTune(int index) throws FolkSetsException {
        try {
            return StringUtils.split(this.setTunes, DEFAULT_SEPARATOR)[index];
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while trying to retrieve tune id at index " + index + " from set.", e);
        }
    }

    public int getTuneCount() throws FolkSetsException {
        try {
            return StringUtils.split(this.setTunes, DEFAULT_SEPARATOR).length;
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while computing the tune count in a set.", e);
        }
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null) {
            return false;
        }
        if (otherObject.getClass() != this.getClass()) {
            return false;
        }
        final SetEntity otherSetEntity = (SetEntity) otherObject;
        if (!Objects.equals(this.setId, otherSetEntity.setId)) {
            return false;
        }
        if (!Objects.equals(this.setName, otherSetEntity.setName)) {
            return false;
        }
        if (!Objects.equals(this.setTunes, otherSetEntity.setTunes)) {
            return false;
        }
        return true;
    }
}
