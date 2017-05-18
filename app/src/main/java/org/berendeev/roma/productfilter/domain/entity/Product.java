package org.berendeev.roma.productfilter.domain.entity;

public class Product {
    public String name;
    public int categoryId;

    public Product(String name, int categoryId) {
        this.name = name;
        this.categoryId = categoryId;
    }
}
