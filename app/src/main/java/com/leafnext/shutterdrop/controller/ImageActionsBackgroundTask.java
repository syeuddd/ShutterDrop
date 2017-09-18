package com.leafnext.shutterdrop.controller;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by syedehteshamuddin on 2017-09-18.
 */

class ImageActionsBackgroundTask extends AsyncTask<Void,Void,String> {

    private ImageAction mDelegate = null;
    private String mImageName;
    private Bitmap mBitmap;
    private Context mContext;
    private Boolean mSave;
    private WallpaperManager mWallpaperManager;
    private final String saveString = "save_wallpaper";
    private final String wallpaperString = "set_wallpaper";
    private boolean dirResult;

    interface ImageAction {
        void actionDone(String success);
    }

    ImageActionsBackgroundTask(ImageAction delegate,
                               String imageName,
                               Bitmap bitmap,
                               Context context,
                               Boolean save,
                               WallpaperManager wallpaperManager) {

        mDelegate = delegate;
        //      mImageUrl = imageUrl;
        mImageName = imageName;
        mBitmap = bitmap;
        mContext = context;
        mSave = save;
        mWallpaperManager = wallpaperManager;
    }


    @Override
    protected String doInBackground(Void... params) {


        if (mSave) {

            if (saveFile(mImageName,mBitmap)) {
                return saveString;
            }else {
                return "";
            }

        } else {

            saveFile(mImageName,mBitmap);

            if (mWallpaperManager != null ) {

                try {
                    mWallpaperManager.setBitmap(mBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }

            }
            else {
                return "";
            }

        }

        return wallpaperString;
    }




    @Override
    protected void onPostExecute(String mSuccess) {
        super.onPostExecute(mSuccess);
        mDelegate.actionDone(mSuccess);



    }

    private boolean saveFile(String mImageName, Bitmap mImage) {

        final String path = Environment.getExternalStorageDirectory().toString();


        File dir = new File(path + "/Wallypaper");
        if (!dir.exists()) {
            dirResult = dir.mkdirs();
        }


        final File file = new File(dir, mImageName + ".jpg");


        if (!file.exists()) {

            try {

                FileOutputStream out = new FileOutputStream(file);
                mImage.compress(CompressFormat.JPEG, 100, out);
                out.close();
                scanFile(file.getAbsolutePath());


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }
        return true;
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(mContext,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        if (uri == null){
                            Log.i("WallpaperScanning", "Scanning failed " + path);
                        }else {
                            Log.i("WallpaperScanning", "Scanning successeded " + path);
                        }

                    }
                });
    }

}