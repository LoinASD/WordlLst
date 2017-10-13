package io.cyanlab.loinasd.wordllst.controller.pdf;

class Node {
    private double x;
    private double y;
    private String rawText;
    private String text;
    private boolean isConverted = false;

    Node(double x, double y){
        this.x = x;
        this.y = y;
    }

    Node() {}

    public void convertText(CharConverter converter) {
        char cc;
        StringBuilder message = new StringBuilder();
        if (rawText.charAt(0) == '(') {
            String[] stringarr = rawText.split("[(]");
            for (String s : stringarr) {
                String[] sS= s.split("[)]");
                if (sS.length>0){
                    s = sS[0];
                }
                message.append(s);
            }
        } else if (rawText.charAt(0) == '<') {
            int i = 0;

            while (i < rawText.length()) {
                cc = rawText.charAt(i);
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
                i++;
            }
        }
        this.text = message.toString();
        this.isConverted = true;
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

    public void setText(String text) {this.text = text;}

    private boolean isConverted() {
        return isConverted;
    }
}

