package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.TUNE_ID;
import static com.bandito.folksets.util.Constants.TUNE_TAGS;
import static com.bandito.folksets.util.Constants.TABLE_SET;
import static com.bandito.folksets.util.Constants.TABLE_TUNE;

import android.content.Context;

import androidx.core.util.Pair;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.bandito.folksets.sql.DatabaseManager;
import com.bandito.folksets.sql.entities.SetEntity;
import com.bandito.folksets.sql.entities.TuneEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private Context appContext;

    private TuneEntity tuneEntity;
    private SetEntity setEntity;

    @Before
    public void before() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        tuneEntity = generateTuneEntity();
        setEntity = generateSetEntity();
    }

    @Test
    public void initializeDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> DatabaseManager.initializeDatabase(appContext));
    }

    @Test
    public void insertTuneInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            DatabaseManager.insertTuneInDatabase(tuneEntity);
            List<TuneEntity> tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase("*", null, null, null, null);
            Assertions.assertThat(tuneEntityList).hasSize(1);
            Assertions.assertThat(tuneEntityList.get(0).tuneTitles).isEqualTo(tuneEntity.tuneTitles);
        });
    }

    @Test
    public void insertTunesInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            List<TuneEntity> tuneEntityList = new ArrayList<>();
            tuneEntityList.add(generateTuneEntity());
            tuneEntityList.add(generateTuneEntity());
            tuneEntityList.add(generateTuneEntity());
            DatabaseManager.insertTunesInDatabase(tuneEntityList);
            tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase("*", null, null, null, null);
            Assertions.assertThat(tuneEntityList).hasSize(3);
        });
    }

    @Test
    public void removeTuneFromDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            long tuneIdToDelete = DatabaseManager.insertTuneInDatabase(tuneEntity);
            DatabaseManager.insertTuneInDatabase(tuneEntity);
            List<TuneEntity> tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase("*", null, null, null, null);
            Assertions.assertThat(tuneEntityList).hasSize(2);
            int numberOfRowsDeleted = DatabaseManager.removeTuneFromDatabase(tuneIdToDelete);
            Assertions.assertThat(numberOfRowsDeleted).isEqualTo(1);
            tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase("*", null, null, null, null);
            Assertions.assertThat(tuneEntityList).hasSize(1);
        });
    }

    @Test
    public void removeTunesFromDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            List<TuneEntity> tuneEntityList = new ArrayList<>();
            tuneEntityList.add(generateTuneEntity());
            tuneEntityList.add(generateTuneEntity());
            tuneEntityList.add(generateTuneEntity());
            DatabaseManager.insertTunesInDatabase(tuneEntityList);
            tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase("*", null, null, null, null);
            Assertions.assertThat(tuneEntityList).hasSize(3);
            List<Long> tuneIds = tuneEntityList.stream().map(tuneEntity -> tuneEntity.tuneId).collect(Collectors.toList());
            DatabaseManager.removeTunesFromDatabase(tuneIds);
            tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase("*", null, null, null, null);
            Assertions.assertThat(tuneEntityList).hasSize(0);
        });
    }

    @Test
    public void updateTuneInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            long idOfTuneToUpdate = DatabaseManager.insertTuneInDatabase(tuneEntity);
            DatabaseManager.insertTuneInDatabase(tuneEntity);
            List<TuneEntity> tuneEntityList = DatabaseManager.findTuneByIdInDatabase("*", String.valueOf(idOfTuneToUpdate), null, null);
            Assertions.assertThat(tuneEntityList).hasSize(1);
            TuneEntity tuneEntityForUpdate = tuneEntityList.get(0);
            String newTitle = "newTitle";
            tuneEntityForUpdate.tuneTitles = newTitle;
            int numberOfRowsUpdated = DatabaseManager.updateTuneInDatabase(tuneEntityForUpdate);
            Assertions.assertThat(numberOfRowsUpdated).isEqualTo(1);
            tuneEntityList = DatabaseManager.findTuneByIdInDatabase("*", String.valueOf(idOfTuneToUpdate), null, null);
            Assertions.assertThat(tuneEntityList).hasSize(1);
            Assertions.assertThat(tuneEntityList.get(0).tuneTitles).isEqualTo(newTitle);
            Assertions.assertThat(tuneEntityList.get(0).tuneComposer).isEqualTo(tuneEntity.tuneComposer);
        });
    }

    @Test
    public void insertSetInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            DatabaseManager.insertSetInDatabase(setEntity);
            List<SetEntity> setEntityList = DatabaseManager.findAllSetsInDatabase("*", null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            Assertions.assertThat(setEntityList.get(0).setName).isEqualTo(setEntity.setName);
        });
    }

    @Test
    public void removeSetFromDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            long setIdToDelete = DatabaseManager.insertSetInDatabase(setEntity);
            DatabaseManager.insertSetInDatabase(setEntity);
            List<SetEntity> setEntityList = DatabaseManager.findAllSetsInDatabase("*", null, null);
            Assertions.assertThat(setEntityList).hasSize(2);
            int numberOfRowsDeleted = DatabaseManager.removeSetFromDatabase(setIdToDelete);
            Assertions.assertThat(numberOfRowsDeleted).isEqualTo(1);
            setEntityList = DatabaseManager.findAllSetsInDatabase("*", null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
        });
    }

    @Test
    public void updateSetInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            long idOfSetToUpdate = DatabaseManager.insertSetInDatabase(setEntity);
            DatabaseManager.insertSetInDatabase(setEntity);
            List<SetEntity> setEntityList = DatabaseManager.findSetByIdInDatabase("*",  String.valueOf(idOfSetToUpdate), null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            SetEntity setEntityForUpdate = setEntityList.get(0);
            String newName = "newName";
            setEntityForUpdate.setName = newName;
            int numberOfRowsUpdated = DatabaseManager.updateSetInDatabase(setEntityForUpdate);
            Assertions.assertThat(numberOfRowsUpdated).isEqualTo(1);
            setEntityList = DatabaseManager.findSetByIdInDatabase("*",  String.valueOf(idOfSetToUpdate), null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            Assertions.assertThat(setEntityList.get(0).setName).isEqualTo(newName);
            Assertions.assertThat(setEntityList.get(0).setTunes).isEqualTo(setEntity.setTunes);
        });
    }

    @Test
    public void findTuneByLongIdInListInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            Long tuneId = DatabaseManager.insertTuneInDatabase(tuneEntity);
            List<TuneEntity> tuneEntityList = DatabaseManager.findTuneByIdInDatabase("*", tuneId, null, null);
            Assertions.assertThat(tuneEntityList).hasSize(1);
            Assertions.assertThat(tuneEntityList.get(0).tuneTitles).isEqualTo(tuneEntity.tuneTitles);
        });
    }

    @Test
    public void findTuneByStringIdInListInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            Long tuneId = DatabaseManager.insertTuneInDatabase(tuneEntity);
            List<TuneEntity> tuneEntityList = DatabaseManager.findTuneByIdInDatabase("*", String.valueOf(tuneId), null, null);
            Assertions.assertThat(tuneEntityList).hasSize(1);
            Assertions.assertThat(tuneEntityList.get(0).tuneTitles).isEqualTo(tuneEntity.tuneTitles);
        });
    }

    @Test
    public void findTunesWithNullValueInListInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            DatabaseManager.insertTuneInDatabase(tuneEntity);
            List<TuneEntity> tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase("*", null, null, null, null);
            Assertions.assertThat(tuneEntityList).hasSize(1);
            Assertions.assertThat(tuneEntityList.get(0).tuneTitles).isEqualTo(tuneEntity.tuneTitles);
        });
    }

    @Test
    public void findTunesWithTagsValuesInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            TuneEntity tuneEntity1 = generateTuneEntity();
            TuneEntity tuneEntity2 = generateTuneEntity();
            TuneEntity tuneEntity3 = generateTuneEntity();
            tuneEntity3.tuneTags = "otherTags";
            DatabaseManager.insertTuneInDatabase(tuneEntity1);
            DatabaseManager.insertTuneInDatabase(tuneEntity2);
            DatabaseManager.insertTuneInDatabase(tuneEntity3);
            String tag1 = "tag1";
            String tag2 = "tag2";
            String[] tarArray = new String[] { tag1, tag2 };
            List<TuneEntity> tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase("*", TUNE_TAGS, tarArray, null, null);
            Assertions.assertThat(tuneEntityList).hasSize(2);
            for (TuneEntity se : tuneEntityList) {
                Assertions.assertThat(se.tuneTags).contains(tag1).contains(tag2);
            }
        });
    }

    @Test
    public void findAllSetsInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            DatabaseManager.insertSetInDatabase(setEntity);
            List<SetEntity> setEntityList = DatabaseManager.findAllSetsInDatabase("*", null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            Assertions.assertThat(setEntityList.get(0).setName).isEqualTo(setEntity.setName);
        });
    }

    @Test
    public void findSetByStringIdInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            Long setId = DatabaseManager.insertSetInDatabase(setEntity);
            List<SetEntity> setEntityList = DatabaseManager.findSetByIdInDatabase("*", String.valueOf(setId), null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            Assertions.assertThat(setEntityList.get(0).setName).isEqualTo(setEntity.setName);
        });
    }

    @Test
    public void findSetByLongIdInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            Long setId = DatabaseManager.insertSetInDatabase(setEntity);
            List<SetEntity> setEntityList = DatabaseManager.findSetByIdInDatabase("*", setId, null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            Assertions.assertThat(setEntityList.get(0).setName).isEqualTo(setEntity.setName);
        });
    }

    @Test
    public void findSetByNameInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            DatabaseManager.insertSetInDatabase(setEntity);
            List<SetEntity> setEntityList = DatabaseManager.findSetsByNameInDatabase("*", setEntity.setName, null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            Assertions.assertThat(setEntityList.get(0).setName).isEqualTo(setEntity.setName);
        });
    }

    @Test
    public void findSetsWithTuneInDatabase1() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            DatabaseManager.truncateTable(TABLE_SET);
            TuneEntity tuneEntity1 = generateTuneEntity();
            TuneEntity tuneEntity2 = generateTuneEntity();
            TuneEntity tuneEntity3 = generateTuneEntity();
            tuneEntity1.tuneTitles = "tune1;otherName";
            tuneEntity2.tuneTitles = "placeholder;tune2";
            tuneEntity3.tuneTitles = "tune3";
            DatabaseManager.insertTuneInDatabase(tuneEntity1);
            DatabaseManager.insertTuneInDatabase(tuneEntity2);
            DatabaseManager.insertTuneInDatabase(tuneEntity3);
            SetEntity setEntity1 = generateSetEntity();
            SetEntity setEntity2 = generateSetEntity();
            SetEntity setEntity3 = generateSetEntity();
            List<TuneEntity> tuneEntityList = DatabaseManager.findTunesWithValueInListInDatabase(TUNE_ID, null, null, null, null);
            setEntity1.setTunes = tuneEntityList.get(0).tuneId + ";" + tuneEntityList.get(1).tuneId;
            setEntity2.setTunes = tuneEntityList.get(1).tuneId + ";" + tuneEntityList.get(2).tuneId;
            setEntity3.setTunes = tuneEntityList.get(0).tuneId + ";" + tuneEntityList.get(2).tuneId;
            DatabaseManager.insertSetInDatabase(setEntity1);
            DatabaseManager.insertSetInDatabase(setEntity2);
            DatabaseManager.insertSetInDatabase(setEntity3);
            Pair<Integer, List<SetEntity>> result = DatabaseManager.findSetsWithTunesInDatabase("tune", null, null);
            Assertions.assertThat(result.first).isEqualTo(3);
            Assertions.assertThat(result.second).hasSize(3);
            result = DatabaseManager.findSetsWithTunesInDatabase("tune3", null, null);
            Assertions.assertThat(result.first).isEqualTo(1);
            Assertions.assertThat(result.second).hasSize(2);
            result = DatabaseManager.findSetsWithTunesInDatabase("inexistant", null, null);
            Assertions.assertThat(result.first).isEqualTo(0);
            Assertions.assertThat(result.second).hasSize(0);
        });
    }

    @Test
    public void findSetsWithTuneInDatabase2() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            SetEntity setEntity1 = generateSetEntity();
            SetEntity setEntity2 = generateSetEntity();
            SetEntity setEntity3 = generateSetEntity();
            setEntity3.setTunes = "otherTunes";
            DatabaseManager.insertSetInDatabase(setEntity1);
            DatabaseManager.insertSetInDatabase(setEntity2);
            DatabaseManager.insertSetInDatabase(setEntity3);
            long tuneId = 2;
            List<SetEntity> setEntityList = DatabaseManager.findSetsWithTunesInDatabase(new Long[]{tuneId}, null, null);
            Assertions.assertThat(setEntityList).hasSize(2);
            for (SetEntity se : setEntityList) {
                Assertions.assertThat(se.setTunes).contains(String.valueOf(tuneId));
            }
        });
    }

    @Test
    public void removeTuneFromSets() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            SetEntity setEntity1 = generateSetEntity();
            SetEntity setEntity2 = generateSetEntity();
            SetEntity setEntity3 = generateSetEntity();
            setEntity3.setTunes = "2";
            DatabaseManager.insertSetInDatabase(setEntity1);
            DatabaseManager.insertSetInDatabase(setEntity2);
            DatabaseManager.insertSetInDatabase(setEntity3);
            DatabaseManager.removeTuneFromSets(2);
            List<SetEntity> setEntityList = DatabaseManager.findAllSetsInDatabase("*", null, null);
            Assertions.assertThat(setEntityList).hasSize(2);
        });
    }

    @Test
    public void getAllTagsInTuneTable() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            TuneEntity tuneEntity1 = generateTuneEntity();
            TuneEntity tuneEntity2 = generateTuneEntity();
            TuneEntity tuneEntity3 = generateTuneEntity();
            tuneEntity2.tuneTags = "tag2;tag3;tag4";
            tuneEntity3.tuneTags = "tag3;tag4;tag5";
            DatabaseManager.insertTuneInDatabase(tuneEntity1);
            DatabaseManager.insertTuneInDatabase(tuneEntity2);
            DatabaseManager.insertTuneInDatabase(tuneEntity3);
            String[] tagArray = DatabaseManager.getAllUniqueTagInTuneTable();
            Assertions.assertThat(tagArray).hasSize(5);
        });
    }

    @Test
    public void getAllPlayersInTuneTable() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_TUNE);
            TuneEntity tuneEntity1 = generateTuneEntity();
            TuneEntity tuneEntity2 = generateTuneEntity();
            TuneEntity tuneEntity3 = generateTuneEntity();
            tuneEntity2.tunePlayedBy = "Grace;Patrice;Carl";
            tuneEntity3.tunePlayedBy = "Patrice;Carl;Henry";
            DatabaseManager.insertTuneInDatabase(tuneEntity1);
            DatabaseManager.insertTuneInDatabase(tuneEntity2);
            DatabaseManager.insertTuneInDatabase(tuneEntity3);
            String[] playerArray = DatabaseManager.getAllUniquePlayedByInTuneTable();
            Assertions.assertThat(playerArray).hasSize(5);
        });
    }

    private TuneEntity generateTuneEntity() {
        TuneEntity generatedTuneEntity = new TuneEntity(
                "title1;title2;title3",
                "file path",
                "file type",
                "2024-01-01");
        generatedTuneEntity.tuneTags = "tag1;tag2;tag3";
        generatedTuneEntity.tuneComposer = "composer";
        generatedTuneEntity.tuneRegionOfOrigin = "regiong of origin";
        generatedTuneEntity.tuneKey = "key";
        generatedTuneEntity.tuneIncipit = "incipit";
        generatedTuneEntity.tuneForm = "form";
        generatedTuneEntity.tunePlayedBy = "Jacob;Grace;Patrice";
        generatedTuneEntity.tuneNote = "note";
        generatedTuneEntity.tuneLastConsultationDate = "2024-02-02";
        generatedTuneEntity.tuneConsultationNumber = 3;
        return generatedTuneEntity;
    }

    private SetEntity generateSetEntity() {
        SetEntity generatedSetEntity = new SetEntity();
        generatedSetEntity.setName = "name";
        generatedSetEntity.setTunes = "1;2;3";
        return generatedSetEntity;
    }
}