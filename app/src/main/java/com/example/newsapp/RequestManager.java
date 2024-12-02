package com.example.newsapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.newsapp.Models.NewsApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RequestManager {
    Context context;

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public void getNewsHeadlines(OnFetchDataListener listener, String category, String query) {
        CallNewsApi callNewsApi = retrofit.create(CallNewsApi.class);

        // Set default category to "general" if not provided
        category = (category == null || category.isEmpty()) ? "general" : category;

        // Log API key to ensure it's correct
        String apiKey = context.getString(R.string.api_key);
        Log.d("RequestManager", "API Key: " + apiKey);

        // Call the API with the given parameters
        Call<NewsApiResponse> call = callNewsApi.callHeadlines("us", category, query, apiKey);

        call.enqueue(new Callback<NewsApiResponse>() {
            @Override
            public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                // Check if the response is successful
                if (!response.isSuccessful()) {
                    // Log the failure details
                    Log.e("RequestManager", "Request failed with status code: " + response.code() + " and message: " + response.message());
                    Toast.makeText(context, "Request failed with status code: " + response.code(), Toast.LENGTH_LONG).show();
                    listener.onError("Request failed: " + response.code());
                    return;
                }

                // Check if response body is null
                if (response.body() == null) {
                    Log.e("RequestManager", "Response body is null");
                    Toast.makeText(context, "No data received from the API", Toast.LENGTH_LONG).show();
                    listener.onError("No data received from the API");
                    return;
                }

                // If everything is fine, pass the data to the listener
                listener.onFetchData(response.body().getArticles(), response.message());
            }

            @Override
            public void onFailure(Call<NewsApiResponse> call, Throwable t) {
                // Log failure and display the error
                Log.e("RequestManager", "Request failed: " + t.getMessage(), t);
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
