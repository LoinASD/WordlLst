package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import io.cyanlab.loinasd.wordllst.activities.MainActivity;
import io.cyanlab.loinasd.wordllst.activities.ShowFragment;
import io.cyanlab.loinasd.wordllst.controller.database.DataProvider;

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

    private boolean isOldVersion;

    final private int COINS_TO_SET_X = 10;

    public final static String MESSAGE_DB_ERROR = "DB is null";
    public final static String MESSAGE_EXISTS = "List already exists";
    public final static String MESSAGE_NO_DICT = "No dictionary found";
    public final static String MESSAGE_IO_EXCEPTION = "No dictionary found";

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
     *
     * Main extraction method. Extracts text from @io
     *
     * @param io
     * @return Wordlist if successfully extracted else String with message;
     */
    public Object extract(final PipedInputStream io) {

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

                WordList list = nodeCollect();

                return list != null ? list : MESSAGE_DB_ERROR;

            } else if (isExists) {

                return MESSAGE_EXISTS;
            } else {

                return MESSAGE_NO_DICT;
            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        return MESSAGE_IO_EXCEPTION;
    }

    /**
     * Finds BT-ET blocks and sends contents to @TextExtractor, also searches for CMap
     * @throws IOException
     */
    private void parse() throws IOException {

        if ((char)io.read() == '3') isOldVersion = true;

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

    private void convertName(){

        if (newWlName != null && newWlName.length() > 0){

            Node nameNode = new Node();
            nameNode.primText = (newWlName);

            convertText(converter, nameNode);

            newWlName = nameNode.primText;

            if (!DataProvider.isBaseLoaded)
                return;

            List<String> names = DataProvider.loadListNames();

            if (names == null)
                return;

            for (String s : names) {
                if (newWlName.equals(s)) {
                    isExists = true;
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

                // range looks like <0000> <FFFF> or <00><FF>
                range = readLine().contains("<0000>") ? 4 : 2;
            }

            if (lineStr.endsWith("beginbfchar")){
                count = Integer.parseInt("" + lineStr.charAt(0));
                for (int i = count; i > 0; i--) {
                    lineStr = readLine();
                    chars = lineStr.split("<");
                    int c1 = Integer.parseInt(chars[1].substring(0, range), 16);
                    int c2 = Integer.parseInt(chars[2].substring(0, range), 16);
                    converter.addNewRange(c1, c1, c2);
                }
            }
            if (lineStr.endsWith("beginbfrange")) {
                count = Integer.parseInt("" + lineStr.charAt(0));
                for (int i = count; i > 0; i--) {
                    lineStr = readLine();
                    chars = lineStr.split("<");
                    String strC1 = chars[1].substring(0, range);
                    String strC2 = chars[2].substring(0, range);
                    //System.out.printf("%nc1 = %s; c2 = %s%n", strC1, strC2);
                    int c1 = Integer.parseInt(strC1, 16);
                    int c2 = Integer.parseInt(strC2, 16);
                    //System.out.printf("int: c1 = %d; c2 = %d", c1, c2);
                    if (lineStr.contains("[")) {
                        String[] arr = lineStr.substring(
                                lineStr.indexOf('[') + 1, lineStr.length() - 1).split("<");
                        int c, dif = c1;
                        for (String s: arr) {
                            if (!s.equals("")) {
                                c = Integer.parseInt(s.substring(0, range), 16);
                                converter.addNewRange(dif, dif, c);
                                dif++;
                            }
                        }
                    } else {
                        String strC3 = chars[3].substring(0, range);
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

    @Nullable
    private WordList nodeCollect() {

        /*
          This method take all prims, convert text and sort
         */

        nodes.add(waitingNode);

        WordList list = new WordList();
        list.name = newWlName;

        ThreadGroup group = new ThreadGroup("Converting");

        for (final Node node : nodes) {

            Thread thread = new Thread(group, () -> {

                if (node == null){

                    nodes.remove(node);

                    return;
                }


                CharConverter converter = new CharConverter();

                converter.ranges = this.converter.ranges;

                convertText(converter,node);
            });

            thread.start();
        }

        while (group.activeCount() > 0) {
        }

        if (!DataProvider.isBaseLoaded)
            return null;

        DataProvider.insertList(list);
        DataProvider.insertAllNodes(nodes);

        return DataProvider.getList(newWlName);
    }

    void convertText(CharConverter converter, Node node) {

        if (node == null)
            return;

        for (int j = 0; j < 2; j++) {

            String text = (j == 0 ? node.transText : node.primText);

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

                        for (int k = 0; k < 4; k++) {// 4 - char`s length in HEX

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
                if (j == 0)
                    node.transText = message.toString();
                else
                    node.primText = message.toString();
            }

        }

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
                    waitingNode.wlName = newWlName;

                    waitingNode.primText = textPlusX.getText();
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
            while ((char) ch != ']' && ch != -1) {
                while (((char) ch != '(') && ((char) ch != '<') && ch != -1 && (char)ch != ']') {
                    // skip all text which is out of brackets
                    ch = io.read();
                }
                if ((char) ch == '<') {
                    curLang = Lang.BRACE;
                    text.append((char) ch);
                    while ((char) ch != '>') {
                        ch = io.read();
                        text.append((char) ch);
                    }
                } else if ((char)ch == '('){
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
                if ((char) ch == ']') break;
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

        StringBuilder nameBuf;
        double xBuf;

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

            if (!isOldVersion) {
                if (newWlName == null) {

                    newWlName = text.trim();

                    List<String> names = DataProvider.loadListNames();

                    if (names == null)
                        return;
                    
                    for (String s : names) {
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
            }else {
                if (newWlName == null){
                    if (nameBuf == null){
                        rusX = 302.14d;
                        nameBuf = new StringBuilder(text);
                        xBuf = x;
                    }else if (x > xBuf){
                        xBuf = x;
                        nameBuf.append(text);
                    }else{

                        newWlName = nameBuf.toString();
                        convertName();

                    }
                }else{

                    if (waitingNode == null) {

                        waitingNode = new Node();
                        waitingNode.wlName = newWlName;

                        waitingNode.primText = text;
                        waitingNodeLang = Lang.ENG;

                    }

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

                waitingNode.wlName = newWlName;
                if (nodeLang == Lang.ENG) {

                    nodes.add(waitingNode);

                    waitingNode = new Node();

                    waitingNode.primText = text;

                } else waitingNode.transText = text;
                waitingNodeLang = nodeLang;

            } else {
                if (waitingNodeLang == Lang.ENG)
                    waitingNode.primText = waitingNode.primText.concat(text);
                else waitingNode.transText = waitingNode.transText.concat(text);
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
