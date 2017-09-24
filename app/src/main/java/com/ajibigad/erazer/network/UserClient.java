package com.ajibigad.erazer.network;

import com.ajibigad.erazer.data.Expense;
import com.ajibigad.erazer.data.User;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ajibigad on 13/08/2017.
 */

public interface UserClient {

    @GET("{username}")
    public Call<User> getUserDetails(@Path("username") String username);

    @POST("fcm")
    public Call<Void> addUserFcmToken(@Body RequestBody fcmToken);

    @HTTP(method = "DELETE", path = "fcm", hasBody = true)
    public Call<Void> deleteUserFcmToken(@Body RequestBody fcmToken);

    @GET("{username}/expenses")
    public Call<List<Expense>> getUserExpenses(@Path("username") String username,
                                               @Query("search") String searchQuery, @Query("sort_by") String sortBy);
}
