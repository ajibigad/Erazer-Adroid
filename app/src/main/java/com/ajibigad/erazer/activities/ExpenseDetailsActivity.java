package com.ajibigad.erazer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.data.Expense;
import com.ajibigad.erazer.data.User;
import com.ajibigad.erazer.network.ExpenseService;
import com.ajibigad.erazer.network.UserService;
import com.ajibigad.erazer.service.ErazerIntentService;
import com.ajibigad.erazer.utils.NetworkConnectivityUtils;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnegative;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Response;

public class ExpenseDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Expense> {

    public static final String EXPENSE_PARCEL = "expense_parcel";

    public static final String EXTRA_EXPENSE_ID = "extra_expense_id";
    private static final int EXPENSE_LOADER = 44433228;
    private static final String TAG = ExpenseDetailsActivity.class.getSimpleName();

    private Expense selectedExpense;

    private User selectedUser;

    @BindViews({R.id.expense_title, R.id.expense_description, R.id.expense_cost, R.id.expense_state, R.id.expense_date})
    List<TextView> detailsViews;

    @BindView(R.id.btn_approve)
    Button approveBtn;

    @BindView(R.id.btn_decline)
    Button declineBtn;

    @BindView(R.id.btn_settle)
    Button settleBtn;

    @BindView(R.id.expense_proof_type)
    TextView tvProofType;

    @BindView(R.id.expense_proof_image)
    ImageView ivProofImage;

    @BindView(R.id.expense_proof_description)
    TextView tvProofDescription;

    @BindView(R.id.proof_layout)
    View proofLayout;

    @BindView(R.id.progress_bar_layout)
    View progressBarLayout;

    @BindView(R.id.details_layout)
    View detailsLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        if (getIntent().hasExtra(EXPENSE_PARCEL)) {
            selectedExpense = Parcels.unwrap(getIntent().getExtras().getParcelable(EXPENSE_PARCEL));
            displayDetails();
        } else if (getIntent().hasExtra(EXTRA_EXPENSE_ID)) {
            //start up loader to fetch the expense
            loadExpense(getIntent().getLongExtra(EXTRA_EXPENSE_ID, -1));
        } else if (getIntent().getExtras().get("id") != null) { // this is coming from FCM data payload when app is in background
            loadExpense(Long.parseLong((String) getIntent().getExtras().get("id")));
        } else finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!intent.hasExtra(EXTRA_EXPENSE_ID)) {
            finish();
        }
        loadExpense(getIntent().getLongExtra(EXTRA_EXPENSE_ID, -1));

    }

    private void loadExpense(@Nonnegative long expenseID) {
        LoaderManager loaderManager = getSupportLoaderManager();
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_EXPENSE_ID, expenseID);
        if (loaderManager.getLoader(EXPENSE_LOADER) == null) {
            loaderManager.initLoader(EXPENSE_LOADER, bundle, this);
        } else {
            loaderManager.restartLoader(EXPENSE_LOADER, bundle, this);
        }
    }

    private void displayDetails() {
        detailsViews.get(0).setText(selectedExpense.getTitle());
        detailsViews.get(1).setText(selectedExpense.getDescription());
        detailsViews.get(2).setText(String.valueOf(selectedExpense.getCost()));
        detailsViews.get(3).setText(String.valueOf(selectedExpense.getState()));
        detailsViews.get(4).setText(DateUtils.getRelativeTimeSpanString(selectedExpense.getDateAdded().getTime()));
        tvProofType.setText(String.valueOf(selectedExpense.getProofType()));
        switch (selectedExpense.getProofType()) {
            case TEXT:
                tvProofDescription.setText(selectedExpense.getProof());
                break;
            case IMAGE:
                proofLayout.setVisibility(View.VISIBLE);
                tvProofDescription.setVisibility(View.INVISIBLE);
                Log.i("Image URI", ExpenseService.getImageUrlFromExpense(selectedExpense));
                Picasso picasso = new Picasso.Builder(this)
                        .downloader(new OkHttp3Downloader(ExpenseService.getHttpClient()))
                        .build();
                picasso.load(ExpenseService.getImageUrlFromExpense(selectedExpense))
                        .placeholder(R.drawable.loading)
                        .into(ivProofImage);
                break;
            case EMAIL:
                proofLayout.setVisibility(View.INVISIBLE);
                break;
        }

        if (UserService.getCurrentUser().isAdmin()) {
            switch (selectedExpense.getState()) {
                case PENDING:
                    approveBtn.setVisibility(View.VISIBLE);
                    declineBtn.setVisibility(View.VISIBLE);
                    settleBtn.setVisibility(View.INVISIBLE);
                    break;
                case APPROVED:
                    settleBtn.setVisibility(View.VISIBLE);
                    approveBtn.setVisibility(View.INVISIBLE);
                    declineBtn.setVisibility(View.INVISIBLE);
                    break;
                case DECLINED:
                    approveBtn.setVisibility(View.INVISIBLE);
                    declineBtn.setVisibility(View.INVISIBLE);
                    settleBtn.setVisibility(View.INVISIBLE);
            }

        }
    }

    @OnClick({R.id.btn_settle, R.id.btn_approve, R.id.btn_decline})
    public void changeState(Button clickedButton) {
        Toast.makeText(this, "Updating expense", Toast.LENGTH_SHORT).show();
        switch (clickedButton.getId()) {
            case R.id.btn_approve:
                ErazerIntentService.startActionChangeState(this, selectedExpense.getId(), Expense.STATE.APPROVED);
                break;
            case R.id.btn_decline:
                ErazerIntentService.startActionChangeState(this, selectedExpense.getId(), Expense.STATE.DECLINED);
                break;
            case R.id.btn_settle:
                ErazerIntentService.startActionChangeState(this, selectedExpense.getId(), Expense.STATE.SETTLED);
                break;
        }
        finish();
    }

    private void showProgressBar() {
        detailsLayout.setVisibility(View.INVISIBLE);
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBarLayout.setVisibility(View.INVISIBLE);
        detailsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Expense> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<Expense>(this) {

            @Override
            protected void onStartLoading() {
                if (NetworkConnectivityUtils.isConnected(getContext())) {
                    showProgressBar();
                    forceLoad();
                } else {
                    Toast.makeText(ExpenseDetailsActivity.this, R.string.check_network_connection, Toast.LENGTH_SHORT).show();
                    deliverResult(null);
                }
            }

            @Override
            public Expense loadInBackground() {
                long expenseID = args.getLong(EXTRA_EXPENSE_ID);
                try {
                    Response<Expense> response = ExpenseService.getExpenseClient()
                            .getExpense(expenseID).execute();

                    if (response.isSuccessful()) {
                        return response.body();
                    } else {
                        Log.i(TAG, String.format("Message : %s,Response code: %s", response.message(), response.code()));
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Expense> loader, Expense expense) {
        hideProgressBar();
        if (expense == null) {
            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        selectedExpense = expense;
        displayDetails();
    }

    @Override
    public void onLoaderReset(Loader<Expense> loader) {

    }
}
