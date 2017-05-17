package org.berendeev.roma.productfilter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.berendeev.roma.productfilter.presentation.FlowLayoutManager;
import org.berendeev.roma.productfilter.presentation.StringAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        FlowLayoutManager layoutManager = new FlowLayoutManager();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        List<String> strings = Arrays.asList("iceCream", "bread");
        List<String> strings = createList();
        strings.add("dfjbvdkjfbnvkdbfvkdbvkjbandkvjbdkfjvbndkjfbvnkdjfbvkdbnvkdbnvkjhbdj");
        StringAdapter adapter = new StringAdapter(strings);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    private List<String> createList(){
        List<String> strings = new ArrayList<>();
        for (int index = 0; index < 20; index++) {
            strings.add("product " + index);
        }
        return strings;
    }
}
