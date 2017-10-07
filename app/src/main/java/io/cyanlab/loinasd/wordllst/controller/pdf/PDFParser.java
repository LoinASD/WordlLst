package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

class PDFParser {
    private static int ch;
    private static char cc;
    private static final String markerStream = "stream";
    private static final String markerEndStream = "endstream";
    private static final String markerObj = "obj";
    private static final String markerEndObj = "endobj";
    private static final String markerDecode = "/Filter/FlateDecode/Length";
    private static final String markerLength = "Length";


    //Парсит большинство объектов, но часто, как по мне, путает obj и endobj, поэтому ошибки




    private static void parsePdf(String file) {
        try {
            // Поправить
            File pdf = new File("C:/Android/Describing people_Сharacter_Intermediate.pdf");
            long pfdLength = pdf.length();
            FileInputStream fio = new FileInputStream(pdf);
            //byte[] byteBuffer = new byte[(int) pfdLength];
            //int fileLength = fio.read(byteBuffer);
            ByteBuffer bb = ByteBuffer.allocate(9);
            cc = (char)fio.read();

            //Мб лучше пока сс!=0
            //for (long i = 0; i < pdf.length(); i++)
            while((fio.available()!=0)){
                cc = (char) fio.read();
                char[] marker = markerObj.toCharArray();

                if (cc == marker[0]) {
                    boolean isObj = true;
                    for (int j = 1; j < marker.length; j++) {
                        //bb.put((byte) cc);
                        cc = (char) fio.read();
                        if (cc != marker[j]) isObj = false;
                    }
                    if (!isObj) {
                        continue;
                    } else {
                        boolean isObjEnd = false;
                        boolean isFonts = false;
                        int streamLength = -1;
                        while ((cc != '>')&&(!isFonts)) {
                            char[] marker2 = markerLength.toCharArray();
                            cc = (char) fio.read();
                            if (cc == marker2[0]) {
                                boolean isLength = true;
                                for (int j = 1; j < marker2.length; j++) {
                                    cc = (char) fio.read();
                                    if (cc != marker2[j]) isLength = false;
                                }
                                if (isLength) {
                                    StringBuffer res = new StringBuffer(6);
                                    fio.read();
                                    cc = (char) fio.read();
                                    while ((cc != '>')&&(cc!= '/')) {
                                        res.append(cc);
                                        cc = (char) fio.read();
                                    }
                                    if (cc == '/') {
                                        isFonts = true;
                                    } else{
                                        try {
                                            streamLength = Integer.parseInt(res.toString());
                                        } catch (NumberFormatException e) {
                                            isFonts = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (isFonts) {
                            marker = markerEndObj.toCharArray();
                            isObjEnd = false;
                            int k = fio.available();
                            while (!isObjEnd){
                                cc = (char)fio.read();
                                if (cc == marker[0]){
                                    isObjEnd = true;
                                    for (int j = 1; j < marker.length; j++) {
                                        cc = (char) fio.read();
                                        if (cc != marker[j]) isObjEnd = false;
                                    }
                                }
                            }
                            continue;

                        } else{
                            marker = markerStream.toCharArray();
                            while(cc!=marker[0]){
                                cc = (char)fio.read();
                                if (cc == markerEndStream.toCharArray()[0]){
                                    isObjEnd = true;
                                    for (int j = 1; j < marker.length; j++) {
                                        cc = (char) fio.read();
                                        if (cc != marker[j]) isObjEnd = false;
                                    }
                                    if (isObjEnd) break;
                                }
                            }
                            boolean isStream = true;
                            for (int j = 1; j < marker.length; j++) {
                                cc = (char) fio.read();
                                if (cc != marker[j]) isStream = false;
                            }
                            if ((!isStream)||(streamLength <=0 )||(fio.available()==0)){
                                marker = markerEndObj.toCharArray();
                                isObjEnd = false;
                                while (!isObjEnd){
                                    cc = (char)fio.read();
                                    if (cc == marker[0]){
                                        isObjEnd = true;
                                        for (int j = 1; j < marker.length; j++) {
                                            cc = (char) fio.read();
                                            if (cc != marker[j]) isObjEnd = false;
                                        }
                                    }
                                }
                                continue;
                            } else {
                                fio.read();
                                fio.read();
                                FileOutputStream fOS = new FileOutputStream("C:/Android/"+ streamLength+".txt");
                                decode(streamLength,fio,fOS);
                            }

                        }

                        marker = markerEndObj.toCharArray();
                        isObjEnd = false;
                        while ((!isObjEnd)&&(fio.available()!=0)){
                            cc = (char)fio.read();
                            if (cc == marker[0]){
                                isObjEnd = true;
                                for (int j = 1; j < marker.length; j++) {
                                    cc = (char) fio.read();
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

    private static void readMarker(String marker) {

    }

    private static void parse() {
        try {
            FileInputStream fio = new FileInputStream("/home/loinasd/prog/test/pdfParser/out1.txt");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void decode(int length, InputStream in, OutputStream out) {
        try {
            //FileInputStream fileInputStream = new FileInputStream("/home/loinasd/prog/test/pdfParser/new.txt");
            byte[] output = new byte[length];
            int compressedDataLength = in.read(output);
            Inflater decompressor = new Inflater();
            decompressor.setInput(output, 0, compressedDataLength);
            byte[] result = new byte[length * 10];
            int resultLength = decompressor.inflate(result);
            decompressor.end();
            String outputString = new String(result, 0, resultLength, "UTF-8");
            System.out.print(outputString);
            //FileOutputStream fileOutputStream = new FileOutputStream("/home/loinasd/prog/test/pdfParser/out1.txt");
            out.write(result, 0, resultLength);
            out.close();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (DataFormatException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean isZipped(byte[] compressed) {
        return compressed[0] == 31 && compressed[1] == -117;
    }
}
