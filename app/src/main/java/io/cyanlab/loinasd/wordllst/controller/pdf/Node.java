package io.cyanlab.loinasd.wordllst.controller.pdf;

class Node {
    private double x;
    private double y;
    private String text;

    Node() {}

    public void convertText(CharConverter converter) {
        char cc;
        StringBuilder message = new StringBuilder();
        if (text.charAt(0) == '(') {
            String[] stringarr = text.split("[(]");
            for (String s : stringarr) {
                s= s.split("[)]")[0];
                message.append(s);
            }
        } else if (text.charAt(0) == '<') {
            int i = 0;

            while (i < text.length()) {
                cc = text.charAt(i);
                if (cc == '<') {
                    cc = text.charAt(++i);
                    StringBuilder numChar;

                    while (cc != '>') {
                        numChar = new StringBuilder();
                        for (int j = 0; j < 4; j++) { // 4 - char`s length in HEX
                            numChar.append(cc);
                            cc = text.charAt(++i);
                        }
                        int c = Integer.parseInt(numChar.toString(), 16);
                        message.append(converter.convert(c));
                    }
                }
                i++;
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

    public void setText(String text) {this.text = text;}


}
