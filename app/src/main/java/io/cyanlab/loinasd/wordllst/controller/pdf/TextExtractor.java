package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class TextExtractor {

    private static int ch;
    private final InputStream io;
    private ArrayList<Node> nodes = new ArrayList<>();
    private CharConverter converter = new CharConverter();
    private static String lineStr;
    private static boolean gotDictionary = false;

    public TextExtractor(InputStream io) throws IOException {
        this.io = io;
        ch = 0;

        parse();
        if (gotDictionary){
            for (Node node: nodes) {
                node.convertText(converter);
                System.out.println(node.getText());
            }
        }


    }

    private void parse() throws IOException {
        while (ch != -1) {
            lineStr = readLine();
            if (lineStr.equals("BT")) {
                textToken();
            } else if (lineStr.equals("begincmap")){
                //TODO: parse cmap
            }

        }
    }

    private void getCord(Node node) throws IOException {
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

    private String readLine() throws IOException {
        StringBuilder l = new StringBuilder();
        while (ch != -1) {
            ch = io.read();

            if ((char) ch != '\n') {
                l.append((char) ch);
            } else {
                return l.toString();
            }
        }
        return null;
    }

    private void textToken() throws IOException {
        StringBuilder message = new StringBuilder();
        Node node = new Node();
        getCord(node);
        ch = io.read();
        while ((char) ch != ']'){
            message.append((char) ch);
            ch = io.read();
        }
        node.setRawText(message.toString().trim());
        nodes.add(node);
    }
}
