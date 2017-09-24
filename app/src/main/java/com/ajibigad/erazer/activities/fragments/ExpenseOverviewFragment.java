package com.ajibigad.erazer.activities.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.Toast;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.activities.ExpensesActivity;
import com.ajibigad.erazer.adapters.ExpenseOverviewAdapter;
import com.ajibigad.erazer.data.Expense;
import com.ajibigad.erazer.data.ExpensesOverview;
import com.ajibigad.erazer.network.ExpenseService;
import com.ajibigad.erazer.utils.NetworkConnectivityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExpenseOverviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ExpenseOverviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<ExpensesOverview>>,
        DatePickerDialog.OnDateSetListener {

    private static final int EXPENSES_OVERVIEW_LOADER = 87633;
    public static final String EXTRA_STATE = "extra_state";
    public static final String EXTRA_MONTH = "extra_month";
    public static final String EXTRA_YEAR = "extra_year";
    public static final String EXTRA_USERNAME = "extra_username";
    private static final String TAG = ExpenseOverviewFragment.class.getSimpleName();

    private String selectedUsername;
    private int selectedMonth, selectedYear;

    @BindView(R.id.progress_bar_layout)
    View progressBarLayout;

    @BindView(R.id.expense_overview_grid)
    GridView gridView;

    @BindView(R.id.monthly_toggle)
    SwitchCompat switchCompat;

    private ExpenseOverviewAdapter adapter;

    private OnFragmentInteractionListener mListener;

    public ExpenseOverviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_expense_overview, container, false);
        ButterKnife.bind(this, view);

        if (getArguments() != null && getArguments().containsKey(EXTRA_USERNAME)) {
            selectedUsername = getArguments().getString(EXTRA_USERNAME);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ExpenseOverviewAdapter(getContext(), R.layout.expenses_overview_grid_item, generateDefaultExpensesOverview());
        gridView.setAdapter(adapter);

        // Set a click listener on that View
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ExpensesActivity.class);
                //username, state, month, year
                if (selectedUsername != null) {
                    intent.putExtra(EXTRA_USERNAME, selectedUsername);
                }

                intent.putExtra(EXTRA_MONTH, selectedMonth);
                intent.putExtra(EXTRA_YEAR, selectedYear);
                intent.putExtra(EXTRA_STATE, adapter.getItem(position).getState());
                startActivity(intent);
            }
        });
        loadExpensesOveriew();
    }

    private List<ExpensesOverview> generateDefaultExpensesOverview() {
        List<ExpensesOverview> expensesOverviewList = new ArrayList<>();
        expensesOverviewList.add(0, new ExpensesOverview(0, Expense.STATE.PENDING.name()));
        expensesOverviewList.add(1, new ExpensesOverview(0, Expense.STATE.APPROVED.name()));
        expensesOverviewList.add(2, new ExpensesOverview(0, Expense.STATE.DECLINED.name()));
        expensesOverviewList.add(3, new ExpensesOverview(0, Expense.STATE.SETTLED.name()));
        return expensesOverviewList;
    }

    private void loadExpensesOveriew() {
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<List<ExpensesOverview>> expensesOverviewLoader = loaderManager.getLoader(EXPENSES_OVERVIEW_LOADER);
        if (expensesOverviewLoader == null) {
            loaderManager.initLoader(EXPENSES_OVERVIEW_LOADER, null, this);
        } else {
            loaderManager.restartLoader(EXPENSES_OVERVIEW_LOADER, null, this);
        }
    }

    private void showProgressBar() {
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    private void showOverview() {
        progressBarLayout.setVisibility(View.INVISIBLE);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<List<ExpensesOverview>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<ExpensesOverview>>(getContext()) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                showProgressBar();
                if (!NetworkConnectivityUtils.isConnected(getContext())) {
                    deliverResult(Collections.<ExpensesOverview>emptyList());
                } else forceLoad();
            }

            @Override
            public List<ExpensesOverview> loadInBackground() {
                try {
                    Response<List<ExpensesOverview>> response;
                    if (selectedUsername != null) {
                        response = ExpenseService.getExpenseClient()
                                .getUserExpensesOverview(selectedUsername, selectedMonth, selectedYear).execute();
                    } else {
                        response = ExpenseService.getExpenseClient()
                                .getAllExpensesOverview(selectedMonth, selectedYear).execute();
                    }
                    if (response.isSuccessful()) {
                        return response.body();
                    } else {
                        return Collections.emptyList();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<ExpensesOverview>> loader, List<ExpensesOverview> data) {
        if (data.isEmpty()) {
            adapter.setData(generateDefaultExpensesOverview());
            Toast.makeText(getContext(), "No overview found", Toast.LENGTH_SHORT).show();
        } else adapter.clear();
        adapter.addAll(data);
        showOverview();
    }

    @Override
    public void onLoaderReset(Loader<List<ExpensesOverview>> loader) {
        adapter.setData(null);
    }

    @OnClick(R.id.monthly_toggle)
    public void toggleMonthlyView() {
        if (switchCompat.isChecked()) {
            //change monthlyview label to Monthly
            //show month picker btn
            //show datepickerdialog
            //change toolbar title to Overview for {month, year} eg Jan, 2017
            //change toolbar title to {username}'s Overview for {month, year} eg Jan, 2017 if username
        } else {
            //change toolbar title to Overview
            //change monthlyview label to Monthly View
            //hide month picker btn
        }
    }

    @OnClick(R.id.btn_month_picker)
    public void showDatePickerDialog() {
        createDialogWithoutDateField().show();
    }

    private DatePickerDialog createDialogWithoutDateField() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        DatePickerDialog dpd = new DatePickerDialog(getContext(), this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        try {
            java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {
                        Log.i(TAG, datePickerField.getName());
                        if ("mDaySpinner".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }
            }
            dpd.setTitle("Select Month");
        } catch (Exception ex) {
        }
        return dpd;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedMonth = month;
        selectedYear = year;
        loadExpensesOveriew();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
