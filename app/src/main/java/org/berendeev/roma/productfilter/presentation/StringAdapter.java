package org.berendeev.roma.productfilter.presentation;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.berendeev.roma.productfilter.R;
import org.berendeev.roma.productfilter.domain.entity.Product;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class StringAdapter extends RecyclerView.Adapter<StringAdapter.StringHolder> {

    private List<Product> products;

    private int color1, color2, color3, color4;

    public StringAdapter(List<Product> products, Context context) {
        this.products = products;
        hasStableIds();
        color1 = ContextCompat.getColor(context, R.color.color1);
        color2 = ContextCompat.getColor(context, R.color.color2);
        color3 = ContextCompat.getColor(context, R.color.color3);
        color4 = ContextCompat.getColor(context, R.color.color4);
    }

    @Override public long getItemId(int position) {
        return products.hashCode();
    }

    @Override public StringHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new StringHolder(view);
    }

    @Override public void onBindViewHolder(StringHolder holder, int position) {
        holder.item.setText(products.get(position).name);
        holder.cardView.setCardBackgroundColor(getColor(products.get(position).categoryId));
    }

    private int getColor(int categoryId) {
        switch (categoryId){
            case 1:
                return color1;
            case 2:
                return color2;
            case 3:
                return color3;
            case 4:
                return color4;
        }
        return 0;
    }

    @Override public int getItemCount() {
        return products.size();
    }

    public void update(List<Product> strings) {
        this.products = strings;
        notifyDataSetChanged();
    }

    class StringHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.item) TextView item;
        @BindView(R.id.card) CardView cardView;

        public StringHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
