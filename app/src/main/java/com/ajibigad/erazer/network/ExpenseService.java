package com.ajibigad.erazer.network;

import com.ajibigad.erazer.data.Expense;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ajibigad on 29/07/2017.
 */

public class ExpenseService implements ErazerService {

    public final static String EXPENSE_API = API_BASE_URL + "/expense/";
    public final static String EXPENSE_IMAGE_API = EXPENSE_API + "images/";

    public final static String TAG = ExpenseService.class.getSimpleName();

    private static ExpenseClient expenseClient;

    private static OkHttpClient httpClient;

    public static OkHttpClient getHttpClient() {
        return httpClient;
    }

    static {
        setupRetrofit();
    }

    private static void setupRetrofit() {

        httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request request = chain.request();
                        Request requestWithAuthentication = request.newBuilder()
                                .header("Authorization", UserService.getCurrentUser().getToken()).build();
                        return chain.proceed(requestWithAuthentication);
                    }
                })
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(EXPENSE_API)
                        .addConverterFactory(
                                GsonConverterFactory.create()
                        );

        Retrofit retrofit = builder.client(httpClient).build();

        expenseClient = retrofit.create(ExpenseClient.class);
    }

    public static ExpenseClient getExpenseClient() {
        return expenseClient;
    }

    public static String getImageUrlFromExpense(Expense expense) {
        return new StringBuilder()
                .append(EXPENSE_IMAGE_API)
                .append(expense.getUser().getUsername())
                .append("/")
                .append(expense.getProof()).toString();
    }

}
