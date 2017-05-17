package org.berendeev.roma.productfilter.presentation;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.berendeev.roma.productfilter.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class StringAdapter extends RecyclerView.Adapter<StringAdapter.StringHolder> {

    private List<String> strings;

    public StringAdapter(List<String> strings) {
        this.strings = strings;
    }

    @Override public StringHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new StringHolder(view);
    }

    @Override public void onBindViewHolder(StringHolder holder, int position) {
        holder.item.setText(strings.get(position));
    }

    @Override public int getItemCount() {
        return strings.size();
    }

    public void update(List<String> strings) {
        this.strings = strings;
        notifyDataSetChanged();
    }

    class StringHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.item) TextView item;

        public StringHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
