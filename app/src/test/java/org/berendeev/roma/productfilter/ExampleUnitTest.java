package org.berendeev.roma.productfilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.berendeev.roma.productfilter.filter.TextFilter;
import org.berendeev.roma.productfilter.filter.TextFilterImpl;
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

        TextFilter filter = new TextFilterImpl();

        System.out.println(filter.filter(strings, "мор"));
    }
}