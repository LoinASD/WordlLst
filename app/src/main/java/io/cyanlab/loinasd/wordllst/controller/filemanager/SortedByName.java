package io.cyanlab.loinasd.wordllst.controller.filemanager;

import java.util.Comparator;
class SortedByName implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        String str1 = (String) o1;
        String str2 = (String) o2;
        str1 = str1.toUpperCase();
        str2 = str2.toUpperCase();
        return str1.compareTo(str2);
    }
}

