package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class TextExtractor {

    private static int ch;
    private static char cc;
    private final InputStream io;
    private ArrayList<String> ar = new ArrayList<>();
    private ArrayList<Node> nodes = new ArrayList<>();
    private StringBuilder prev = new StringBuilder();
    private CharConverter converter = new CharConverter();

    public TextExtractor(InputStream io) throws IOException {
        this.io = io;
        ch = 0;
        while (ch != -1) {
            ch = io.read();
            cc = (char) ch;
            if ((char) ch == 'B') {
                ch = io.read();
                cc = (char) ch;
                if ((char) ch == 'T') {
                    textToken();
                }
            }
        }
        for (Node node: nodes) {
            node.convertText(converter);
            System.out.println(node.getText());
        }

    }

    private void textToken() throws IOException {
        StringBuilder message = new StringBuilder();
        StringBuffer lineBuf = new StringBuffer();
        String line;
        Node node = new Node();
        //node.convertText(converter);
        ch = io.read();

        while ((char) ch != '[') {
            cc = (char) ch;
            if (cc != '\n') {
                lineBuf.append(cc);
            } else {
                line = lineBuf.toString();
                lineBuf = new StringBuffer();
                if (line.endsWith("Tm")) {
                    String[] cord = line.split(" ");
                    node.setX(Double.parseDouble(cord[4]));
                    node.setY(Double.parseDouble(cord[5]));
                }
            }
            ch = io.read();
        }
        ch = io.read();
        while ((char) ch != ']'){
            message.append((char) ch);
        }
        node.setRawText(message.toString().trim());
        nodes.add(node);
    }
}
