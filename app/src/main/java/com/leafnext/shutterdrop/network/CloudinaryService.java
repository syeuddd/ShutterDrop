package com.leafnext.shutterdrop.network;

import com.leafnext.shutterdrop.model.ImageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface CloudinaryService {

//    @GET("resources/image")
//    Call<ImageResponse> getImageResource();
//
//    @GET("resources/image/tags/{tags}?max_results=4")
//    Call<ImageResponse> getImageResource(@Path("tags") String tag);

    @GET("resources/image/tags/{tags}?max_results=4")
    Call<ImageResponse> getImageResource(@Path("tags") String tag, @Query("next_cursor") String nextCursor);
}