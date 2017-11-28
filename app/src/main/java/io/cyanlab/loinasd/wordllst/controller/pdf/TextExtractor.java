package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.os.Bundle;
import android.os.Message;

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

    public void extract(PipedInputStream io, DBHelper dbHelper) {
        this.io = io;
        ch = 0;

        try {
            parse(dbHelper);
            if ((gotDictionary) && (!isExists)) {
                convertText();
                nodeCollect(dbHelper);
            } else {
                MainActivity.h.sendEmptyMessage(4);
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
        converter = new CharConverter();
        String[] chars;
        int count;
        while (ch != -1) {
            lineStr = readLine();
            if (lineStr.endsWith("beginbfchar")){
                count = Integer.parseInt("" + lineStr.charAt(0));
                for (int i = count; i > 0; i--) {
                    lineStr = readLine();
                    chars = lineStr.split(" ");
                    int c1 = Integer.parseInt(chars[0].substring(1, 5), 16);
                    int c2 = Integer.parseInt(chars[1].substring(1, 5), 16);
                    converter.addNewRange(c1, c1, c2);
                }
            }
            if (lineStr.endsWith("beginbfrange")) {
                count = Integer.parseInt("" + lineStr.charAt(0));
                for (int i = count; i > 0; i--) {
                    lineStr = readLine();
                    chars = lineStr.split(" ");
                    int c1 = Integer.parseInt(chars[0].substring(1, 5), 16);
                    int c2 = Integer.parseInt(chars[1].substring(1, 5), 16);
                    if (lineStr.contains("[")) {
                        String[] arr = lineStr.substring(
                                lineStr.indexOf('[') + 1, lineStr.length() - 1).split(" ");
                        int c, dif = c1;
                        for (String s: arr) {
                            c = Integer.parseInt(s.substring(1, 5), 16);
                            converter.addNewRange(dif, dif, c);
                            dif++;
                        }
                    } else {
                        int c3 = Integer.parseInt(chars[2].substring(1, 5), 16);
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
                    while (ch != '>') {
                        StringBuilder rawSb = new StringBuilder();
                        for (int j = 0; j < 4; j++) {
                            rawSb.append(ch);
                            ch = hotText.charAt(++i);
                            rawMes.append(ch);
                        }
                        rawSb.trimToSize();
                        char rus = converter.convert(Integer.parseInt(rawSb.toString(), 16));
                        message.append(rus);
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

        while (end < text.length() - 1) {

            StringBuilder node = new StringBuilder();
            cc = text.charAt(start);

            while (((lang == LangChecker.langCheck(text.charAt(start))) || (lang == -1)) && (end < text.length() - 1)) {
                node.append(cc);
                cc = text.charAt(++end);
                lang = LangChecker.langCheck(text.charAt(end));
            }

            start = end;

            if (switcher) {
                prim.add(new String(node).trim());
            } else {
                trans.add(new String(node).trim());
            }

            switcher = !switcher;

        }

        String wlName = newWlName;
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("wlName", null);
        msg.what = 2;

        boolean isWritten = false;
        if (!isExists) {
            isWritten = dbHelper.saveNewWL(wlName, prim, trans);
        }
        if (isWritten) {
            data.putString("wlName", wlName);
        }

        msg.setData(data);
        MainActivity.h.sendMessage(msg);

    }
}
