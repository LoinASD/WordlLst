package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Lev on 01.03.2018.
 */

@Entity
public class WordList {

    @PrimaryKey
    public Integer id;

    public String getWlName() {
        return wlName;
    }

    public void setWlName(String wlName) {
        this.wlName = wlName;
    }

    private String wlName;


}
