package com.ajibigad.erazer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.data.User;
import com.ajibigad.erazer.network.UserService;
import com.ajibigad.erazer.utils.NetworkConnectivityUtils;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Credentials;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<User> {

    private static final int LOGIN_LOADER = 80097;
    @BindViews({R.id.input_username, R.id.input_password})
    public List<TextInputEditText> editTexts;

    @BindViews({R.id.input_layout_username, R.id.input_layout_password})
    public List<TextInputLayout> textInputLayouts;

    @BindView(R.id.btn_login)
    public Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_login)
    public void performLogin() {
        if (!validateTextFields()) {
            return;
        }

        //use the username and pasword to ping the server
        //if bad credentials is received then login failed
        //if 200 login passed

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<User> expensesOverviewLoader = loaderManager.getLoader(LOGIN_LOADER);
        if (expensesOverviewLoader == null) {
            loaderManager.initLoader(LOGIN_LOADER, null, this);
        } else {
            loaderManager.restartLoader(LOGIN_LOADER, null, this);
        }
    }

    private boolean validateTextFields() {
        int count = 0;
        for (TextInputLayout textInputLayout : textInputLayouts) {
            if (editTexts.get(count).getText().toString().trim().isEmpty()) {
                textInputLayout.setError(getString(R.string.blank_field_msg));
                requestFocus(editTexts.get(count));
                return false;
            } else {
                textInputLayout.setErrorEnabled(false);
            }
            count++;
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    static final ButterKnife.Setter<View, Boolean> ENABLED = new ButterKnife.Setter<View, Boolean>() {
        @Override
        public void set(View view, Boolean value, int index) {
            view.setEnabled(value);
        }
    };

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<User>(this) {

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                ButterKnife.apply(textInputLayouts, ENABLED, false);
                btnLogin.setEnabled(false);
                if (!NetworkConnectivityUtils.isConnected(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, getString(R.string.check_network_connection), Toast.LENGTH_SHORT).show();
                    ButterKnife.apply(textInputLayouts, ENABLED, true);
                    btnLogin.setEnabled(true);
                } else forceLoad();
            }

            @Override
            public User loadInBackground() {
                try {
                    String username = editTexts.get(0).getText().toString(),
                            password = editTexts.get(1).getText().toString();
                    Response<User> response = UserService.getUserClient(username, password)
                            .getUserDetails(username).execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    } else {
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
    public void onLoadFinished(Loader<User> loader, User user) {
        if (user != null) {

            String authToken = Credentials.basic(user.getUsername(), editTexts.get(1).getText().toString());
            UserService.persistLoginDetails(user, authToken, this);

            Intent dashboardIntent = new Intent(this, DashboardActivity.class);
            startActivity(dashboardIntent);
            finish();
        } else {
            Toast.makeText(this, "Login failed\n Please check credentials", Toast.LENGTH_LONG).show();
            ButterKnife.apply(textInputLayouts, ENABLED, true);
            btnLogin.setEnabled(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<User> loader) {

    }
}
