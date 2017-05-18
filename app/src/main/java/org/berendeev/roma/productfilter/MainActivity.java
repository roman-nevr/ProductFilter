package org.berendeev.roma.productfilter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.berendeev.roma.productfilter.domain.entity.Product;
import org.berendeev.roma.productfilter.filter.ProductsFilter;
import org.berendeev.roma.productfilter.filter.ProductsFilterImpl;
import org.berendeev.roma.productfilter.presentation.FlowLayoutManager;
import org.berendeev.roma.productfilter.presentation.StringAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.filter) EditText filter;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.clear_button) ImageButton clearButton;
    @BindView(R.id.hint) TextView hint;
    private StringAdapter adapter;
    private List<Product> productList;
    private ProductsFilter productsFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        FlowLayoutManager layoutManager = new FlowLayoutManager();
        productList = createList();
        productsFilter = new ProductsFilterImpl();

        recyclerView.setLayoutManager(layoutManager);

        initFilter();
//        setList(productList);
        initClearButton();
    }

    private void initClearButton() {
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                filter.setText("");
                filter.requestFocus();
            }
        });
    }

    private void initFilter(){
        filter.clearFocus();
        filter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0){
                    showHint();
                    setList(new ArrayList<Product>());
                }else {
                    hideHint();
                    setList(productsFilter.filter(productList, s.toString()));
                }
            }

            @Override public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setList(List<Product> strings){
        if(adapter == null){
            adapter = new StringAdapter(strings, getApplicationContext());
            recyclerView.setAdapter(adapter);
        }else {
            adapter.update(strings);
        }
    }

    private List<Product> createList(){
        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<List<Product>>() {}.getType();
        return gson.fromJson(Json.productsJson, type);
    }

    private void showHint(){
        hint.setVisibility(VISIBLE);
    }

    private void hideHint(){
        hint.setVisibility(GONE);
    }
}
