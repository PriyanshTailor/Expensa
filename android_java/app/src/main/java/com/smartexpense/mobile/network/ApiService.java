package com.smartexpense.mobile.network;

import com.smartexpense.mobile.model.Trip;
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

    // Bill endpoints
    @GET("api/bills/user/{userId}")
    Call<List<com.smartexpense.mobile.model.Bill>> getBills(@Path("userId") String userId);

    @POST("api/bills")
    Call<com.smartexpense.mobile.model.Bill> createBill(@Body com.smartexpense.mobile.model.Bill bill);

    @PUT("api/bills/{id}/pay")
    Call<com.smartexpense.mobile.model.Bill> markBillAsPaid(@Path("id") String id);

    @DELETE("api/bills/{id}")
    Call<Void> deleteBill(@Path("id") String id);

    // Trip (Notebook) endpoints
    @GET("api/trips/user/{userId}")
    Call<List<Trip>> getTrips(@Path("userId") String userId);

    @GET("api/trips/{id}")
    Call<Trip> getTrip(@Path("id") String id);

    @POST("api/trips")
    Call<Trip> createTrip(@Body Map<String, Object> tripData);

    @PUT("api/trips/{id}/member-expense")
    Call<Trip> addTripExpense(@Path("id") String id, @Body Map<String, Object> data);

    @POST("api/trips/{id}/members")
    Call<Trip> addTripMember(@Path("id") String id, @Body Map<String, String> data);

    @DELETE("api/trips/{id}")
    Call<Void> deleteTrip(@Path("id") String id);

    @GET("api/users/{id}")
    Call<Map<String, Object>> getUserProfile(@Path("id") String id);

    @PUT("api/users/{id}/balance")
    Call<Map<String, Object>> updateBalance(@Path("id") String id, @Body Map<String, Double> data);

    @GET("api/transactions/recent")
    Call<List<Map<String, Object>>> getRecentTransactions();

    @GET("api/transactions/summary")
    Call<Map<String, Double>> getTransactionSummary();
}
