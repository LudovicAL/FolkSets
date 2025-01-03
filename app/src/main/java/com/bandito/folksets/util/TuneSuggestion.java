package com.bandito.folksets.util;

import com.bandito.folksets.sql.entities.TuneEntity;

public class TuneSuggestion {
    public TuneEntity minusToneMinor;
    public TuneEntity minusToneMajor;

    public TuneEntity relativeMinor;
    public TuneEntity relativeMajor;

    public TuneEntity plusToneMinor;
    public TuneEntity plusToneMajor;
    public TuneEntity plusFifthMinor;
    public TuneEntity plusFifthMajor;
    public TuneEntity plusFourthMinor;
    public TuneEntity plusFourthMajor;

    public boolean hasSuggestion() {
        if (minusToneMinor != null
                || minusToneMajor != null
                || relativeMinor != null
                || relativeMajor != null
                || plusToneMinor != null
                || plusToneMajor != null
                || plusFourthMinor != null
                || plusFourthMajor != null
                || plusFifthMinor != null
                || plusFifthMajor != null) {
            return true;
        }
        return false;
    }
}
