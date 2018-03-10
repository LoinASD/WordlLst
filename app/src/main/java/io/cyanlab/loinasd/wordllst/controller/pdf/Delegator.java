package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;

import io.cyanlab.loinasd.wordllst.controller.DBHelper;


public class Delegator {

    private static int ch;
    private PipedInputStream io;
    private CharConverter converter = new CharConverter();
    private static String lineStr;
    private static boolean gotDictionary = false;
    private String newWlName;
    private boolean isExists = false;
    //private StringBuilder text;
    private int range;
    private DBHelper dbHelper;
    private int proggress;
    private ArrayList<Node> nodes = new ArrayList<>();

    private void updateProgress() {
        proggress++;
    }


    public void extract(final PipedInputStream io, DBHelper dbHelper) {
        /**
         * This Method extracts Nodes with text from PipedInputStream
         * Works with DBHelper
         * This is Main Method
         */

        this.io = io;
        this.dbHelper = dbHelper;

        try {
            parse();
            if ((gotDictionary) && (!isExists)) {
                nodeCollect();
            } else {
                //MainActivity.h.sendEmptyMessage(4);
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void parse() throws IOException {
        /**
         * This Method finds Text blocks within tags BT and ET
         * also this method finds cmap
         */

        int buff;
        boolean isDone = false;

        while (ch != -1) {
            ch = io.read();

            if (ch == 'B') {
                ch = io.read();
                if (ch == 'T') {
                    io.read();
                    final PipedOutputStream nodeOut;
                    final PipedInputStream nodeIn;
                    Thread bundle;
                    try {
                        nodeOut = new PipedOutputStream();
                        nodeIn = new PipedInputStream(nodeOut);

                        bundle = new Thread(new TextExtractor(nodeIn));
                        bundle.start();
                        while (!isDone) {
                            nodeOut.write(ch);
                            ch = io.read();
                            if (ch == 'E') {
                                buff = ch;
                                ch = io.read();
                                if (ch == 'T')
                                    isDone = true;
                                else nodeOut.write(buff);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
          This method take all nodes, convert text and sort
         */


        WordList list = new WordList()

    }

    private class TextExtractor implements Runnable{

        PipedInputStream io;
        TextExtractor(final PipedInputStream io) {
            this.io = io;
        }

        @Override
        public void run() {
            try {
                textToken();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private String extractRawText() throws IOException {
            /**
             * This Method returns a String with rew text
             */
            StringBuilder text = new StringBuilder();
            while ((char) ch != ']') {
                while (((char) ch != '(') && ((char) ch != '<')) {
                    // skip all text out brackets
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
            return text.toString();
        }

        @Nullable
        private Node getCord() throws IOException {
            // Get current coordinates
            ch = io.read();
            Node node = null;
            while ((char) ch != '[') {
                lineStr = readLine();
                if (lineStr.endsWith("Tm")) {
                    String[] cord = lineStr.split(" ");
                    node = new Node(Double.parseDouble(cord[4]), Double.parseDouble(cord[5]));
                }
                ch = io.read();
            }
            return node;
        }

        private void textToken() throws IOException {

            /**
             * This Method extract text and gives it to Node
             */

            //Node node = getCord();
            Node node = new Node();
            node.setRawText(extractRawText());
            if (newWlName == null) {
                node.setHead(true);
                newWlName = node.getRawText().trim().replaceAll(" ", "_").replaceAll(":", "");

                for (String s : dbHelper.loadWlsNames()) {
                    if (newWlName.equals(s)) {
                        isExists = true;
                    }
                }
            }

            nodes.add(node);
        }
    }
}
