package io.cyanlab.loinasd.wordllst.model;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public final class PDFParser {

    GZIPInputStream gis;
    private static PDFParser pdfParser;
    private static final String markerStream = "stream";
    private static final String markerObj = "obj";
    private static final String markerDecode = "/Filter/FlateDecode/Length";
    private static final String markerLength = "Length";
    private static BufferedReader r;
    private static int ch;
    private static char cc;
    private static String file;
    private static int countChar = 0;
    private static int currentLength;


    private PDFParser(){}

    public static PDFParser getParser() {
        if (pdfParser == null) pdfParser = new PDFParser();
        return pdfParser;
    }

    public static void parse() throws IOException{
        ch = 0;
        while (readMarker(markerObj)) {
            readObj();
        }

    }

    private static class Obj {
        int length;
        StringBuffer message;

        Obj() {

        }
    }

    private static void readObj() throws IOException {
        if (readMarker(markerDecode)) {
            currentLength = 0;


        }

    }

    private static boolean readMarker(String marker) throws IOException {

        outer:   while (ch > -1) {
            ch = r.read();    cc = (char) ch;

            if (cc == marker.charAt(0)) {

                for (int i = 1; i < marker.length(); i++) {

                    if (i == 3)  {
                        System.out.println();
                    }

                    cc = (char) r.read();
                    if (cc != marker.charAt(i)) continue outer;

                }
                return true;

            }
        }
        return false;
    }

    private String readText(String stopTag) throws IOException {
        StringBuilder buf = new StringBuilder();
        ch = r.read();    cc = (char) ch;
        countChars++;
        //fullPage.append(cc);
        if (cc == '<') return "";

        boolean gotTagEnd = false;
        while (!gotTagEnd) {

            if (cc == '<') {
                String tag = readTag();
                if (tag.equals(stopTag)) gotTagEnd = true;
                else if (tag.equals("<br/>")) buf.append('\n');
            }
            if (cc == '&') {
                buf.append(replaceSpecialSymbols(readSpec()));
            } else if (!gotTagEnd){
                buf.append(cc);
                ch = r.read();    cc = (char) ch;
                countChars++;
                //fullPage.append(cc);
                if (ch == 13) ch = 10;
            }
        }
        return new String(buf);
    }

    /*private static boolean hasMoreObj() throws IOException {
        outer: while (ch > -1) {
            ch = r.read(); cc = (char) ch;
            if ( cc == markerObj.charAt(0)) {
                for (int i = 0; i < markerObj.length(); i++) {

                    if (i == 4)  {
                        System.out.println();
                    }

                    cc = (char) r.read();
                    if ( cc != markerObj.charAt(i)) continue outer;
                }
                return true;
            }
        }
        return false;
    }*/
}
