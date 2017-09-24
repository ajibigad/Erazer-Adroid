package com.ajibigad.erazer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.ajibigad.erazer.R;
import com.ajibigad.erazer.data.User;
import com.ajibigad.erazer.network.UserService;

import butterknife.ButterKnife;
import io.realm.Realm;

import static com.ajibigad.erazer.network.UserService.AUTH_USERNAME_KEY;

public class MainActivity extends AppCompatActivity {

    private static final int LOGIN = 90;
    private static final int DASHBOARD = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        GoogleApiAvailability.makeGooglePlayServicesAvailable();

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case LOGIN:
                        openLogin();
                        break;
                    case DASHBOARD:
                        openDashboard();
                        break;
                }
            }
        };
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                final String username = preferences.getString(AUTH_USERNAME_KEY, "");
                if (username.isEmpty()) {
                    handler.sendEmptyMessage(LOGIN);
                } else {
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            User user = realm.where(User.class)
                                    .equalTo("username", username).findFirst();
                            if (user == null) handler.sendEmptyMessage(LOGIN);
                            UserService.setCurrentUser(realm.copyFromRealm(user));
                            handler.sendEmptyMessage(DASHBOARD);
                        }
                    });
                }
            }
        }, 2000);
    }

    public void openDashboard() {
        Intent dashboardIntent = new Intent(this, DashboardActivity.class);
        startActivity(dashboardIntent);
        finish();
    }

    public void openLogin() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        GoogleApiAvailability.makeGooglePlayServicesAvailable();
    }
}
