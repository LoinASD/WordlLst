package io.cyanlab.loinasd.wordllst.controller.pdf;

import android.app.Activity;
import android.os.Environment;

import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PDFParser {

    private static char cc;
    private static final String markerStream = "stream";
    private static final String markerObj = "obj";
    private static final String markerEndObj = "endobj";
    private static final String markerLength = "Length";


    /*Парсит все в этой жизни, пишет в 1 поток.*/

    public static int parsePdf(String file, OutputStream out) {
        try {
            BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file));
            cc = (char) bufInput.read();
            while ((bufInput.available() != 0)) {
                int streamLength = 0;
                boolean isFonts = false;
                while ((cc != '>') && (!isFonts)) {
                    cc = (char) bufInput.read();
                    if (cc == markerLength.charAt(0)) {
                        boolean isLength = search4Marker(bufInput,markerLength);
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
                    continue;
                } else {
                    boolean isStream = false;
                    while ((!isStream) && (!isObjEnd)) {
                        if (cc == markerStream.charAt(0)) {
                            isStream = search4Marker(bufInput, markerStream);
                        }
                        if (!isStream) cc = (char) bufInput.read();
                    }
                    if (!isStream) {
                        continue;
                    } else{
                        bufInput.read();
                        bufInput.read();
                        //FileOutputStream fOS = new FileOutputStream("C:/Android/WH"+ streamLength+".txt");
                        try {
                            decode(streamLength, bufInput, out);
                        } catch (DataFormatException e) {
                            System.out.println("Ошибка расшифровки GZIPa");
                        }
                    }
                }
            }
            return 1;
        } catch (FileNotFoundException e) {
            return 0;
        } catch (IOException e) {
            return 0;
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




    // Возвращает 1 когда нашел и 0 когда не нашел Маркер

    private static boolean search4Marker(InputStream inputStream, String marker) throws IOException{
        boolean isMarker = true;
        for (int i = 1; i < marker.length(); i++) {
            cc = (char) inputStream.read();
            if (cc != marker.charAt(i)) {
                isMarker= false;
                break;
            }
        }
        return isMarker;
    }

}
