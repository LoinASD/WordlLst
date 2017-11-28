package io.cyanlab.loinasd.wordllst.controller.pdf;


class LangChecker {
    static final int LANG_ENG = 0;
    static final int LANG_RUS = 1;
    static final int[] UNICODE_RANGE_RUS = {1040, 1103};
    static final int[] UNICODE_RANGE_ENG = {65, 122};

    static int langCheck(char ch) {
        int cc = (int) ch;
        if ((cc >= UNICODE_RANGE_ENG[0]) && (cc <= UNICODE_RANGE_ENG[1])) {
            return LANG_ENG;
        } else if ((cc >= UNICODE_RANGE_RUS[0]) && (cc <= UNICODE_RANGE_RUS[1])) {
            return LANG_RUS;
        }
        return -1;
    }
}
