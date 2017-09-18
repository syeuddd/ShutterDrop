package com.leafnext.shutterdrop.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.leafnext.shutterdrop.model.ImageResponse;
import com.leafnext.shutterdrop.network.CloudinaryService;
import com.leafnext.shutterdrop.network.RetrofitClient;

import java.io.IOException;

import retrofit2.Response;


 class MyAsyncTask extends AsyncTask<Void,Void,ImageResponse> {

    private CloudinaryService mCloudinaryService;

    private Context mContext;

    private ImageResponse imageResponse;

    private String tag;

    private String mNextCursor;

    private RetrofitClient mRetrofitClient;

    private ProgressBar mProgressBar;

    interface AsyncResponse{
        void processFinish(ImageResponse imageResourceObject);
    }

    private AsyncResponse delegate = null;

    MyAsyncTask(AsyncResponse delegate, Context context, String tags, String nextCursor, CloudinaryService cloudinaryService, RetrofitClient retrofitClient) {

        this.delegate = delegate;
        this.tag = tags;
        this.mNextCursor = nextCursor;
        mCloudinaryService = cloudinaryService;
        mRetrofitClient = retrofitClient;
        mContext = context;
        imageResponse = new ImageResponse();

    }

    @Override
    public void onPreExecute() {
        mProgressBar = new ProgressBar(mContext);
        mProgressBar.setVisibility(View.VISIBLE);

        Log.i("AsyncTask","dialog has started");
    }

    @Override
    public ImageResponse doInBackground(Void... params) {

        try {
            Response<ImageResponse> mImageResponse = mCloudinaryService.getImageResource(tag, mNextCursor).execute();


            if (mImageResponse.raw().cacheResponse() != null) {
                imageResponse = mImageResponse.body();
            }


            if (mImageResponse.raw().networkResponse() != null){
                imageResponse = mImageResponse.body();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageResponse;
    }


    @Override
    public void onPostExecute(ImageResponse imageResponse) {
        mProgressBar.setVisibility(View.GONE);
        delegate.processFinish(imageResponse);
    }
}
