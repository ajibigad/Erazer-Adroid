package com.ajibigad.erazer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.adapters.ExpenseAdapter;
import com.ajibigad.erazer.data.Expense;
import com.ajibigad.erazer.network.ExpenseService;
import com.ajibigad.erazer.network.UserService;
import com.ajibigad.erazer.network.request.ExpenseSearchRequestBuilder;
import com.ajibigad.erazer.utils.NetworkConnectivityUtils;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;

import static com.ajibigad.erazer.activities.fragments.ExpenseOverviewFragment.EXTRA_STATE;
import static com.ajibigad.erazer.activities.fragments.ExpenseOverviewFragment.EXTRA_USERNAME;

public class ExpensesActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Expense>>, ExpenseAdapter.ExpenseAdapterOnClickHandler {

    private static final int QUERY_EXPENSES_LOADER = 536362;
    private ExpenseSearchRequestBuilder searchRequestBuilder;
    private String selectedUsername;
    private ExpenseAdapter expenseAdapter;

    @BindView(R.id.expenses_recycler_view)
    RecyclerView expenseRecyclerView;

    @BindView(R.id.progress_bar_layout)
    View progressBarLayout;

    @BindView(R.id.tv_error_message_display)
    TextView tvErrorMessage;

    private final String TAG = ExpensesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        searchRequestBuilder = new ExpenseSearchRequestBuilder();

        if (getIntent().hasExtra(EXTRA_USERNAME)) {
            selectedUsername = getIntent().getExtras().getString(EXTRA_USERNAME);
            toolbar.setTitle(String.format("%s's ", selectedUsername));
        }

        if (getIntent().hasExtra(EXTRA_STATE)) {
            searchRequestBuilder.state(getIntent().getExtras().getString(EXTRA_STATE));
            toolbar.setTitle(String.format("%s %s Expenses", selectedUsername != null ? toolbar.getTitle() : "All ",
                    getIntent().getExtras().getString(EXTRA_STATE)));
        }

        expenseAdapter = new ExpenseAdapter(this, this);
        expenseRecyclerView.setAdapter(expenseAdapter);
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadResults();
    }

    private void loadResults() {
        LoaderManager loaderManager = getSupportLoaderManager();
        if (loaderManager.getLoader(QUERY_EXPENSES_LOADER) == null) {
            loaderManager.initLoader(QUERY_EXPENSES_LOADER, null, this);
        } else {
            loaderManager.restartLoader(QUERY_EXPENSES_LOADER, null, this);
        }
    }

    private void showErrorMessage() {
        tvErrorMessage.setVisibility(View.VISIBLE);
        expenseRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showResultsView() {
        expenseRecyclerView.setVisibility(View.VISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        expenseRecyclerView.setVisibility(View.INVISIBLE);
        tvErrorMessage.setVisibility(View.INVISIBLE);
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<List<Expense>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Expense>>(this) {

            @Override
            protected void onStartLoading() {
                if (NetworkConnectivityUtils.isConnected(getContext())) {
                    showProgressBar();
                    forceLoad();
                } else {
                    Toast.makeText(ExpensesActivity.this, R.string.check_network_connection, Toast.LENGTH_SHORT).show();
                    deliverResult(Collections.<Expense>emptyList());
                }
            }

            @Override
            public List<Expense> loadInBackground() {
                try {
                    Response<List<Expense>> response = null;
                    if (selectedUsername != null) {
                        response = UserService.getUserClient()
                                .getUserExpenses(selectedUsername, searchRequestBuilder.build(), "").execute();
                    } else
                        response = ExpenseService.getExpenseClient()
                                .getAllExpenses(searchRequestBuilder.build(), "").execute();

                    if (response.isSuccessful()) {
                        return response.body();
                    } else {
                        Log.i(TAG, String.format("Message : %s,Response code: %s", response.message(), response.code()));
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
    public void onLoadFinished(Loader<List<Expense>> loader, List<Expense> expenses) {
        progressBarLayout.setVisibility(View.INVISIBLE);
        if (expenses.isEmpty()) {
            showErrorMessage();
        } else {
            expenseAdapter.setExpenses(expenses);
            showResultsView();
            getSupportActionBar().setTitle(String.format("%d expenses found", expenses.size()));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Expense>> loader) {

    }

    @Override
    public void onClick(Expense expense) {
        //open expense details activity
        Intent expenseDetailsIntent = new Intent(this, ExpenseDetailsActivity.class);
        expenseDetailsIntent.putExtra(ExpenseDetailsActivity.EXPENSE_PARCEL, Parcels.wrap(expense));
        startActivity(expenseDetailsIntent);
    }
}
