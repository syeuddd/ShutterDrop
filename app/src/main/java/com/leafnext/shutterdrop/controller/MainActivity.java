package com.leafnext.shutterdrop.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.leafnext.shutterdrop.controller.MyAsyncTask.AsyncResponse;
import com.leafnext.shutterdrop.model.ImageInfo;
import com.leafnext.shutterdrop.model.ImageResponse;
import com.leafnext.shutterdrop.model.Resource;
import com.leafnext.shutterdrop.network.CloudinaryService;
import com.leafnext.shutterdrop.network.RetrofitClient;
import com.leafnext.shutterdrop.R;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeader.OnAccountHeaderListener;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncResponse {


    private List<Resource> mResources;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    private Context mContext;

    private String nextCursor = "";

    private AccountHeader headerResult = null;

    private Drawer result = null;

    public static ArrayList<String> imageUrls;

    public static ArrayList<String> imageNames;

    private ArrayList<String> imageUrlBackUp;

    private ArrayList<String> imageNameBackUp;

    private String category = "";

    private SharedPreferences sharedPrefs;
    private Gson gson;
    private Editor editor;

    private ImageResponse imageResponse;

    private ImageInfo imageInfo;

    private AsyncTask imageDownloadAsyncTask;

    private final AsyncResponse myAsync = this;

    private CloudinaryService mCloudinaryService;

    public RetrofitClient mRetrofitClient;

    private ConnectivityManager  connectivityManager;

    private NetworkInfo activeNetworkInfo;

    private final String BASE_URL = "https://api.cloudinary.com/v1_1/dajatnqrw/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mContext = this;

        mRetrofitClient = new RetrofitClient(mContext);

        mCloudinaryService = mRetrofitClient.getClient(BASE_URL).create(CloudinaryService.class);

        View coordinatedLayout = findViewById(R.id.snackbarPositionForNoInternet);

        imageInfo = ImageInfo.getInstance();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        gson = new Gson();

        imageUrls = new ArrayList<>();
        imageNames = new ArrayList<>();
        imageUrlBackUp = new ArrayList<>();
        imageNameBackUp = new ArrayList<>();

        navigationDrawer();

        mPager = findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new OnPageChangeListener() {


            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (imageUrls.size() - 1 == position & !nextCursor.equals("") & isNetworkAvailable(mContext)) {

                    if (mResources != null) {
                        mResources.clear();
                    }

                    fetchRequest(imageDownloadAsyncTask);

                }

                if ((imageUrls.size() - 1 == position) & (!isNetworkAvailable(mContext))) {


                    Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
                }


                if (imageUrls.size() -1 == position & nextCursor.equals("")& isNetworkAvailable(mContext)){

                    Toast.makeText(MainActivity.this, "Reached end please browse other categories", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (isNetworkAvailable(mContext)) {


            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


            if (category.equals("")) {
                category = "startup";
            }

            fetchRequest(imageDownloadAsyncTask);

        } else {

            runSnackBar(coordinatedLayout);

            String jSon = sharedPrefs.getString("imageInfo", "");

            if (!jSon.equals("")) {

                imageInfo = gson.fromJson(jSon, ImageInfo.class);

                imageUrls.addAll(imageInfo.getImageUrls());
                imageNames.addAll(imageInfo.getPublicImageId());
                nextCursor = imageInfo.getNextcursor();
                category = imageInfo.getCategoryType();

                if (mPagerAdapter == null) {

                    mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), mContext);

                    mPager.setAdapter(mPagerAdapter);


                } else {
                    mPagerAdapter.notifyDataSetChanged();
                }

            } else {
                Toast.makeText(MainActivity.this, "No stored data please connect device to internet", Toast.LENGTH_SHORT).show();
            }


        }
    }




    private void navigationDrawer() {

        Toolbar toolbar = findViewById(R.id.my_action_bar_toolbar);
        setSupportActionBar(toolbar);
        toolbar.bringToFront();


        new DrawerBuilder().withActivity(this).build();


        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Category");

        //create the drawer and remember the `Drawer` result object
        result = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(false)
                .withAccountHeader(buildHeader())
                .addDrawerItems(
                        item1,
                        new SecondaryDrawerItem().withName("New Uploads").withIcon(R.mipmap.upload),
                        new SecondaryDrawerItem().withName("Abstract").withIcon(R.mipmap.abstract_icon),
                        new SecondaryDrawerItem().withName("Auto").withIcon(R.mipmap.auto),
                        new SecondaryDrawerItem().withName("Holdays").withIcon(R.mipmap.holiday),
                        new SecondaryDrawerItem().withName("Ainimal").withIcon(R.mipmap.paw),
                        new SecondaryDrawerItem().withName("Earth").withIcon(R.mipmap.globe),
                        new SecondaryDrawerItem().withName("Pattern").withIcon(R.mipmap.binoculars),
                        new SecondaryDrawerItem().withName("Games").withIcon(R.mipmap.gamepad),
                        new SecondaryDrawerItem().withName("Love").withIcon(R.mipmap.heart),
                        new SecondaryDrawerItem().withName("Cartoon").withIcon(R.mipmap.cartoon)
                )
                .withOnDrawerItemClickListener(new OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        Log.i("Drawer", position + " Drawer position");

                        if (isNetworkAvailable(mContext)) {

                            switch (position) {

                                case 2:moveToNextCategory("newUploads");
                                    break;
                                case 3:moveToNextCategory("abstract");
                                    break;
                                case 4:moveToNextCategory("auto");
                                    break;
                                case 5:moveToNextCategory("holidays");
                                    break;
                                case 6:moveToNextCategory("animals");
                                    break;
                                case 7:moveToNextCategory("earth");
                                    break;
                                case 8:moveToNextCategory("pattern");
                                    break;
                                case 9:moveToNextCategory("games");
                                    break;
                                case 10:moveToNextCategory("love");
                                    break;
                                case 11:moveToNextCategory("cartoons");
                                    break;
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "No Internet, please connect device to internet", Toast.LENGTH_SHORT).show();
                        }
                        return false;

                    }
                })
                .build();

        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

//        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void moveToNextCategory(String mCategory) {
        mPager.setCurrentItem(0);
        clear();
        category = mCategory;
        cancelAsyncTask(imageDownloadAsyncTask);
        fetchRequest(imageDownloadAsyncTask);
    }

    private void cancelAsyncTask(AsyncTask mImageDownloadTask) {

        if (mImageDownloadTask != null){

            if ((imageDownloadAsyncTask.getStatus() == Status.RUNNING) & (imageDownloadAsyncTask.getStatus() == Status.PENDING)) {

                imageDownloadAsyncTask.cancel(true);

            }
        }

    }

    private void fetchRequest(AsyncTask mImageDownloadTask){

        if (mImageDownloadTask != null){

            if ((imageDownloadAsyncTask.getStatus() != Status.RUNNING) & (imageDownloadAsyncTask.getStatus() != Status.PENDING)){

                imageDownloadAsyncTask = new MyAsyncTask(myAsync, mContext, category, nextCursor, mCloudinaryService, mRetrofitClient).execute();

            }

        }else {
            imageDownloadAsyncTask = new MyAsyncTask(myAsync, mContext, category, nextCursor, mCloudinaryService, mRetrofitClient).execute();
        }

    }


    private void clear() {

        if (mResources != null) {
            mResources.clear();
        }
        imageUrls.clear();
        imageNames.clear();
        nextCursor = "";
        category = "";
        if (mPagerAdapter != null) {
            mPagerAdapter.notifyDataSetChanged();
        }

    }

    private AccountHeader buildHeader() {

        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.mipmap.sidebar_material_design)
                .withCompactStyle(false)
                .withOnAccountHeaderListener(new OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                }).build();

        return headerResult;

    }


    @Override
    protected void onDestroy() {

        clear();
        mPager.setAdapter(null);
        super.onDestroy();

    }

    private void runSnackBar(View coordinatorLayoutView){
        String status = "No Internet Connection";
        Snackbar mSnackbar = Snackbar.make(coordinatorLayoutView,status,Snackbar.LENGTH_SHORT);
        View mView = mSnackbar.getView();
        TextView mTextView = mView.findViewById(android.support.design.R.id.snackbar_text);
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1){
            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }else {
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        mSnackbar.show();
    }


    @Override
    public void processFinish(ImageResponse imageResourceObject) {

        if (imageResourceObject != null) {
            imageResponse = imageResourceObject;

            if (imageResponse.getResources() != null) {

                mResources = imageResponse.getResources();

                if (imageUrlBackUp.size()>0){
                    imageUrlBackUp.clear();
                }

                if (imageNameBackUp.size()>0){
                    imageNameBackUp.clear();
                }

                for (int i = 0; i < mResources.size(); i++) {

                    String imageUrl = mResources.get(i).getUrl();

                    String imageName = mResources.get(i).getPublicId();

                    Log.i("retrofitImageUrl", imageName);

                    imageUrls.add(imageUrl);
                    imageNames.add(imageName);

                    imageUrlBackUp.add(imageUrl);
                    imageNameBackUp.add(imageName);

                }

                if (imageResponse.getNextCursor() != null) {
                    nextCursor = imageResponse.getNextCursor();


                } else {
                    nextCursor = "";
                }

                imageInfo.setImageUrls(imageUrlBackUp);
                imageInfo.setPublicImageId(imageNameBackUp);
                imageInfo.setNextcursor(nextCursor);
                imageInfo.setCategoryType(category);


                String jSon = gson.toJson(imageInfo);
                //Clear exsiting stuff
                editor = sharedPrefs.edit();
                editor.clear();
                editor.apply();
                // add new stuff
                editor.putString("imageInfo", jSon);
                editor.commit();

                if (mPagerAdapter == null) {

                    mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), mContext);

                    mPager.setAdapter(mPagerAdapter);

                    mPagerAdapter.notifyDataSetChanged();

                } else {
                    mPagerAdapter.notifyDataSetChanged();
                }


            } else {
                Toast.makeText(mContext, "No valid data was returned please try again", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(mContext, "Something went wrong please try again", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isNetworkAvailable(Context context) {

        try{

            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }catch (Exception e){
            e.printStackTrace();
        }

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }
}
