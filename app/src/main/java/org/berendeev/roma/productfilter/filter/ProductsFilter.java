package org.berendeev.roma.productfilter.filter;

import org.berendeev.roma.productfilter.domain.entity.Product;

import java.util.List;

public interface ProductsFilter {
    List<Product> filter (List<Product> source, String filter);
}
