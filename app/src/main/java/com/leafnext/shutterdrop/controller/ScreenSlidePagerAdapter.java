package com.leafnext.shutterdrop.controller;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Created by syedehteshamuddin on 2017-09-18.
 */

class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    private String url;
    private String imagePublicId;
    private Context mContext;


    ScreenSlidePagerAdapter(FragmentManager fm, Context context) {
        super(fm);

        mContext = context;

    }


    @Override
    public Fragment getItem(int position) {

        Log.i("Position", Integer.toString(position) +""+ "get item");

        if (MainActivity.imageUrls != null & MainActivity.imageUrls.size() > 0){

            url = MainActivity.imageUrls.get(position);
            imagePublicId = MainActivity.imageNames.get(position);

            Log.i("url",url);

            Log.i("url",Integer.toString(position));

            return ScreenSlidePageFragment.newInstance(url,imagePublicId);

        }else {

            Log.i("AdapterClass","mresources not iniliased");
            return null;

        }

    }

    @Override
    public int getItemPosition(Object object) {

        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount() {
        if (MainActivity.imageUrls != null)
            return MainActivity.imageUrls.size();
        return 0;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

    }
}
