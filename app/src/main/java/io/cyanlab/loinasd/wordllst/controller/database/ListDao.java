package io.cyanlab.loinasd.wordllst.controller.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

/**
 * Created by Анатолий on 13.03.2018.
 */
@Dao
public interface ListDao {

    @Query("SELECT * FROM wordlist")
    List<WordList> getAllLists();

    @Transaction
    @Query("SELECT wlName, id FROM WordList WHERE id = :id")
    FilledList getWordlist(int id);

    @Transaction
    @Query("SELECT wlName, id FROM WordList WHERE wlName = :wlName")
    FilledList getWordlist(String wlName);

    @Insert
    long insertList(WordList list);

    @Delete
    void deleteList(WordList list);

    @Query("DELETE FROM wordlist WHERE wlName = :wlName")
    void deleteList(String wlName);

    @Query("SELECT wlName FROM wordlist")
    List<String> loadNames();


}
