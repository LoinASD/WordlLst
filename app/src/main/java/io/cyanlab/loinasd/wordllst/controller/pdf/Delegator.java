package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.os.Bundle;
import android.os.Message;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import io.cyanlab.loinasd.wordllst.activities.NavActivity;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;


public class Delegator {

    private int ch;
    private PipedInputStream io;
    private CharConverter converter;
    private static String lineStr;
    private static boolean gotDictionary;
    private String newWlName;
    private boolean isExists;
    //private StringBuilder text;
    private int range;
    private DBHelper dbHelper;
    private int proggress;
    private ArrayList<Node> nodes;
    private Node waitingNode;
    private Lang waitingNodeLang;
    TextExtractor extractor;
    private int waitingNodeLine;
    private static Logger log = Logger.getLogger(Delegator.class.getName());

    private void updateProgress() {
        proggress++;
    }


    public void extract(final PipedInputStream io) {
        /**
         * This Method extracts Nodes with text from PipedInputStream
         * Works with DBHelper
         * This is Main Method
         */

        long startTime = System.currentTimeMillis();
        this.io = io;
        nodes = new ArrayList<>();
        extractor = new TextExtractor();
        converter = new CharConverter();
        gotDictionary = false;
        isExists = false;

        try {
            parse();
            if (gotDictionary && !isExists) {
                nodeCollect();
            } else {
                NavActivity.h.sendEmptyMessage(NavActivity.HANDLE_MESSAGE_NOT_EXTRACTED);
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        log.warning("Delegator works in ms:" + (System.currentTimeMillis() - startTime));
        //System.out.printf("Delegator works %d ms", System.currentTimeMillis() - startTime);
    }

    private void parse() throws IOException {
        /**
         * This Method finds Text blocks within tags BT and ET
         * also this method finds cmap
         */



        while (ch != -1) {
            ch = io.read();

            if (ch == 'B') {
                ch = io.read();
                if (ch == 'T') {
                    extractor.textToken();
                }
            } else if (ch =='b'){
                String line = readLine();
                if (line.equals("egincmap"))
                    parseCMap();
            }
        }
    }

    private void parseCMap() throws IOException {

        /**
         * This Method parses a charMap in the end of PDF file
         */

        String[] chars;
        int count;
        while (ch != -1) {
            lineStr = readLine();
            if (lineStr.endsWith("begincodespacerange")) {

                // range looks like <0000> <FFFF>
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
                    //System.out.printf("%nc1 = %s; c2 = %s%n", strC1, strC2);
                    int c1 = Integer.parseInt(strC1, 16);
                    int c2 = Integer.parseInt(strC2, 16);
                    //System.out.printf("int: c1 = %d; c2 = %d", c1, c2);
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
                        //System.out.printf("%nc3 = %s; int3: %d%n", strC3, c3);
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

        /**
         * This is lightweight realisation of Reader`s readline;
         * Checks inputStream in lines
         */

        StringBuilder l = new StringBuilder();
        while (true) {
            if (io.available()!= 0) {
                while (ch != -1) {
                    ch = io.read();
                    if (ch == -1) return "";
                    if ((char) ch != '\n') {
                        l.append((char) ch);
                    } else {
                        if (l.length() > 0 && l.charAt(l.length() - 1) == '\r')
                            l.deleteCharAt(l.length() - 1);
                        return l.toString();
                    }
                }
            }
        }

    }

    private void nodeCollect() {

        /*
          This method take all prims, convert text and sort
         */

        nodes.add(waitingNode);

        WordList list = new WordList();
        list.setWlName(newWlName);

        ThreadGroup group = new ThreadGroup("Converting");

        for (final Node node : nodes) {

            Thread thread = new Thread(group, new Runnable() {
                @Override
                public void run() {
                    node.convertText(converter);
                }
            });

            thread.start();
        }

        while (group.activeCount() > 0) {
        }

        NavActivity.database.nodeDao().insertAll(nodes);
        NavActivity.database.listDao().insertList(list);



        Message message = new Message();

        message.what = NavActivity.HANDLE_MESSAGE_EXTRACTED;

        Bundle data = new Bundle();

        data.putString(NavActivity.WL_NAME, newWlName);

        message.setData(data);

        NavActivity.h.sendMessage(message);


    }

    private class TextExtractor {

        private Lang curLang;

        private Lang getStringLang(String text) {
            int i = 0;
            Lang lang = Lang.UNDEFINED;
            while (i < text.length() && !(lang == Lang.ENG || lang == Lang.BRACE)) {
                lang = LangChecker.langCheck(text.charAt(i++));
            }
            return lang;
        }

        private String extractRawText() throws IOException {
            /**
             * This fills Node with raw text
             *
             */

            ch = io.read();
            while ((char) ch != '[') {
                lineStr = readLine();
                ch = io.read();
            }


            StringBuilder text = new StringBuilder();
            while ((char) ch != ']') {
                while (((char) ch != '(') && ((char) ch != '<')) {
                    // skip all text out brackets
                    ch = io.read();
                }
                if ((char) ch == '<') {
                    curLang = Lang.BRACE;
                    text.append((char) ch);
                    while ((char) ch != '>') {
                        ch = io.read();
                        text.append((char) ch);
                    }
                } else {
                    curLang = Lang.UNDEFINED;
                    ch = io.read();
                    while ((char) ch != ')') {
                        if (curLang != Lang.ENG && LangChecker.langCheck((char) ch) == Lang.ENG)
                            curLang = Lang.ENG;
                        text.append((char) ch);
                        ch = io.read();
                    }
                }
                ch = io.read();
            }

            return text.toString();

        }

        private void getCord4Node(Node node) throws IOException {
            // Get current coordinates and pass it to node

            ch = io.read();
            while ((char) ch != '[') {
                lineStr = readLine();
                if (lineStr.endsWith("Tm")) {
                    String[] cord = lineStr.split(" ");
                    node.setX(Double.parseDouble(cord[4]));
                    node.setY(Double.parseDouble(cord[5]));

                }
                ch = io.read();
            }
        }

        private void textToken() throws IOException {

            /**
             * This Method extract text and passes it to Node
             */

            //Node node = new Node();
            //getCord4Node(node);

            String text = extractRawText();

            if (newWlName == null) {
                newWlName = text.trim().replaceAll(" ", "_").replaceAll(":", "");

                for (String s : NavActivity.database.listDao().loadNames()) {
                    if (newWlName.equals(s)) {
                        isExists = true;
                    }
                }
            } else {

                delegateText(text);

            }


        }

        private void delegateText(String text) {

            Lang nodeLang = curLang;

            //System.out.println(text + "\t" + curLang);

            if (waitingNode == null) {

                waitingNode = new Node();
                waitingNode.setWlName(newWlName);
                waitingNode.setPrimText(text);
                waitingNodeLang = Lang.ENG;

            } else {
                if (nodeLang == (waitingNodeLang == Lang.ENG ? Lang.BRACE : Lang.ENG)) {


                    waitingNode.setWlName(newWlName);
                    if (nodeLang == Lang.ENG) {

                        nodes.add(waitingNode);

                        waitingNode = new Node();

                        waitingNode.setPrimText(text);

                    } else waitingNode.setTransText(text);
                    waitingNodeLang = nodeLang;

                } else {
                    if (waitingNodeLang == Lang.ENG)
                        waitingNode.setPrimText(waitingNode.getPrimText().concat(text));
                    else waitingNode.setTransText(waitingNode.getTransText().concat(text));
                }

            }
        }
    }
}
