package com.bandito.folksets.sql.entities;

import static com.bandito.folksets.util.Constants.DEFAULT_SEPARATOR;

import static java.util.Objects.isNull;

import com.bandito.folksets.exception.FolkSetsException;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SetEntity implements Serializable {
    public Long setId;
    public String setName;
    public String setSongs;

    public String getSong(int index) throws FolkSetsException {
        try {
            return StringUtils.split(this.setSongs, DEFAULT_SEPARATOR)[index];
        } catch (Exception e) {
            throw new FolkSetsException("An error occured while trying to retrieve song id at index " + index + " from set.", null);
        }
    }
}
