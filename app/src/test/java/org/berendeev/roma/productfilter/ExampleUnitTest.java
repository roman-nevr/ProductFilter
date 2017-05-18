package org.berendeev.roma.productfilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.berendeev.roma.productfilter.domain.entity.Product;
import org.berendeev.roma.productfilter.filter.ProductsFilter;
import org.berendeev.roma.productfilter.filter.ProductsFilterImpl;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void json(){
        Gson gson = new GsonBuilder().create();

        List<String> strings = new ArrayList<>();
        strings.add("Мороженное");
        strings.add("Пироженное");
        strings.add("Газировка");
        String s = gson.toJson(strings);
        System.out.println(s);
    }

    @Test
    public void filter(){

        System.out.println(Json.json);

        Gson gson = new GsonBuilder().create();

        Type type = new TypeToken<List<String>>() {}.getType();

        List<String> strings = gson.fromJson(Json.json, type);

        ProductsFilter filter = new ProductsFilterImpl();

        System.out.println(filter.filter(strings, "мор"));
    }

    @Test
    public void tut(){
        Gson gson = new GsonBuilder().create();

        Type type = new TypeToken<List<String>>() {}.getType();

        List<String> strings = gson.fromJson(Json.json, type);

        List<Product> products = new ArrayList<>();

        for (String string : strings) {
            products.add(new Product(string, 1));
        }

        String prJson = gson.toJson(products);

        System.out.println(prJson);
    }
}