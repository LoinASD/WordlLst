package io.cyanlab.loinasd.wordllst.controller.pdf;


class LangChecker {

    static final int[] UNICODE_RANGE_RUS = {1040, 1103};
    static final int[] UNICODE_RANGE_ENG = {65, 122};
    static final int[] UNICODE_RANGE_NUMERIC = {48, 57};

    static Lang langCheck(char ch) {
        int cc = (int) ch;
        if ((cc >= UNICODE_RANGE_ENG[0]) && (cc <= UNICODE_RANGE_ENG[1])) {
            return Lang.ENG;
        } else if ((cc >= UNICODE_RANGE_RUS[0]) && (cc <= UNICODE_RANGE_RUS[1])) {
            return Lang.RUS;
        } else if ((cc >= UNICODE_RANGE_NUMERIC[0]) && (cc <= UNICODE_RANGE_NUMERIC[1])) {
            return Lang.NUM;
        } else if (cc == '<')
            return Lang.BRACE;
        return Lang.UNDEFINED;
    }
}
