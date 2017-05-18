package org.berendeev.roma.productfilter.filter;

import org.berendeev.roma.productfilter.domain.entity.Product;

import java.util.ArrayList;
import java.util.List;

public class TextFilterImpl implements TextFilter {

    @Override public List<Product> filter(List<Product> source, String filter) {
        List<Product> productList = new ArrayList<>();
        if (filter.isEmpty()){
            return source;
        }
        for (Product product : source) {
            if (product.name.toLowerCase().indexOf(filter.toLowerCase()) == 0) {
                productList.add(product);
            }
        }
        return productList;
    }

}
