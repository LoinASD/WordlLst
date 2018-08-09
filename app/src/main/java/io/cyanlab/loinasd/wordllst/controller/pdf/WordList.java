package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by Lev on 01.03.2018.
 */

@Entity(indices = @Index(value = "name", unique = true))
public class WordList {

    @NonNull
    public String name;

    @PrimaryKey(autoGenerate = true)
    public int id;
}
