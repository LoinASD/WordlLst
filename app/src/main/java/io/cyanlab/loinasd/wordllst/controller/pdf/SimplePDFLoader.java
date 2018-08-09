package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import io.cyanlab.loinasd.wordllst.activities.ListsFragment;
import io.cyanlab.loinasd.wordllst.controller.PDFListLoader;

public class SimplePDFLoader implements PDFListLoader {

    private Listener listener;

    @Override
    public void loadPDFList(String file) {

        final PipedOutputStream pout;
        final PipedInputStream pin;
        try {
            pout = new PipedOutputStream();
            pin = new PipedInputStream(pout);
            Thread parser = new Thread(() -> new PDFParser().parsePdf(file, pout));

            Thread extractor = new Thread(() -> {

                Object result = new Delegator().extract(pin);

                if (result.getClass() != WordList.class){

                    listener.onError((String) result);
                    return;
                }

                listener.onListLoaded((WordList) result);

            });

            parser.setPriority(Thread.MAX_PRIORITY);

            parser.start();
            extractor.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setListener(Listener listener) {

        this.listener = listener;
    }
}
