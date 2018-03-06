package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import io.cyanlab.loinasd.wordllst.activities.NavActivity;

public class PDFParser {

    private static char cc;
    private static final String markerStream = "stream";
    private static final String markerLength = "Length";

    /*Парсит все в этой жизни, пишет в 1 поток.*/

    public void parsePdf(final String file, final PipedOutputStream out) {
        try {
            BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file));
            cc = (char) bufInput.read();
            while (bufInput.available() != 0) {
                cc = (char) bufInput.read();
                int streamLength = 0;
                boolean isFonts = false;
                while ((cc != '>') && (!isFonts) && (bufInput.available() > 0)) {
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
                    while ((!isStream)&&(bufInput.available()>0)) {
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
            NavActivity.h.sendEmptyMessage(1);
            out.close();
        } catch (FileNotFoundException e) {
            //MainActivity.h.sendEmptyMessage(1);
        } catch (IOException e) {
           // MainActivity.h.sendEmptyMessage(1);
        }
    }

    private static void decode(final int length, final InputStream in,
                               final PipedOutputStream out) throws DataFormatException, IOException {
        byte[] output = new byte[length];
        int compressedDataLength = in.read(output);
        Inflater decompressor = new Inflater();
        decompressor.setInput(output, 0, compressedDataLength);
        byte[] result = new byte[length * 10];
        int resultLength = decompressor.inflate(result);
        decompressor.end();


        out.write(result, 0, resultLength);
    }


    // Возвращает true когда нашел и false когда не нашел Маркер

    private static boolean search4Marker(InputStream inputStream, String marker) throws IOException{
        for (int i = 1; i < marker.length(); i++) {
            cc = (char) inputStream.read();
            if (cc != marker.charAt(i)) {
                return false;
            }
        }
        return true;
    }


}
