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
        for (int j = 0; j < 2; j++) {
            String text = (j == 0 ? transText : primText);
            if (text != null && text.contains("<")) {
                StringBuilder message = null;
                char cc;
                message = new StringBuilder();
                int last = 0;
                int i = text.indexOf('<');

                while (i != -1 && i < text.length() && i >= last) {
                    message.append(text.substring(last, i));
                    cc = text.charAt(++i);
                    StringBuilder numChar;

                    while (cc != '>') {
                        numChar = new StringBuilder();
                        for (int k = 0; k < 4; k++) { // 4 - char`s length in HEX
                            numChar.append(cc);
                            cc = text.charAt(++i);
                        }
                        int c = Integer.parseInt(numChar.toString(), 16);
                        message.append(converter.convert(c));
                    }
                    last = i + 1;
                    i = text.substring(last).indexOf('<') + last;
                }
                if (j == 0) {
                    transText = message.toString();
                } else primText = message.toString();
            }

        }


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
