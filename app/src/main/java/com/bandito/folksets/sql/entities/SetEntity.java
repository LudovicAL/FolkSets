package com.bandito.folksets.sql.entities;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;

import com.bandito.folksets.exception.FolkSetsException;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class SetEntity implements Serializable {
    public Long setId;
    public String setName;
    public String setTunes;

    public String getTune(int index) throws FolkSetsException {
        try {
            return StringUtils.split(this.setTunes, DEFAULT_SEPARATOR)[index];
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while trying to retrieve tune id at index " + index + " from set.", null);
        }
    }
}
