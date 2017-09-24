package com.ajibigad.erazer.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ajibigad.erazer.data.Role;
import com.ajibigad.erazer.data.User;
import com.ajibigad.erazer.fcm.ErazerFirebaseInstanceIdService;
import com.ajibigad.erazer.service.ErazerIntentService;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import io.realm.Realm;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ajibigad on 06/08/2017.
 */

public class UserService implements ErazerService {

    public static final String AUTH_USERNAME_KEY = "pref_username";

    private static final String USER_API = API_BASE_URL + "/user/";
    private static User currentUser;
    private static UserClient userClient;
    private static OkHttpClient httpClient;

    private static void setupRetrofit(String username, String password) {
        setupRetrofit(Credentials.basic(username, password));
    }

    private static void setupRetrofit(final String authToken) {

        httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request request = chain.request();
                        Request requestWithAuthentication = request.newBuilder()
                                .header("Authorization", authToken).build();
                        return chain.proceed(requestWithAuthentication);
                    }
                })
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(USER_API)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        Retrofit retrofit = builder.client(httpClient).build();

        userClient = retrofit.create(UserClient.class);
    }

    public static UserClient getUserClient(String username, String password) {
        setupRetrofit(username, password);
        return userClient;
    }

    public static UserClient getUserClient(String token) {
        setupRetrofit(token);
        return userClient;
    }

    public static UserClient getUserClient() {
        if (userClient == null) {
            return getUserClient(getCurrentUser().getToken());
        }
        return userClient;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void persistLoginDetails(final User user, String authToken, Context context) {
        user.setToken(authToken);
        user.setAdmin(hasAdminRole(user));
        UserService.setCurrentUser(user);

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(user);
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(AUTH_USERNAME_KEY, user.getUsername()).apply();
        ErazerFirebaseInstanceIdService.getTokenAndUpdateServer(context);

    }

    public static void removeLoginDetails(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().remove(UserService.AUTH_USERNAME_KEY).apply();

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.where(User.class)
                        .equalTo("username", UserService.getCurrentUser().getUsername()).findFirst();
                user.getRoles().deleteAllFromRealm();
                user.deleteFromRealm();
            }
        });

        String fcmToken = FirebaseInstanceId.getInstance().getToken();
        ErazerIntentService.startActionDeleteFcmToken(context, fcmToken);

        UserService.setCurrentUser(null);
    }

    private static boolean hasAdminRole(User user) {
        for (Role role : user.getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                return true;
            }
        }
        return false;
    }
}
