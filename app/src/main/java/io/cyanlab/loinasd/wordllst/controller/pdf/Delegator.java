package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.os.Bundle;
import android.os.Message;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

import io.cyanlab.loinasd.wordllst.activities.NavActivity;
import io.cyanlab.loinasd.wordllst.activities.ShowFragment;


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
    private int proggress;
    private ArrayList<Node> nodes;
    private Node waitingNode;
    private Lang waitingNodeLang;
    private TextExtractor extractor;

    final private int COINS_TO_SET_X = 10;

    private double engX;

    private boolean isRusXSet;
    private double rusX;
    private int rusXCoins;
    private int rusXErr;

    private static Logger log = Logger.getLogger(Delegator.class.getName());

    private void updateProgress() {
        proggress++;
    }

    public static Logger getLog() {
        return log;
    }

    /**
     * Main extraction method. Extracts text from @io
     *
     * @param io
     */


    public void extract(final PipedInputStream io) {

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
            } else if (isExists) {
                NavActivity.h.sendEmptyMessage(NavActivity.HANDLE_MESSAGE_EXISTS);
            } else {
                NavActivity.h.sendEmptyMessage(NavActivity.HANDLE_MESSAGE_NOT_EXTRACTED);
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        log.warning("Delegator works in ms:" + (System.currentTimeMillis() - startTime));
        //System.out.printf("Delegator works %d ms", System.currentTimeMillis() - startTime);
    }

    /**
     * Finds BT-ET blocks and sends contents to @TextExtractor, also searches for CMap
     * @throws IOException
     */
    private void parse() throws IOException {

        while (ch != -1) {
            ch = io.read();

            if (ch == 'B') {
                ch = io.read();
                if (ch == 'T') {
                    extractor.textToken();
                }
            } else if (ch =='b'){
                ch = io.read();
                if (ch == 'e') {
                    String line = readLine();
                    if (line.equals("gincmap"))
                        parseCMap();
                }
            }
        }
    }

    /**
     * Parses CMap and fills @CharConverters @Ranges
     * @throws IOException
     */
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

    /**
     * Reads line from @inputStream. Lightweight realization of @Readers @readLine;
     * @return Line
     * @throws IOException
     */
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

    /**
     * Method, called when the whole text has been read.
     * Converts text within Nodes with CharConverter using personal thread for each Node
     */
    private void nodeCollect() {

        /*
          This method take all prims, convert text and sort
         */

        nodes.add(waitingNode);

        WordList list = new WordList();
        list.setWlName(newWlName);
        list.maxWeight = list.currentWeight = nodes.size() * ShowFragment.RIGHT_ANSWERS_TO_COMPLETE;

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


    /**
     * Class that extracts text from InputStream and fills Nodes with it
     */
    private class TextExtractor {

        double x;

        /**
         * Special number to describe the error of X arrangement
         */
        final int xArea = 20;

        ArrayList<TextPlusX> textBuffer = new ArrayList<>();

        /**
         *This method finds proper Translation X.
         * While proper X is not found, text and its X are stored in @textBuffer.
         */
        private void handleX(double X) {
            if (rusX != 0.0) {
                if (rusX == X) {
                    rusXCoins++;
                    rusXErr--;
                } else {
                    rusXErr++;
                }
                if (rusXCoins == COINS_TO_SET_X) {
                    isRusXSet = true;
                    loadBuffer();
                }
                if (rusXErr == COINS_TO_SET_X) {
                    rusXErr = 0;
                    rusXCoins = 0;
                    rusX = X;
                }
            } else {
                rusX = X;
            }
        }

        /**
         * When proper X is found, creates nodes that were in buffer.
         */
        private void loadBuffer() {
            for (TextPlusX textPlusX : textBuffer) {

                if (waitingNode == null) {

                    waitingNode = new Node();
                    waitingNode.setWlName(newWlName);

                    waitingNode.setWeight(ShowFragment.RIGHT_ANSWERS_TO_COMPLETE);

                    waitingNode.setPrimText(textPlusX.getText());
                    waitingNodeLang = Lang.ENG;

                    continue;
                }

                delegateText(textPlusX.getText(), textPlusX.getX());

            }


        }

        /**
         * Keeps recently read texts language;
         */
        private Lang curLang;

        /**
         * Extracts text from IS
         * Sets current Text Language @curLang
         * @return @String extracted text
         */
        private String extractRawText() throws IOException {

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

                        if (curLang != Lang.ENG && (LangChecker.langCheck((char) ch) == Lang.NUM) || ((char) ch == '.')) {
                            ch = io.read();
                            continue;
                        }

                        text.append(ch != 47 ? (char) ch : ',');
                        ch = io.read();
                    }
                }
                ch = io.read();
            }

            return text.toString();

        }

        /**
         * Extracts texts X coordinate
         * @return
         */
        private double getNodeX() {
            // Get current coordinates and pass it to node

            try {
                ch = io.read();
                while ((char) ch != '[') {
                    lineStr = readLine();
                    if (lineStr.endsWith("Tm")) {
                        String[] cord = lineStr.split(" ");
                        return Double.parseDouble(cord[4]);
                    }
                    ch = io.read();
                }
            } catch (IOException e) {
                return -1;
            }
            return -1;
        }

        /**
         * Calls all other methods of this class
         *
         * The result of its work is a Node, filled with raw text
         */
        private void textToken() throws IOException {

            /**
             * This Method extract text and passes it to Node
             */

            //Node node = new Node();
            x = getNodeX();

            String text = extractRawText();

            if (newWlName == null) {
                newWlName = text.trim().replaceAll(" ", "_").replaceAll(":", "");

                for (String s : NavActivity.database.listDao().loadNames()) {
                    if (newWlName.equals(s)) {
                        isExists = true;
                    }
                }
            } else {

                Lang nodeLang = curLang;

                if (!isRusXSet && nodeLang == Lang.BRACE) {
                    handleX(x);
                }

                if (!isRusXSet) {
                    textBuffer.add(new TextPlusX(text, x, nodeLang));

                } else {
                    delegateText(text, x);
                }


            }


        }

        /**
         * In most common cases creates a Node filled with text
         * <p>
         * Also fills unfinished Nodes with translation texts
         *
         * @param text
         * @param x
         */
        private void delegateText(String text, double x) {

            Lang nodeLang = (x >= engX && x < rusX - xArea) ? Lang.ENG : Lang.RUS;

            if (nodeLang == (waitingNodeLang == Lang.ENG ? Lang.RUS : Lang.ENG)) {

                waitingNode.setWlName(newWlName);
                if (nodeLang == Lang.ENG) {

                    nodes.add(waitingNode);

                    waitingNode = new Node();

                    waitingNode.setWeight(ShowFragment.RIGHT_ANSWERS_TO_COMPLETE);

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

    /**
     * Class, used to store info about buffered text in @TextExtractor
     */
    private class TextPlusX {

        private String text;
        private double X;
        private Lang textLang;

        TextPlusX(String text, double x, Lang lang) {
            this.text = text;
            this.X = x;
            textLang = lang;
        }

        String getText() {
            return text;
        }

        double getX() {
            return X;
        }

        Lang getTextLang() {
            return textLang;
        }


    }
}
