package io.cyanlab.loinasd.wordllst.controller.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import io.cyanlab.loinasd.wordllst.controller.pdf.Node;

/**
 * Created by Анатолий on 13.03.2018.
 */
@Dao
public interface NodeDao {

    @Query("SELECT * FROM node")
    List<Node> getAllNodes();

    @Query("SELECT * FROM node WHERE nodeWLName = :wlName")
    List<Node> getNodes(String wlName);

    @Transaction
    @Insert
    void insertAll(List<Node> nodes);

    @Insert
    long insertNode(Node node);

    @Transaction
    @Query("DELETE FROM node WHERE nodeWLName = :wlName")
    void deleteNodes(String wlName);
}
