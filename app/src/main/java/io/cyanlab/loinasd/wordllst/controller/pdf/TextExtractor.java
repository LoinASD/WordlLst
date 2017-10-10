package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import io.cyanlab.loinasd.wordllst.model.Facade;


public class TextExtractor {

    private static int ch;
    private final BufferedInputStream io;
    private ArrayList<Node> nodes = new ArrayList<>();
    private CharConverter converter = new CharConverter();
    private static String lineStr;
    private static boolean gotDictionary = false;

    public TextExtractor(InputStream io) throws IOException {
        this.io = new BufferedInputStream(io);
        ch = 0;

        parse();
        if (gotDictionary){
            for (Node node: nodes) {
                node.convertText(converter);

            }
        }
        nodeCollect();
        for (Node node : nodes) {
            System.out.println(node.getText());
        }

    }

    private void parse() throws IOException {
        while (ch != -1) {
            lineStr = readLine();
            if (lineStr.equals("BT")) {
                textToken();
            } else if (lineStr.equals("begincmap")){
                parseCMap();
            }

        }
    }

    private void parseCMap() throws IOException {
        converter = new CharConverter();
        String[] chars;
        int count = 0;
        while (ch != -1) {
            lineStr = readLine();
            if (lineStr.endsWith("beginbfchar")){
                count = Integer.parseInt("" + lineStr.charAt(0));
                for (int i = count; i > 0; i--) {
                    lineStr = readLine();
                    chars = lineStr.split(" ");
                    String s = chars[0].substring(1, 5);
                    int c = Integer.parseInt(s, 16);
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
                    int c3 = Integer.parseInt(chars[2].substring(1, 5), 16);
                    converter.addNewRange(c1, c2, c3);
                }
            }

            if (lineStr.equals("endcmap")) {
                gotDictionary = true;
                break;
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
            if (ch == -1) return "";
            if ((char) ch != '\n') {
                l.append((char) ch);
            } else {
                return l.toString();
            }
        }
        return l.toString();
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

    private void nodeCollect() {
        ArrayList<Node> newNodes = new ArrayList<>();
        boolean b = false;
        Node prev = nodes.get(0);
        for (Node node: nodes) {
            if (b) {
                prev.setText(prev.getText() + node.getText());
                b = false;
                continue;
            }
            if (node.getText().trim().equals("-")) {
                prev.setText(prev.getText() + node.getText());
                b = true;
                continue;
            }
            if (!node.getText().equals(" ")) {
                newNodes.add(node);
                prev = node;
            }
        }
        nodes = newNodes;
        boolean toggle = false;
        ArrayList<String> p = new ArrayList<>(), t = new ArrayList<>();
        String WLname = nodes.remove(0).getText();
        for (Node n : nodes) {
            if (toggle) {
                t.add(n.getText());
                toggle = false;
            }
            if (!toggle) {
                p.add(n.getText());
                toggle = true;
            }
        }
        Facade.getFacade().addNewWL(WLname,p, t);
    }
}
