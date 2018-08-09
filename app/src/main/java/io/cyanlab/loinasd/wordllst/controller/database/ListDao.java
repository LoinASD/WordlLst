package io.cyanlab.loinasd.wordllst.controller.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

/**
 * Created by Анатолий on 13.03.2018.
 */
@Dao
public interface ListDao {

    @Query("SELECT * FROM wordlist")
    List<WordList> getAllLists();

    @Transaction
    @Query("SELECT * FROM WordList WHERE name = :wlName")
    WordList getWordlist(String wlName);

    @Update
    int updateList(WordList list);

    @Insert
    void insertList(WordList list);

    @Delete
    void deleteList(WordList list);

    @Query("DELETE FROM wordlist WHERE name = :wlName")
    void deleteList(String wlName);

    @Query("SELECT name FROM wordlist")
    List<String> loadNames();

}
