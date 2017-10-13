package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.content.Intent;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import io.cyanlab.loinasd.wordllst.model.Facade;


public class TextExtractor {

    private static int ch;
    private BufferedInputStream io;
    private ArrayList<Node> nodes = new ArrayList<>();
    private CharConverter converter = new CharConverter();
    private static String lineStr;
    private static boolean gotDictionary = false;

    private static TextExtractor instance;

    public static TextExtractor getExtractor(){
        if(instance == null){
            instance = new TextExtractor();
        }
        return instance;
    }

    private TextExtractor(){}

    public void extract(InputStream io) {
        this.io = new BufferedInputStream(io);
        ch = 0;

        try {
            parse();
            if (gotDictionary){
                for (Node node: nodes) {
                    node.convertText(converter);
                }
                nodeCollect();
                for (Node node : nodes) {
                    System.out.println(node.getText());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

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
                if (l.charAt(l.length() - 1) == '\r')
                    l.deleteCharAt(l.length() - 1);
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
        nodes.trimToSize();
        //------DELETE EMPTY NODES AND CONCAT NODES WITH HYPHEN---??
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
        //-------------------------------------------------//

        //-------SPREADING------------------------//
        boolean toggle = false;
        ArrayList<String> left = new ArrayList<>(),
                right = new ArrayList<>(),
                center = new ArrayList<>();

        //String WLname = nodes.remove(0).getText();

        Node head = nodes.get(0);
        boolean notAHead = false;
        for (Node n : nodes) {
            if (n.getY() == head.getY()) notAHead = true;
            if (n.getX() < 300) left.add(n.getText());
            else right.add(n.getText());
        }

        //--------------------------------------------//
        Facade.getFacade().addNewWL(head.getText(),left, right);
    }
}
