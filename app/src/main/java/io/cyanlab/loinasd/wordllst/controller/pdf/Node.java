package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = WordList.class, parentColumns = "name", childColumns = "wlName", onDelete = CASCADE, onUpdate = CASCADE))
public class Node implements Serializable {

    @ColumnInfo(index = true)
    public String wlName;

    @PrimaryKey(autoGenerate = true)
    public Integer id;

    public String primText;

    public String transText;
}
