package com.smartexpense.mobile.network;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static String customBaseUrl = null;

    public static void setCustomBaseUrl(String url) {
        customBaseUrl = url;
        retrofit = null; // force rebuild
    }

    public static String getBaseUrl() {
        if (customBaseUrl != null) {
            return customBaseUrl;
        }
        // Production API deployed on Render
        return "https://expensaa.onrender.com/";
    }
    private static Retrofit retrofit = null;
    private static String authToken = null;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json");
                        
                        if (authToken != null && !authToken.isEmpty()) {
                            android.util.Log.d("RetrofitClient", "Adding Auth Header: Bearer " + authToken.substring(0, Math.min(authToken.length(), 10)) + "...");
                            requestBuilder.header("Authorization", "Bearer " + authToken);
                        } else {
                            android.util.Log.w("RetrofitClient", "No Auth Token available for request to: " + original.url());
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
