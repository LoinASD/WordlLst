package io.cyanlab.loinasd.wordllst.controller.pdf;

public class Node {

    private double x;
    private double y;
    private String rawText;
    private String text;

    Node(double x, double y){
        this.x = x;
        this.y = y;
    }

    Node() {}

    public void convertText(CharConverter converter) {
        char cc;
        StringBuilder message = new StringBuilder();
        String[] stringarr = rawText.split("[(]");
        for (String s : stringarr) {
            s= s.split("[)]")[0];
            message.append(s);
        }
        System.out.println(message);
        int i = 0;
        cc = rawText.charAt(i);
        while (i < rawText.length()) {
            if (cc == '<') {
                cc = rawText.charAt(++i);
                StringBuilder numChar;

                while (cc != '>') {
                    numChar = new StringBuilder();
                    for (int j = 0; j < 4; j++) { // 4 - char`s length in HEX
                        numChar.append(cc);
                        cc = rawText.charAt(++i);
                    }
                    int c = Integer.parseInt(numChar.toString(), 16);
                    message.append(converter.convert(c));
                }
            }
        }

        this.text = message.toString();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getText() {
        return text;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setRawText(String text) {
        this.rawText = text;
    }
}

