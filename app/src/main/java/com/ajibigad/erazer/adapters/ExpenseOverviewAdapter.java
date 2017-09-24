package com.ajibigad.erazer.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.data.ExpensesOverview;

import java.util.List;

/**
 * Created by ajibigad on 29/07/2017.
 */

public class ExpenseOverviewAdapter extends ArrayAdapter<ExpensesOverview> {

    private Context mContext;
    private int layoutResourceId;
    private List<ExpensesOverview> data;

    public ExpenseOverviewAdapter(@NonNull Context context, @LayoutRes int resource, List<ExpensesOverview> data) {
        super(context, resource, data);
        this.mContext = context;
        layoutResourceId = resource;
        this.data = data;
    }

    static class ViewHolder {
        TextView tvCount;
        TextView tvState;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        ExpensesOverview overview = getItem(position);

        if (convertView == null) {
            // If it's not recycled, initialize some attributes
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.tvCount = (TextView) convertView.findViewById(R.id.expense_count);
            holder.tvState = (TextView) convertView.findViewById(R.id.expense_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            ;
        }

        holder.tvCount.setText(String.valueOf(overview.getCount()));
        holder.tvState.setText(overview.getState());
        return convertView;
    }

    public void setData(List<ExpensesOverview> data) {
        this.data = data;
        notifyDataSetChanged();
    }
}
