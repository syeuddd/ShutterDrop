package com.leafnext.shutterdrop.network;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by syedehteshamuddin on 2017-09-18.
 */

public class RetrofitClient extends AppCompatActivity {

    private Context mContext;

    private final String API_KEY = "731726557119943";

    private final String API_SECRET = "NqBysVhnyhb8OCDDc6Sd9Vtp3RE";

    private String credentials = Credentials.basic(API_KEY, API_SECRET);

    private Retrofit retrofit = null;

    private ConnectivityManager connectivityManager;

    private NetworkInfo activeNetworkInfo;

    public RetrofitClient(Context base) {
        mContext = base;
    }


    private Cache getCache() {

        int cacheSize = 10 * 1024 * 1024; // 10 MB
        return new Cache(mContext.getCacheDir(), cacheSize);
    }


    public OkHttpClient getClient() {

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(Level.BASIC);


        return new OkHttpClient.Builder()

                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {

                        return response.request().newBuilder().header("Authorization", credentials).build();
                    }
                })
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {


                        Request request = (chain.request().newBuilder()
                                .header("Accept", "Application/JSON")
                                .header("Authorization", credentials).build());


                        return chain.proceed(request);

                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(provideOfflineCacheInterceptor())
                .addNetworkInterceptor(provideCacheInterceptor())
                .cache(getCache())
                .build();
    }


    private Interceptor provideCacheInterceptor() {

        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Response response = chain.proceed(chain.request());


                CacheControl cacheControl = new CacheControl.Builder()
                        .maxAge((60 * 12), TimeUnit.MINUTES)
                        .build();

                return response.newBuilder()
                        .header("Cache-Control", cacheControl.toString())
                        .build();
            }
        };


    }

    private Interceptor provideOfflineCacheInterceptor() {


        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                if (isNetworkAvailable()) {
                    CacheControl cacheControl = new CacheControl.Builder()
                            .maxStale(1, TimeUnit.DAYS)
                            .build();

                    request = request.newBuilder()
                            .cacheControl(cacheControl)
                            .build();
                }

                return chain.proceed(request);

            }
        };
    }





    public Retrofit getClient(String baseUrl) {


        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .client(getClient())
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;

    }

    private boolean isNetworkAvailable() {

        try{

            connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }catch (Exception e){
            e.printStackTrace();
        }

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }
}
