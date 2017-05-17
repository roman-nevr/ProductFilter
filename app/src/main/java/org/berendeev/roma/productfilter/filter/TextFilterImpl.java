package org.berendeev.roma.productfilter.filter;

import java.util.ArrayList;
import java.util.List;

public class TextFilterImpl implements TextFilter {

    @Override public List<String> filter(List<String> source, String filter) {
        List<String> strings = new ArrayList<>();
        if (filter.isEmpty()){
            return source;
        }
        for (String string : source) {
            if (string.toLowerCase().indexOf(filter.toLowerCase()) == 0) {
                strings.add(string);
            }
        }
        return strings;
    }

}
