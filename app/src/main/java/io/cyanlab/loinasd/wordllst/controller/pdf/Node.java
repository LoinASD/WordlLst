package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Node implements Serializable {

    private int weight;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @ColumnInfo(name = "nodeWLName")
    private String wlName;

    public String getWlName() {
        return wlName;
    }

    public void setWlName(String wlName) {
        this.wlName = wlName;
    }

    @PrimaryKey
    public Integer id;

    private String primText;

    public String getTransText() {
        return transText;
    }

    public void setTransText(String transText) {
        if (transText == null) this.transText = "";
        else this.transText = transText;
    }

    private String transText;

    void convertText(CharConverter converter) {
        System.out.printf("P: %s%nT: %s%n%n", primText, transText);
        for (int j = 0; j < 2; j++) {
            String text = (j == 0 ? transText : primText);
            if (text != null && text.contains("<")) {
                StringBuilder message = new StringBuilder();
                char cc;
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
                        int c;
                        try{
                            c = Integer.parseInt(numChar.toString(), 16);
                            char ch = converter.convert(c);
                            if (ch != '.' && LangChecker.langCheck(ch) != Lang.NUM)
                                message.append(ch);
                        }catch (Exception e) {
                            e.printStackTrace();
                            System.out.println(numChar);
                            System.out.println(cc);
                        }

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

    public String getPrimText() {
        return primText;
    }

    public void setPrimText(String primText) {
        this.primText = primText;
    }


}
