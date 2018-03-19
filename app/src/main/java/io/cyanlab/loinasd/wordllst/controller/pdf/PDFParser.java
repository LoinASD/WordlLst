package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import io.cyanlab.loinasd.wordllst.activities.NavActivity;


public class PDFParser {

    private static char cc;
    private static final String markerStream = "stream";
    private static final String markerLength = "Length";
    private Logger log = Logger.getLogger(PDFParser.class.getName());

    private OutputStream outputStream;
    private Thread decoder;

    /**
     * Парсит все в этой жизни из файла @file, пишет в @out поток.
     *
     * @param file - файл для чтения
     * @param out - поток для записи
     */
    public void parsePdf(final String file, final PipedOutputStream out) {
        try {
            outputStream = out;
            BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(file),2048);
            long startTime = System.currentTimeMillis();
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
                            StringBuilder res = new StringBuilder();
                            bufInput.read();
                            cc = (char) bufInput.read();
                            while ((cc != '>') && (cc != '/')) {
                                res.append(cc);
                                cc = (char) bufInput.read();
                            }
                            try {
                                streamLength = Integer.parseInt(res.toString());
                            } catch (NumberFormatException e) {
                                isFonts = true;
                            }
                            if (cc == '/') {
                                bufInput.skip(streamLength);
                                continue;
                            }
                        }
                    }
                    if (cc == '>') cc = (char) bufInput.read();
                }

                if ((streamLength > 0)) {
                    boolean isStream = false;
                    while ((!isStream) && (bufInput.available() > 0)) {
                        if (cc == markerStream.charAt(0)) {
                            isStream = search4Marker(bufInput, markerStream);
                        }
                        if (!isStream) cc = (char) bufInput.read();
                    }
                    if (isStream) {
                        bufInput.read();
                        bufInput.read();
                        System.out.println(streamLength);
                        //FileOutputStream fOS = new FileOutputStream("C:/Android/WH"+ streamLength+".txt");
                        try {
                            if (decoder != null) {
                                decoder.join();
                            }
                            decode(streamLength, bufInput);
                        } catch (DataFormatException e) {
                            System.out.println("Ошибка расшифровки GZIPa");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            log.warning("Ошибка расшифровки GZIPa");
                            //System.out.println("Ошибка расшифровки GZIPa");
                        }
                    }
                }
            }
            decoder.join();
            NavActivity.h.sendEmptyMessage(1);
            out.close();
            log.warning("PDFParser works (ms): " + (System.currentTimeMillis() - startTime));
        } catch (FileNotFoundException e) {
            NavActivity.h.sendEmptyMessage(NavActivity.HANDLE_MESSAGE_NOT_EXTRACTED);
        } catch (IOException e) {
            NavActivity.h.sendEmptyMessage(NavActivity.HANDLE_MESSAGE_NOT_EXTRACTED);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод, расшифровывающий @length байт из потока @in и пишущий расшифрованные данные в @outputStream
     *
     * @param length - длина зашифрованной части файла в байтах
     * @param in - входной поток
     */
    private void decode(int length, InputStream in) throws DataFormatException, IOException {
        byte[] output = new byte[length];
        int compressedDataLength = in.read(output);
        decoder = new Thread(new UnGzipper(output, compressedDataLength));
        decoder.start();

    }


    /**
     * Возвращает true когда нашел и false когда не нашел @marker в @inputStream
     *
     * @param inputStream - входной поток
     * @param marker      - Маркер
     * @return true когда нашел и false когда не нашел @marker
     */

    private static boolean search4Marker(InputStream inputStream, String marker) throws IOException{
        for (int i = 1; i < marker.length(); i++) {
            cc = (char) inputStream.read();
            if (cc != marker.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Исполняемый класс, который декодит текст и пишет его в @outputStream*/

    private class UnGzipper implements Runnable {

        private byte[] output;
        private int compressedDataLength;

        UnGzipper(byte[] output, int compressedDataLength) {
            this.output = output;
            this.compressedDataLength = compressedDataLength;
        }

        @Override
        public void run() {
            Inflater decompressor = new Inflater();
            decompressor.setInput(output, 0, compressedDataLength);

            byte[] result = new byte[compressedDataLength * 10];
            int resultLength = 0;
            try {
                resultLength = decompressor.inflate(result);
            } catch (DataFormatException e) {
                e.printStackTrace();
            }

            decompressor.end();

            try {
                outputStream.write(result, 0, resultLength);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
