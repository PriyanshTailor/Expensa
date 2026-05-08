package com.smartexpense.mobile.network;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> credentials);

    @POST("api/auth/register")
    Call<Map<String, Object>> register(@Body Map<String, String> userData);

    // Transaction endpoints
    @GET("api/transactions")
    Call<List<Map<String, Object>>> getTransactions();

    @POST("api/transactions")
    Call<Map<String, Object>> createTransaction(@Body Map<String, Object> transaction);

    @PUT("api/transactions/{id}")
    Call<Map<String, Object>> updateTransaction(@Path("id") String id, @Body Map<String, Object> transaction);

    @DELETE("api/transactions/{id}")
    Call<Void> deleteTransaction(@Path("id") String id);

    // Budget endpoints
    @GET("api/budgets")
    Call<List<Map<String, Object>>> getBudgets();

    @POST("api/budgets")
    Call<Map<String, Object>> createBudget(@Body Map<String, Object> budget);

    @PUT("api/budgets/{id}")
    Call<Map<String, Object>> updateBudget(@Path("id") String id, @Body Map<String, Object> budget);

    @DELETE("api/budgets/{id}")
    Call<Void> deleteBudget(@Path("id") String id);
}
