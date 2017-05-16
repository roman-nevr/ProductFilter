package org.berendeev.roma.productfilter.filter;

import java.util.List;

public interface TextFilter {
    List<String> filter (List<String> source, String filter);
}
