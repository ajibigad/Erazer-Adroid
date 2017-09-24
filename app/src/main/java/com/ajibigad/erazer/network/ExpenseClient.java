package com.ajibigad.erazer.network;

import com.ajibigad.erazer.data.Expense;
import com.ajibigad.erazer.data.ExpensesOverview;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ajibigad on 29/07/2017.
 */

public interface ExpenseClient {

    //get expenses sorted by any of its attributes
    //get expenses by a username, state, month and year
    //get all expenses overview
    //get expenses overview by username

    @GET("all")
    public Call<List<Expense>> getAllExpenses(@Query("search") String searchQuery, @Query("sort_by") String sortBy);

    @GET("overview/{username}")
    public Call<List<ExpensesOverview>> getUserExpensesOverview(@Path("username") String username,
                                                                @Query("month") int month, @Query("year") int year);

    @GET("overview/all")
    public Call<List<ExpensesOverview>> getAllExpensesOverview(@Query("month") int month, @Query("year") int year);

    @POST("./")
    public Call<Expense> createExpense(@Body Expense expense);

    @GET("{id}")
    public Call<Expense> getExpense(@Path("id") long id);

    @PATCH("{id}/changeState")
    public Call<Expense> changeExpenseState(@Path("id") long id, @Query("state") String state);
}
