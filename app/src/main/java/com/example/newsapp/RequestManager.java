package com.example.newsapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.newsapp.Models.NewsApiResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RequestManager {
    private static final String TAG = "RequestManager";
    Context context;

    // Configure Retrofit with logging
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .client(new OkHttpClient.Builder()
                    .addInterceptor(new HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BODY)) // Logs request and response details
                    .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public void getNewsHeadlines(OnFetchDataListener listener, String category, String query) {
        CallNewsApi callNewsApi = retrofit.create(CallNewsApi.class);

        // Set default category to "general" if not provided
        category = (category == null || category.isEmpty()) ? "general" : category;

        // Log API key to ensure it's correctly loaded
        String apiKey = context.getString(R.string.api_key);
        Log.d(TAG, "API Key: " + apiKey);

        // Create the API call
        Call<NewsApiResponse> call = callNewsApi.callHeadlines("us", category, query, apiKey);

        // Log the full request URL and parameters
        Log.d(TAG, "Request URL: " + call.request().url());

        // Execute the call
        call.enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                // Log the raw response for debugging
                Log.d(TAG, "Raw Response: " + response.toString());

                if (!response.isSuccessful()) {
                    Log.e(TAG, "Request failed with status code: " + response.code() +
                            " and message: " + response.message());
                    Toast.makeText(context, "Request failed with status code: " + response.code(), Toast.LENGTH_LONG).show();
                    listener.onError("Request failed: " + response.code());
                    return;
                }

                if (response.body() == null) {
                    Log.e(TAG, "Response body is null");
                    Toast.makeText(context, "No data received from the API", Toast.LENGTH_LONG).show();
                    listener.onError("No data received from the API");
                    return;
                }

                // Log the success
                Log.d(TAG, "Response successful. Articles count: " + response.body().getArticles().size());
                listener.onFetchData(response.body().getArticles(), response.message());
            }

            @Override
            public void onFailure(Call<NewsApiResponse> call, Throwable t) {
                // Log failure details
                Log.e(TAG, "Request failed: " + t.getMessage(), t);
                Toast.makeText(context, "Request failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                listener.onError("Request failed: " + t.getMessage());
            }
        });
    }

    public RequestManager(Context context) {
        this.context = context;
    }

    public interface CallNewsApi {
        @GET("top-headlines")
        Call<NewsApiResponse> callHeadlines(
                @Query("country") String country,
                @Query("category") String category,
                @Query("q") String query,
                @Query("apiKey") String api_key
        );
    }
}
