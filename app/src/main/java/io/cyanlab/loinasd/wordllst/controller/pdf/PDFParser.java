package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

class PDFParser {
    private static int ch;
    private static char cc;
    private static final String markerStream = "stream";
    private static final String markerEndStream = "endstream";
    private static final String markerObj = "obj";
    private static final String markerEndObj = "endobj";
    //private static final String markerDecode = "/Filter/FlateDecode/Length";
    private static final String markerLength = "Length";


    /*Парсит все в этой жизни, пишет в 1 поток.
    Надо будет еще раз все посмотреть и убрать костыли.*/

    private static void parsePdf(String file, OutputStream out) {
        try {
            File pdf = new File(file);
            BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(pdf));
            cc = (char) bufInput.read();
            while ((bufInput.available() != 0)) {
                cc = (char) bufInput.read();
                char[] marker = markerObj.toCharArray();
                if (cc == marker[0]) {
                    boolean isObj = true;
                    for (int j = 1; j < marker.length; j++) {
                        cc = (char) bufInput.read();
                        if (cc != marker[j]) isObj = false;
                    }
                    if (!isObj) {
                        continue;
                    } else {
                        boolean isObjEnd = false;
                        boolean isFonts = false;
                        int streamLength = 0;
                        while ((cc != '<') && (!isObjEnd)) {
                            if (cc == '\n') {
                                cc = (char) bufInput.read();
                                if (cc == '<') break;
                                else {
                                    isObjEnd = true;
                                    for (int i = 0; i < markerEndObj.length(); i++) {
                                        cc = (char) bufInput.read();
                                        if (cc != markerEndObj.charAt(i)) {
                                            isObjEnd = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            cc = (char) bufInput.read();
                        }
                        if (isObjEnd) {
                            continue;
                        }
                        while ((cc != '>') && (!isFonts)) {
                            char[] marker2 = markerLength.toCharArray();
                            cc = (char) bufInput.read();

                            if (cc == marker2[0]) {
                                boolean isLength = true;
                                for (int j = 1; j < marker2.length; j++) {
                                    cc = (char) bufInput.read();
                                    if (cc != marker2[j]) isLength = false;
                                }
                                if (isLength) {
                                    StringBuffer res = new StringBuffer(6);
                                    bufInput.read();
                                    cc = (char) bufInput.read();
                                    while ((cc != '>') && (cc != '/')) {
                                        res.append(cc);
                                        cc = (char) bufInput.read();
                                    }
                                    if (cc == '/') {
                                        isFonts = true;
                                    } else {
                                        try {
                                            streamLength = Integer.parseInt(res.toString());
                                        } catch (NumberFormatException e) {
                                            isFonts = true;
                                        }
                                    }
                                }
                            }
                            if (cc == '>') cc = (char) bufInput.read();
                        }
                        if ((isFonts) || (streamLength <= 0)) {
                            marker = markerEndObj.toCharArray();
                            while (!isObjEnd) {
                                cc = (char) bufInput.read();
                                if (cc == marker[0]) {
                                    isObjEnd = true;
                                    for (int j = 1; j < marker.length; j++) {
                                        cc = (char) bufInput.read();
                                        if (cc != marker[j]) isObjEnd = false;
                                    }
                                }
                            }
                            continue;

                        } else {
                            marker = markerStream.toCharArray();
                            boolean isStream = false;
                            while ((!isStream) && (!isObjEnd)) {

                                if (cc == marker[0]) {
                                    isStream = true;
                                    for (int j = 1; j < marker.length; j++) {
                                        cc = (char) bufInput.read();
                                        if (cc != marker[j]) {
                                            isStream = false;
                                            break;
                                        }
                                    }
                                    if (isStream) break;
                                }
                                if (cc == '\n') {
                                    cc = (char) bufInput.read();
                                    if (cc == 's') continue;
                                    else {
                                        for (int i = 0; i < markerEndObj.length(); i++) {
                                            cc = (char) bufInput.read();
                                            if (cc != markerEndObj.charAt(i)) isObjEnd = false;
                                        }
                                    }
                                }
                                cc = (char) bufInput.read();
                            }
                            if (isObjEnd) {
                                continue;
                            }
                            if (!isStream) {
                                marker = markerEndObj.toCharArray();
                                while (!isObjEnd) {
                                    cc = (char) bufInput.read();
                                    if (cc == marker[0]) {
                                        isObjEnd = true;
                                        for (int j = 1; j < marker.length; j++) {
                                            cc = (char) bufInput.read();
                                            if (cc != marker[j]) isObjEnd = false;
                                        }
                                    }
                                }
                                continue;
                            } else {
                                bufInput.read();
                                bufInput.read();
                                //FileOutputStream fOS = new FileOutputStream("C:/Android/WH"+ streamLength+".txt");
                                try {
                                    decode(streamLength, bufInput, out);
                                    System.out.println(streamLength);
                                } catch (DataFormatException e) {
                                    System.out.println("Ошибка расшифровки GZIPa");
                                    return;
                                }
                            }

                        }
                        marker = markerEndObj.toCharArray();
                        while ((!isObjEnd) && (bufInput.available() > 0)) {
                            cc = (char) bufInput.read();
                            if (cc == marker[0]) {
                                isObjEnd = true;
                                for (int j = 1; j < marker.length; j++) {
                                    cc = (char) bufInput.read();
                                    if (cc != marker[j]) isObjEnd = false;
                                }
                            }
                        }


                    }

                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parse() {
        try {
            FileInputStream fio = new FileInputStream("/home/loinasd/prog/test/pdfParser/out1.txt");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void decode(int length, InputStream in, OutputStream out) throws DataFormatException, IOException {
        byte[] output = new byte[length];
        int compressedDataLength = in.read(output);
        Inflater decompressor = new Inflater();
        decompressor.setInput(output, 0, compressedDataLength);
        byte[] result = new byte[length * 10];
        int resultLength = decompressor.inflate(result);
        decompressor.end();
        String outputString = new String(result, 0, resultLength, "UTF-8");
        System.out.print(outputString);
        out.write(result, 0, resultLength);
    }

    public static boolean isZipped(byte[] compressed) {
        return compressed[0] == 31 && compressed[1] == -117;
    }
}
