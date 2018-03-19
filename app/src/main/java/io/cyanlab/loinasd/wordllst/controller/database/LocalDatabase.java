package io.cyanlab.loinasd.wordllst.controller.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

/**
 * Created by Анатолий on 13.03.2018.
 */

@Database(entities = {WordList.class, Node.class}, version = 1)
public abstract class LocalDatabase extends RoomDatabase {

    public abstract ListDao listDao();

    public abstract NodeDao nodeDao();

}
