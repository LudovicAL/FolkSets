package com.bandito.folksets;

import static com.bandito.folksets.util.Constants.SET_ID;
import static com.bandito.folksets.util.Constants.SONG_ID;
import static com.bandito.folksets.util.Constants.SONG_TAGS;
import static com.bandito.folksets.util.Constants.TABLE_SET;
import static com.bandito.folksets.util.Constants.TABLE_SONG;

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
import com.bandito.folksets.sql.entities.SongEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private Context appContext;

    private SongEntity songEntity;
    private SetEntity setEntity;

    @Before
    public void before() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        songEntity = generateSongEntity();
        setEntity = generateSetEntity();
    }

    @Test
    public void initializeDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> DatabaseManager.initializeDatabase(appContext));
    }

    @Test
    public void insertSongInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            DatabaseManager.insertSongInDatabase(songEntity);
            List<SongEntity> songEntityList = DatabaseManager.findSongsInDatabase("*", null, null, null, null);
            Assertions.assertThat(songEntityList).hasSize(1);
            Assertions.assertThat(songEntityList.get(0).songTitles).isEqualTo(songEntity.songTitles);
        });
    }

    @Test
    public void insertSongsInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            List<SongEntity> songEntityList = new ArrayList<>();
            songEntityList.add(generateSongEntity());
            songEntityList.add(generateSongEntity());
            songEntityList.add(generateSongEntity());
            DatabaseManager.insertSongsInDatabase(songEntityList);
            songEntityList = DatabaseManager.findSongsInDatabase("*", null, null, null, null);
            Assertions.assertThat(songEntityList).hasSize(3);
        });
    }

    @Test
    public void removeSongFromDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            long songIdToDelete = DatabaseManager.insertSongInDatabase(songEntity);
            DatabaseManager.insertSongInDatabase(songEntity);
            List<SongEntity> songEntityList = DatabaseManager.findSongsInDatabase("*", null, null, null, null);
            Assertions.assertThat(songEntityList).hasSize(2);
            int numberOfRowsDeleted = DatabaseManager.removeSongFromDatabase(songIdToDelete);
            Assertions.assertThat(numberOfRowsDeleted).isEqualTo(1);
            songEntityList = DatabaseManager.findSongsInDatabase("*", null, null, null, null);
            Assertions.assertThat(songEntityList).hasSize(1);
        });
    }

    @Test
    public void removeSongsFromDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            List<SongEntity> songEntityList = new ArrayList<>();
            songEntityList.add(generateSongEntity());
            songEntityList.add(generateSongEntity());
            songEntityList.add(generateSongEntity());
            DatabaseManager.insertSongsInDatabase(songEntityList);
            songEntityList = DatabaseManager.findSongsInDatabase("*", null, null, null, null);
            Assertions.assertThat(songEntityList).hasSize(3);
            List<Long> songIds = songEntityList.stream().map(songEntity -> songEntity.songId).collect(Collectors.toList());
            DatabaseManager.removeSongsFromDatabase(songIds);
            songEntityList = DatabaseManager.findSongsInDatabase("*", null, null, null, null);
            Assertions.assertThat(songEntityList).hasSize(0);
        });
    }

    @Test
    public void updateSongInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            long idOfSongToUpdate = DatabaseManager.insertSongInDatabase(songEntity);
            DatabaseManager.insertSongInDatabase(songEntity);
            List<SongEntity> songEntityList = DatabaseManager.findSongsInDatabase("*", SONG_ID, String.valueOf(idOfSongToUpdate), null, null);
            Assertions.assertThat(songEntityList).hasSize(1);
            SongEntity songEntityForUpdate = songEntityList.get(0);
            String newTitle = "newTitle";
            songEntityForUpdate.songTitles = newTitle;
            int numberOfRowsUpdated = DatabaseManager.updateSongInDatabase(songEntityForUpdate);
            Assertions.assertThat(numberOfRowsUpdated).isEqualTo(1);
            songEntityList = DatabaseManager.findSongsInDatabase("*", SONG_ID, String.valueOf(idOfSongToUpdate), null, null);
            Assertions.assertThat(songEntityList).hasSize(1);
            Assertions.assertThat(songEntityList.get(0).songTitles).isEqualTo(newTitle);
            Assertions.assertThat(songEntityList.get(0).songComposer).isEqualTo(songEntity.songComposer);
        });
    }

    @Test
    public void insertSetInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            DatabaseManager.insertSetInDatabase(setEntity);
            List<SetEntity> setEntityList = DatabaseManager.findSetsInDatabase("*", null, null, null, null);
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
            List<SetEntity> setEntityList = DatabaseManager.findSetsInDatabase("*", null, null, null, null);
            Assertions.assertThat(setEntityList).hasSize(2);
            int numberOfRowsDeleted = DatabaseManager.removeSetFromDatabase(setIdToDelete);
            Assertions.assertThat(numberOfRowsDeleted).isEqualTo(1);
            setEntityList = DatabaseManager.findSetsInDatabase("*", null, null, null, null);
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
            List<SetEntity> setEntityList = DatabaseManager.findSetsInDatabase("*", SET_ID, String.valueOf(idOfSetToUpdate), null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            SetEntity setEntityForUpdate = setEntityList.get(0);
            String newName = "newName";
            setEntityForUpdate.setName = newName;
            int numberOfRowsUpdated = DatabaseManager.updateSetInDatabase(setEntityForUpdate);
            Assertions.assertThat(numberOfRowsUpdated).isEqualTo(1);
            setEntityList = DatabaseManager.findSetsInDatabase("*", SET_ID, String.valueOf(idOfSetToUpdate), null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            Assertions.assertThat(setEntityList.get(0).setName).isEqualTo(newName);
            Assertions.assertThat(setEntityList.get(0).setSongs).isEqualTo(setEntity.setSongs);
        });
    }

    @Test
    public void findSongsInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            DatabaseManager.insertSongInDatabase(songEntity);
            List<SongEntity> songEntityList = DatabaseManager.findSongsInDatabase("*", null, null, null, null);
            Assertions.assertThat(songEntityList).hasSize(1);
            Assertions.assertThat(songEntityList.get(0).songTitles).isEqualTo(songEntity.songTitles);
        });
    }

    @Test
    public void findSongsWithTagsInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            SongEntity songEntity1 = generateSongEntity();
            SongEntity songEntity2 = generateSongEntity();
            SongEntity songEntity3 = generateSongEntity();
            songEntity3.songTags = "otherTags";
            DatabaseManager.insertSongInDatabase(songEntity1);
            DatabaseManager.insertSongInDatabase(songEntity2);
            DatabaseManager.insertSongInDatabase(songEntity3);
            String tag1 = "tag1";
            String tag2 = "tag2";
            String[] tarArray = new String[] { tag1, tag2 };
            List<SongEntity> songEntityList = DatabaseManager.findSongsWithValueInListInDatabase("*", SONG_TAGS, tarArray, null, null);
            Assertions.assertThat(songEntityList).hasSize(2);
            for (SongEntity se : songEntityList) {
                Assertions.assertThat(se.songTags).contains(tag1).contains(tag2);
            }
        });
    }

    @Test
    public void findSetsInDatabase() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            DatabaseManager.insertSetInDatabase(setEntity);
            List<SetEntity> setEntityList = DatabaseManager.findSetsInDatabase("*", null, null, null, null);
            Assertions.assertThat(setEntityList).hasSize(1);
            Assertions.assertThat(setEntityList.get(0).setName).isEqualTo(setEntity.setName);
        });
    }

    @Test
    public void findSetsWithSongInDatabase1() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            DatabaseManager.truncateTable(TABLE_SET);
            SongEntity songEntity1 = generateSongEntity();
            SongEntity songEntity2 = generateSongEntity();
            SongEntity songEntity3 = generateSongEntity();
            songEntity1.songTitles = "song1;otherName";
            songEntity2.songTitles = "placeholder;song2";
            songEntity3.songTitles = "song3";
            DatabaseManager.insertSongInDatabase(songEntity1);
            DatabaseManager.insertSongInDatabase(songEntity2);
            DatabaseManager.insertSongInDatabase(songEntity3);
            SetEntity setEntity1 = generateSetEntity();
            SetEntity setEntity2 = generateSetEntity();
            SetEntity setEntity3 = generateSetEntity();
            List<SongEntity> songEntityList = DatabaseManager.findSongsInDatabase(SONG_ID, null, null, null, null);
            setEntity1.setSongs = songEntityList.get(0).songId + ";" + songEntityList.get(1).songId;
            setEntity2.setSongs = songEntityList.get(1).songId + ";" + songEntityList.get(2).songId;
            setEntity3.setSongs = songEntityList.get(0).songId + ";" + songEntityList.get(2).songId;
            DatabaseManager.insertSetInDatabase(setEntity1);
            DatabaseManager.insertSetInDatabase(setEntity2);
            DatabaseManager.insertSetInDatabase(setEntity3);
            Pair<Integer, List<SetEntity>> result = DatabaseManager.findSetsWithSongsInDatabase("song", null, null);
            Assertions.assertThat(result.first).isEqualTo(3);
            Assertions.assertThat(result.second).hasSize(3);
            result = DatabaseManager.findSetsWithSongsInDatabase("song3", null, null);
            Assertions.assertThat(result.first).isEqualTo(1);
            Assertions.assertThat(result.second).hasSize(2);
            result = DatabaseManager.findSetsWithSongsInDatabase("inexistant", null, null);
            Assertions.assertThat(result.first).isEqualTo(0);
            Assertions.assertThat(result.second).hasSize(0);
        });
    }

    @Test
    public void findSetsWithSongInDatabase2() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            SetEntity setEntity1 = generateSetEntity();
            SetEntity setEntity2 = generateSetEntity();
            SetEntity setEntity3 = generateSetEntity();
            setEntity3.setSongs = "otherSongs";
            DatabaseManager.insertSetInDatabase(setEntity1);
            DatabaseManager.insertSetInDatabase(setEntity2);
            DatabaseManager.insertSetInDatabase(setEntity3);
            long songId = 2;
            List<SetEntity> setEntityList = DatabaseManager.findSetsWithSongsInDatabase(new Long[]{songId}, null, null);
            Assertions.assertThat(setEntityList).hasSize(2);
            for (SetEntity se : setEntityList) {
                Assertions.assertThat(se.setSongs).contains(String.valueOf(songId));
            }
        });
    }

    @Test
    public void removeSongFromSets() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SET);
            SetEntity setEntity1 = generateSetEntity();
            SetEntity setEntity2 = generateSetEntity();
            SetEntity setEntity3 = generateSetEntity();
            setEntity3.setSongs = "2";
            DatabaseManager.insertSetInDatabase(setEntity1);
            DatabaseManager.insertSetInDatabase(setEntity2);
            DatabaseManager.insertSetInDatabase(setEntity3);
            DatabaseManager.removeSongFromSets(2);
            List<SetEntity> setEntityList = DatabaseManager.findSetsInDatabase("*", null, null, null, null);
            Assertions.assertThat(setEntityList).hasSize(2);
        });
    }

    @Test
    public void getAllTagsInSongTable() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            SongEntity songEntity1 = generateSongEntity();
            SongEntity songEntity2 = generateSongEntity();
            SongEntity songEntity3 = generateSongEntity();
            songEntity2.songTags = "tag2;tag3;tag4";
            songEntity3.songTags = "tag3;tag4;tag5";
            DatabaseManager.insertSongInDatabase(songEntity1);
            DatabaseManager.insertSongInDatabase(songEntity2);
            DatabaseManager.insertSongInDatabase(songEntity3);
            Set<String> tagSet = DatabaseManager.getAllTagsInSongTable();
            Assertions.assertThat(tagSet).hasSize(5);
        });
    }

    @Test
    public void getAllPlayersInSongTable() {
        Assertions.assertThatNoException().isThrownBy(() -> {
            DatabaseManager.initializeDatabase(appContext);
            DatabaseManager.truncateTable(TABLE_SONG);
            SongEntity songEntity1 = generateSongEntity();
            SongEntity songEntity2 = generateSongEntity();
            SongEntity songEntity3 = generateSongEntity();
            songEntity2.songPlayedBy = "Grace;Patrice;Carl";
            songEntity3.songPlayedBy = "Patrice;Carl;Henry";
            DatabaseManager.insertSongInDatabase(songEntity1);
            DatabaseManager.insertSongInDatabase(songEntity2);
            DatabaseManager.insertSongInDatabase(songEntity3);
            Set<String> playerSet = DatabaseManager.getAllPlayersInSongTable();
            Assertions.assertThat(playerSet).hasSize(5);
        });
    }

    private SongEntity generateSongEntity() {
        SongEntity generatedSongEntity = new SongEntity(
                "title1;title2;title3",
                "file path",
                "file type",
                "2024-01-01");
        generatedSongEntity.songTags = "tag1;tag2;tag3";
        generatedSongEntity.songComposer = "composer";
        generatedSongEntity.songRegionOfOrigin = "regiong of origin";
        generatedSongEntity.songKey  = "key";
        generatedSongEntity.songIncipit = "incipit";
        generatedSongEntity.songForm = "form";
        generatedSongEntity.songPlayedBy = "Jacob;Grace;Patrice";
        generatedSongEntity.songNote = "note";
        generatedSongEntity.songLastConsultationDate = "2024-02-02";
        generatedSongEntity.songConsultationNumber = 3;
        return generatedSongEntity;
    }

    private SetEntity generateSetEntity() {
        SetEntity generatedSetEntity = new SetEntity();
        generatedSetEntity.setName = "name";
        generatedSetEntity.setSongs = "1;2;3";
        return generatedSetEntity;
    }
}