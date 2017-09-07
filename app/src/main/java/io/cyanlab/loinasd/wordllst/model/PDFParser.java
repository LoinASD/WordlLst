package io.cyanlab.loinasd.wordllst.model;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public final class PDFParser {

    GZIPInputStream gis;
    private static PDFParser pdfParser;
    private static String markerStr = "stream";
    private static BufferedReader r;

    private static String file;


    private PDFParser(){}

    public static PDFParser getParser() {
        if (pdfParser == null) pdfParser = new PDFParser();
        return pdfParser;
    }

    public static void parse(InputStreamReader isr) throws IOException{

    }
}
