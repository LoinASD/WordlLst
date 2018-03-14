package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Node implements Serializable {

    @Ignore
    private double x;
    @Ignore
    private double y;

    public String getWlName() {
        return wlName;
    }

    public void setWlName(String wlName) {
        this.wlName = wlName;
    }

    @ColumnInfo(name = "nodeWLName")
    private String wlName;

    @PrimaryKey
    public Integer id;

    private String primText;

    public String getTransText() {
        return transText;
    }

    public void setTransText(String transText) {
        this.transText = transText;
    }

    private String transText;

    public void convertText(CharConverter converter) {
        char cc;
        StringBuilder message = new StringBuilder();
        if (transText.charAt(0) == '(') {
            String[] stringarr = transText.split("[(]");
            for (String s : stringarr) {
                s= s.split("[)]")[0];
                message.append(s);
            }
        } else if (transText.charAt(0) == '<') {
            int i = 0;

            while (i < transText.length()) {
                cc = transText.charAt(i);
                if (cc == '<') {
                    cc = transText.charAt(++i);
                    StringBuilder numChar;

                    while (cc != '>') {
                        numChar = new StringBuilder();
                        for (int j = 0; j < 4; j++) { // 4 - char`s length in HEX
                            numChar.append(cc);
                            cc = transText.charAt(++i);
                        }
                        int c = Integer.parseInt(numChar.toString(), 16);
                        message.append(converter.convert(c));
                    }
                }
                i++;
            }
        }


        this.transText = message.toString();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getPrimText() {
        return primText;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setPrimText(String primText) {
        this.primText = primText;
    }


}
