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

    public static int parsePdf(String file, OutputStream out, Activity activity) {
        try {
            File kek = new File("sdcard/Download/Describing people_Сharacter_Intermediate.pdf");
            FileInputStream pdf = new FileInputStream("sdcard/Download/Describing people_Сharacter_Intermediate.pdf");
            BufferedInputStream bufInput = new BufferedInputStream(pdf);
            cc = (char) bufInput.read();
            while ((bufInput.available() != 0)) {
                cc = (char) bufInput.read();
                if (cc == markerObj.charAt(0)) {
                    boolean isObj = search4Marker(bufInput,markerObj);
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
                                    isObjEnd = search4EndObj(bufInput);
                                }
                            }
                            cc = (char) bufInput.read();
                        }
                        if (isObjEnd) {
                            continue;
                        }
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
                            while (!search4EndObj(bufInput)) {}
                            continue;

                        } else {
                            boolean isStream = false;
                            while ((!isStream) && (!isObjEnd)) {
                                if (cc == markerStream.charAt(0)) {
                                    isStream = search4Marker(bufInput, markerStream);
                                }
                                if (isStream) break;
                                if (cc == '\n') {
                                    cc = (char) bufInput.read();
                                    if (cc == 's') continue;
                                    else {
                                        isObjEnd = search4EndObj(bufInput);
                                    }
                                }
                                cc = (char) bufInput.read();
                            }
                            if (isObjEnd) {
                                continue;
                            }
                            if (!isStream) {
                                while (!isObjEnd) {
                                    isObjEnd = search4EndObj(bufInput);
                                }
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
                        while ((!isObjEnd) && (bufInput.available() > 0)) {
                            isObjEnd = search4EndObj(bufInput);
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


    // Возвращает 1 когда нашел и 0 когда не нашел endobj

    private static boolean search4EndObj(InputStream inputStream) throws IOException{
        boolean isObjEnd = true;
        for (int i = 0; i < markerEndObj.length(); i++) {
            cc = (char) inputStream.read();
            if (cc != markerEndObj.charAt(i)) {
                isObjEnd = false;
                break;
            }
        }
        return isObjEnd;
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
