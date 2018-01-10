package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.ArrayList;

import io.cyanlab.loinasd.wordllst.activities.MainActivity;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;


public class TextExtractor {

    private static int ch;
    private PipedInputStream io;
    private CharConverter converter = new CharConverter();
    private static String lineStr;
    private static boolean gotDictionary = false;
    private String newWlName;
    private boolean isExists = false;
    private StringBuilder text;
    private int range;


    public void extract(PipedInputStream io, DBHelper dbHelper) {
        this.io = io;
        ch = 0;

        try {
            parse(dbHelper);
            if ((gotDictionary) && (!isExists)) {
                convertText();
                nodeCollect(dbHelper);
            } else {
                //MainActivity.h.sendEmptyMessage(4);
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void parse(DBHelper dbHelper) throws IOException {
        while ((ch != -1) && (!isExists)) {
            lineStr = readLine();
            if (lineStr.equals("BT")) {
                textToken(dbHelper);
            } else if (lineStr.equals("begincmap")){
                parseCMap();
            }

        }
    }

    private void parseCMap() throws IOException {
        String[] chars;
        int count;
        while (ch != -1) {
            lineStr = readLine();
            if (lineStr.endsWith("begincodespacerange")) {
                range = readLine().split(" ")[0].length() - 2;

            }

            if (lineStr.endsWith("beginbfchar")){
                count = Integer.parseInt("" + lineStr.charAt(0));
                for (int i = count; i > 0; i--) {
                    lineStr = readLine();
                    chars = lineStr.split(" ");
                    int c1 = Integer.parseInt(chars[0].substring(1, range + 1), 16);
                    int c2 = Integer.parseInt(chars[1].substring(1, range + 1), 16);
                    converter.addNewRange(c1, c1, c2);
                }
            }
            if (lineStr.endsWith("beginbfrange")) {
                count = Integer.parseInt("" + lineStr.charAt(0));
                for (int i = count; i > 0; i--) {
                    lineStr = readLine();
                    chars = lineStr.split(" ");
                    String strC1 = chars[0].substring(1, range + 1);
                    String strC2 = chars[1].substring(1, range + 1);
                    System.out.printf("%nc1 = %s; c2 = %s%n", strC1, strC2);
                    int c1 = Integer.parseInt(strC1, 16);
                    int c2 = Integer.parseInt(strC2, 16);
                    System.out.printf("int: c1 = %d; c2 = %d", c1, c2);
                    if (lineStr.contains("[")) {
                        String[] arr = lineStr.substring(
                                lineStr.indexOf('[') + 1, lineStr.length() - 1).split(" ");
                        int c, dif = c1;
                        for (String s: arr) {
                            c = Integer.parseInt(s.substring(1, range + 1), 16);
                            converter.addNewRange(dif, dif, c);
                            dif++;
                        }
                    } else {
                        String strC3 = chars[2].substring(1, range + 1);
                        int c3 = Integer.parseInt(strC3, 16);
                        System.out.printf("%nc3 = %s; int3: %d%n", strC3, c3);
                        converter.addNewRange(c1, c2, c3);
                    }
                }
            }

            if (lineStr.equals("endcmap")) {
                gotDictionary = true;
                break;
            }
        }

    }

    private String readLine() throws IOException {
        StringBuilder l = new StringBuilder();
        while (true) {
            if (io.available()!= 0) {
                while (ch != -1) {
                    ch = io.read();
                    if (ch == -1) return "";
                    if ((char) ch != '\n') {
                        l.append((char) ch);
                    } else {
                        if (l.charAt(l.length() - 1) == '\r')
                            l.deleteCharAt(l.length() - 1);
                        return l.toString();
                    }
                }
            }
        }

        //return l.toString();
    }

    private void convertText() {
        if (text != null) {
            text.trimToSize();
            String hotText = new String(text);
            int i = 0;
            char ch = hotText.charAt(i);
            while (i < text.length() - 1) {
                if (ch == '<') {
                    StringBuilder rawMes = new StringBuilder();
                    rawMes.append(ch);
                    StringBuilder message = new StringBuilder();
                    ch = hotText.charAt(++i);
                    rawMes.append(ch);
                    if (ch != '<') {
                        while (ch != '>') {
                            StringBuilder rawSb = new StringBuilder();
                            for (int j = 0; j < range; j++) {
                                rawSb.append(ch);
                                ch = hotText.charAt(++i);
                                rawMes.append(ch);
                            }
                            rawSb.trimToSize();
                            char rus = 0;
                            if (rawSb.indexOf("<") == -1) {
                                rus = converter.convert(Integer.parseInt(rawSb.toString(), 16));
                            } else {
                                break;
                            }
                            message.append(rus);
                        }
                    }
                    if (message != null) {
                        hotText = hotText.replaceAll(new String(rawMes), new String(message));
                        i = i - rawMes.length() + message.length();
                    }

                }
                if (!(i < hotText.length() - 1)) {
                    break;
                }
                ch = hotText.charAt(++i);
            }
            text = new StringBuilder(hotText);
        }
    }

    private void textToken(DBHelper dbHelper) throws IOException {

        if (newWlName != null) {
            while ((char) ch != '[') {
                ch = io.read();
            }
            while ((char) ch != ']') {
                while (((char) ch != '(') && ((char) ch != '<')) {
                    ch = io.read();
                }
                if ((char) ch == '<') {
                    text.append((char) ch);
                    while ((char) ch != '>') {
                        ch = io.read();
                        text.append((char) ch);
                    }
                } else {
                    ch = io.read();
                    while ((char) ch != ')') {
                        text.append((char) ch);
                        ch = io.read();
                    }
                }
                ch = io.read();
            }
            text.append(" ");
        } else {
            StringBuilder name = new StringBuilder();
            ch = io.read();
            while ((char) ch != '[') {
                ch = io.read();
            }
            ch = io.read();
            while ((char) ch != ']') {
                while ((char) ch != '(') {
                    ch = io.read();
                    if ((char) ch == '<') {
                        name.append((char) ch);
                        while ((char) ch != '>') {
                            ch = io.read();
                            name.append((char) ch);
                        }
                    }
                }
                ch = io.read();
                while ((char) ch != ')') {
                    name.append((char) ch);
                    ch = io.read();
                }
                ch = io.read();
            }
            name.trimToSize();
            newWlName = new String(name).trim().replaceAll(" ", "_").replaceAll(":", "");

            for (String s : dbHelper.loadWlsNames()) {
                if (newWlName.equals(s)) {
                    isExists = true;
                }
            }
            text = new StringBuilder();

        }
    }


    private void nodeCollect(DBHelper dbHelper) {
        ArrayList<String> prim = new ArrayList<>();
        ArrayList<String> trans = new ArrayList<>();

        text.trimToSize();

        int end = 0;
        char cc = text.charAt(end);
        int lang = LangChecker.langCheck(cc);

        while (lang != LangChecker.LANG_ENG) {

            end++;
            lang = LangChecker.langCheck(text.charAt(end));

        }

        int start = end;

        boolean switcher = true;

        DBHelper.getInstance().getWritableDatabase().beginTransaction();
        boolean isWritten = false;

        try {
            boolean isBrackets = false;

            SQLiteDatabase database = DBHelper.getInstance().getWritableDatabase();

            database.execSQL("create table " + newWlName + " ("
                    + "_id integer primary key autoincrement,"
                    + "prim text,"
                    + "trans text,"
                    + "position integer" + ");");

            ContentValues values = new ContentValues();
            values.put("wlId", newWlName);
            database.insert("WordLists", null, values);

            values.clear();
            int count = 0;


            /*String[] words = text.toString().split(" ");

            int i = 0;

            while (i < words.length){
                StringBuilder node = new StringBuilder();
                while (i < words.length && words[i].length() == 0){
                    i++;
                }
                lang = LangChecker.langCheck(words[i].charAt(0));
                while(i < words.length && (words[i].length() == 0 || (switcher && LangChecker.langCheck(words[i].charAt(0)) != LangChecker.LANG_RUS)||(!switcher && LangChecker.langCheck(words[i].charAt(0)) != LangChecker.LANG_ENG))){
                    node.append(words[i]+ " ");
                    i++;
                }

                if (switcher) {

                    values.put("prim", new String(node).replaceAll("/", ", ").trim());


                } else {
                    String nodeStr = new String(node).trim();

                    nodeStr = nodeStr.substring(0,1).toUpperCase()+nodeStr.substring(1);
                    values.put("trans", nodeStr.replaceAll("/", ", ").trim());
                    values.put("position", count++);
                    database.insert(newWlName, null, values);
                    values.clear();
                }

                switcher = !switcher;

            }
            if (values.getAsString("prim") != null){
                values.put("trans", "");
                values.put("position", count++);
                database.insert(newWlName, null, values);
                values.clear();
            }

            for (i = 0; i< 3; i++){
                values.put("prim", "");
                values.put("trans", "");
                values.put("position", count++);
                database.insert(newWlName, null, values);
                values.clear();
            }*/


            while (end < text.length() - 1) {

                StringBuilder node = new StringBuilder();
                cc = text.charAt(start);


                while (end < text.length() - 1 &&
                        ((switcher && LangChecker.langCheck(text.charAt(end)) != LangChecker.LANG_RUS) ||
                                (!switcher && LangChecker.langCheck(text.charAt(end)) != LangChecker.LANG_ENG))) {

                    if ((int) cc == 92) {
                        if (!isBrackets) {
                            isBrackets = true;
                        } else {
                            node.append(')');
                            isBrackets = false;
                        }
                        cc = text.charAt(++end);
                        lang = LangChecker.langCheck(text.charAt(end));
                    }

                    if ((lang != 2) && (cc != '.')) {
                        node.append(cc);
                    }
                    cc = text.charAt(++end);
                    lang = LangChecker.langCheck(text.charAt(end));
                }

                start = end;


                if (switcher) {

                    values.put("prim", new String(node).replaceAll("/", ", ").trim());


                } else {
                    String nodeStr = new String(node).trim();

                    nodeStr = nodeStr.substring(0, 1).toUpperCase() + nodeStr.substring(1);
                    values.put("trans", nodeStr.replaceAll("/", ", ").trim());
                    values.put("position", count++);
                    database.insert(newWlName, null, values);
                    values.clear();
                }

                switcher = !switcher;

            }
            if (values.getAsString("prim") != null) {
                values.put("trans", "");
                values.put("position", count++);
                database.insert(newWlName, null, values);
                values.clear();
            }
            database.setTransactionSuccessful();
            isWritten = true;
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            DBHelper.getInstance().getWritableDatabase().endTransaction();
        }

        String wlName = newWlName;
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("wlName", null);
        msg.what = 2;


        /*if (!isExists) {
            isWritten = dbHelper.saveNewWL(wlName, prim, trans);
        }*/
        if (isWritten) {
            data.putString("wlName", wlName);
        }

        //msg.setData(data);
        //MainActivity.h.sendMessage(msg);
    }
}
