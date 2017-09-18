package com.leafnext.shutterdrop.controller;

import android.Manifest.permission;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.leafnext.shutterdrop.controller.ImageActionsBackgroundTask.ImageAction;
import com.leafnext.shutterdrop.R;
import com.leafnext.shutterdrop.Util;


public class ScreenSlidePageFragment extends Fragment implements ImageAction {

    private FloatingActionMenu actionFAB;
    private FloatingActionButton save, apply;
    private String imageUrl = "";
    private String imageName = "";
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 100;
    private Context mContext;
    private WallpaperManager wallpaperManager;
    private int screenWidth;
    private int screenHeight;
    private final ImageAction imageAction= this;
    private ProgressBar progressBar;
    private View coordinatorLayoutViewForSnackBar;
    private int mScreenWidth;
    private Bitmap croppedWallpaper;
    ImageView mImageView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.activity_screen_slide_page_fragment, container, false);
        mContext = getActivity();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        return rootView;
    }

    public static ScreenSlidePageFragment newInstance(String url, String publicId) {

        ScreenSlidePageFragment newFragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putString("imagePathUrl", url);
        args.putString("imageName",publicId);
        newFragment.setArguments(args);

        return newFragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        Display display1 = getActivity().getWindowManager().getDefaultDisplay();
        Point point= new Point();
        display1.getSize(point);

        screenWidth = point.x;
        screenHeight = point.y;



        mImageView = getView().findViewById(R.id.imageView);


        actionFAB = getView().findViewById(R.id.social_floating_menu);
        save = getView().findViewById(R.id.save);
        apply = getView().findViewById(R.id.apply);
        wallpaperManager = WallpaperManager.getInstance(mContext.getApplicationContext());

        RelativeLayout relativeLayout =  getView().findViewById(R.id.wallpaperFragmentLayout);
        relativeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                actionFAB.close(true);
            }
        });

        progressBar =  getView().findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        coordinatorLayoutViewForSnackBar = getView().findViewById(R.id.snackbarPosition);

        imageUrl = getArguments().getString("imagePathUrl");
        imageName = getArguments().getString("imageName");

        Log.i("imageName",imageName);


        if (imageUrl.contains("http")) {
            Log.i("imagePathUrl", imageUrl);


            Glide.with(this)
                    .load(imageUrl)
                    .asBitmap()
                    .error(R.mipmap.error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            Log.i("TAG","Done loading image with exception");
                            progressBar.setVisibility(View.GONE);

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Log.i("TAG","Done loading image");
                            progressBar.setVisibility(View.GONE);

                            return false;
                        }
                    })
                    .into(mImageView);


        } else {
            Log.i("imagePathUrl", imageUrl);
        }

        actionFAB.setOnMenuButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED){

                    if (actionFAB.isOpened()){
                        actionFAB.close(true);
                    }else {
                        actionFAB.open(true);
                    }


                }else {
                    requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

                }

            }
        });

        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED){

                    saveFile();

                }else {
                    requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

                }
            }
        });

        apply.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED){
                    setWallpaper();
                }else {
                    requestPermissions(new String[]{permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE:{
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i("TAG","Permission Granted");
                }else {
                    Toast.makeText(mContext,"Need permission to download image",Toast.LENGTH_LONG).show();

                }
            }
        }
    }


    private void saveFile(){
        Glide.with(this)
                .load(imageUrl)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        String error = "No Internet, Unable to save Image";
                        Util.runSnackBar(error,coordinatorLayoutViewForSnackBar );
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Log.i("SaveImageError","No issues everything looks okay");

                        return false;
                    }
                })
                .into(saveTarget);
        actionFAB.close(true);

        Log.i("SaveFilemethod","Save file method has been triggered");
    }

    private SimpleTarget<Bitmap> saveTarget = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {


            Bitmap croppedImage = centerCropWallpaper(resource,wallpaperManager.getDesiredMinimumHeight());


            new ImageActionsBackgroundTask(imageAction,imageName,croppedImage,mContext,true,null).execute();

        }
    };

    private void setWallpaper(){
        Glide.with(this)
                .load(imageUrl)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        String wallpaperError = "No Internet, Unable to set Wallpaper";
                        Util.runSnackBar(wallpaperError,coordinatorLayoutViewForSnackBar );
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(applyTarget);

        actionFAB.close(true);
    }


    private SimpleTarget<Bitmap> applyTarget = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

            Bitmap croppedImage = centerCropWallpaper(resource,wallpaperManager.getDesiredMinimumHeight());

            new ImageActionsBackgroundTask(imageAction,imageName,croppedImage,mContext,false,wallpaperManager).execute();

        }
    };

    private Bitmap centerCropWallpaper(Bitmap wallpaper, int desiredHeight){
        float scale = (float) desiredHeight / wallpaper.getHeight();
        int scaledWidth = (int) (scale * wallpaper.getWidth());
        int deviceWidth = mScreenWidth;
        int imageCenterWidth = scaledWidth /2;
        int widthToCut = imageCenterWidth - deviceWidth / 2;
        int leftWidth = scaledWidth - widthToCut;
        Bitmap scaledWallpaper = Bitmap.createScaledBitmap(wallpaper, scaledWidth, desiredHeight, false);

        return croppedWallpaper = Bitmap.createBitmap(
                scaledWallpaper,
                widthToCut,
                0,
                leftWidth,
                desiredHeight
        );
    }



    @Override
    public void actionDone(String success) {

        switch (success){

            case "save_wallpaper": Util.runSnackBar("Wallpaper Saved !",coordinatorLayoutViewForSnackBar);
                break;
            case "set_wallpaper" : Util.runSnackBar("Wallpaper has been set !",coordinatorLayoutViewForSnackBar);
                break;
            default: Util.runSnackBar("Something Went Wrong :(",coordinatorLayoutViewForSnackBar);
        }

    }

}
