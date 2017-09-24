package com.ajibigad.erazer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.activities.fragments.ExpenseOverviewFragment;
import com.ajibigad.erazer.network.UserService;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.ajibigad.erazer.activities.fragments.ExpenseOverviewFragment.EXTRA_USERNAME;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        ExpenseOverviewFragment fragment = new ExpenseOverviewFragment();
        if (!UserService.getCurrentUser().isAdmin()) {
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_USERNAME, UserService.getCurrentUser().getUsername());
            fragment.setArguments(bundle);

            toolbar.setTitle(String.format("%s's Overview", UserService.getCurrentUser().getUsername()));
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.overview_fragment, fragment).commit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        UserService.removeLoginDetails(this);

        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @OnClick(R.id.btn_create_expense)
    public void createExpense() {
        Intent createExpenseIntent = new Intent(this, CreateExpenseActivity.class);
        startActivity(createExpenseIntent);
    }

}
