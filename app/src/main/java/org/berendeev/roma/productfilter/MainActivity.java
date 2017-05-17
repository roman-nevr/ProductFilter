package org.berendeev.roma.productfilter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.berendeev.roma.productfilter.filter.TextFilter;
import org.berendeev.roma.productfilter.filter.TextFilterImpl;
import org.berendeev.roma.productfilter.presentation.FlowLayoutManager;
import org.berendeev.roma.productfilter.presentation.StringAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.filter) EditText filter;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    private StringAdapter adapter;
    private List<String> strings;
    private TextFilter textFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        FlowLayoutManager layoutManager = new FlowLayoutManager();
        strings = createList();
        textFilter = new TextFilterImpl();

        recyclerView.setLayoutManager(layoutManager);
        initFilter();
        setList(strings);
    }

    private void initFilter(){
        filter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                setList(textFilter.filter(strings, s.toString()));
            }

            @Override public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setList(List<String> strings){
        if(adapter == null){
            adapter = new StringAdapter(strings);
            recyclerView.setAdapter(adapter);
        }else {
            adapter.update(strings);
        }
    }

    private List<String> createList(){
        Gson gson = new GsonBuilder().create();

        Type type = new TypeToken<List<String>>() {}.getType();

        return gson.fromJson(Json.json, type);

    }
}
