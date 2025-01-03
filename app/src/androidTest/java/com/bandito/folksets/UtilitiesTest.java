package com.bandito.folksets;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.bandito.folksets.exception.FolkSetsException;
import com.bandito.folksets.sql.entities.TuneEntity;
import com.bandito.folksets.util.Constants;
import com.bandito.folksets.util.Utilities;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class UtilitiesTest {
    @Test
    public void assertObjectIsNotNull() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            Utilities.assertObjectIsNotNull("objectName", "objectContent");
        });
    }

    @Test
    public void assertObjectIsNotNullError() {
        String objectName = "objectName";
        Assertions.assertThatExceptionOfType(FolkSetsException.class).isThrownBy(() -> {
            Utilities.assertObjectIsNotNull(objectName, null);
        }).withMessage("The object " + objectName + " passed as parameter is null.");
    }

    @Test
    public void rearangeTuneInSetOrder() {
        TuneEntity tuneEntity1 = new TuneEntity("title", "path", "type", "creationDate");
        tuneEntity1.tuneId = 1L;
        TuneEntity tuneEntity2 = new TuneEntity("title", "path", "type", "creationDate");
        tuneEntity2.tuneId = 2L;
        List<TuneEntity> unorderedSetTuneEntityList = List.of(tuneEntity1, tuneEntity2);
        String[] tuneIdsInSetOrder = new String[]{"2", "1"};
        Assertions.assertThatNoException().isThrownBy(() -> {
            List<TuneEntity> orderedSetTuneEntityList = Utilities.rearangeTuneInSetOrder(unorderedSetTuneEntityList, tuneIdsInSetOrder);
            Assertions.assertThat(String.valueOf(orderedSetTuneEntityList.get(0).tuneId)).isEqualTo(tuneIdsInSetOrder[0]);
            Assertions.assertThat(String.valueOf(orderedSetTuneEntityList.get(1).tuneId)).isEqualTo(tuneIdsInSetOrder[1]);
        });
    }

    @Test
    public void convertExceptionToString() {
        FolkSetsException folkSetsException = new FolkSetsException("message", new NullPointerException());
        Assertions.assertThatNoException().isThrownBy(() -> {
            String exceptionAsString = Utilities.convertExceptionToString(folkSetsException);
            Assertions.assertThat(exceptionAsString)
                    .contains("Exception toString(): com.bandito.folksets.exception.FolkSetsException: message")
                    .contains("Exception message: message")
                    .contains("Exception simple class name: FolkSetsException")
                    .contains("Exception cause: java.lang.NullPointerException")
                    .contains("Exception stacktrace:");
        });
    }

    @Test
    public void getKeyIndex() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            int keyIndex = Utilities.getKeyIndex("Am");
            Assertions.assertThat(keyIndex).isEqualTo(6);
        });
    }

    @Test
    public void getKeyIndexInexistant() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            Integer keyIndex = Utilities.getKeyIndex("Inexistant");
            Assertions.assertThat(keyIndex).isNull();
        });
    }

    @Test
    public void getKeyWithIntervalPositiveMajor() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            String result = Utilities.getKeyWithInterval("Am", 11, Constants.KeyQualifier.major);
            Assertions.assertThat(result).isEqualTo("Bb");
        });
    }

    @Test
    public void getKeyWithIntervalNegativeMinor() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            String result = Utilities.getKeyWithInterval("D", -12, Constants.KeyQualifier.minor);
            Assertions.assertThat(result).isEqualTo("F#m");
        });
    }

    @Test
    public void getKeyWithIntervalInexistant() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            String result = Utilities.getKeyWithInterval("Inexistant", -4, Constants.KeyQualifier.minor);
            Assertions.assertThat(result).isNull();
        });
    }
}
