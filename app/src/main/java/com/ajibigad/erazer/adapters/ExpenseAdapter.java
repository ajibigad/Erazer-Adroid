package com.ajibigad.erazer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.data.Expense;

import java.util.Collections;
import java.util.List;

/**
 * Created by ajibigad on 29/07/2017.
 */

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> mValues = Collections.emptyList();
    private final ExpenseAdapter.ExpenseAdapterOnClickHandler mListener;
    private Context mContext;

    public ExpenseAdapter(ExpenseAdapter.ExpenseAdapterOnClickHandler listener, Context context) {
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expenses_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mExpenseTitle.setText(mValues.get(position).getTitle());
        holder.mExpenseDate.setText(DateUtils
                .getRelativeDateTimeString(mContext, mValues.get(position).getDateAdded().getTime(),
                        DateUtils.SECOND_IN_MILLIS, DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_DATE));
        holder.mExpenseState.setText(mValues.get(position).getTitle());
        holder.mExpenseCost.setText(mValues.get(position).getTitle());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onClick(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setData(List<Expense> data) {
        this.mValues = data;
    }

    public void setExpenses(List<Expense> expenses) {
        this.mValues = expenses;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mExpenseTitle;
        final TextView mExpenseDate;
        final TextView mExpenseState;
        final TextView mExpenseCost;
        Expense mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mExpenseTitle = (TextView) view.findViewById(R.id.expense_title);
            mExpenseDate = (TextView) view.findViewById(R.id.expense_date);
            mExpenseState = (TextView) view.findViewById(R.id.expense_state);
            mExpenseCost = (TextView) view.findViewById(R.id.expense_cost);
        }
    }

    public interface ExpenseAdapterOnClickHandler {

        public void onClick(Expense expense);
    }
}
