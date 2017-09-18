package com.leafnext.shutterdrop.model;

import android.os.Parcel;

import java.util.ArrayList;

/**
 * Created by syedehteshamuddin on 2017-09-18.
 */

public class ImageInfo implements android.os.Parcelable {



    private ArrayList<String> mImageUrls;
    private ArrayList<String> mPublicImageId;
    private String nextcursor;
    private String categoryType;


    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getNextcursor() {
        return nextcursor;
    }

    public void setNextcursor(String nextcursor) {
        this.nextcursor = nextcursor;
    }

    private static ImageInfo instance;

    public static ImageInfo getInstance(){
        if (instance == null){
            instance = new ImageInfo();
        }

        return instance;
    }

//    public ImageInfo(ArrayList<String> imageUrls, ArrayList<String> imageIds){
//
//        this.mImageUrls = imageUrls;
//        this.mPublicImageId = imageIds;
//    }


    public ArrayList<String> getImageUrls() {
        return mImageUrls;
    }

    public void setImageUrls(ArrayList<String> imageUrls) {
        this.mImageUrls = imageUrls;
    }

    public ArrayList<String> getPublicImageId() {
        return mPublicImageId;
    }

    public void setPublicImageId(ArrayList<String> publicImageId) {
        this.mPublicImageId = publicImageId;
    }

    public void clearData(){
        mImageUrls.clear();
        mPublicImageId.clear();
        nextcursor = "";
        categoryType = "";
    }

    public void clearPublicImageId(){
        mImageUrls.clear();
    }

    public boolean checkImageUrlsExist(){
        return mImageUrls != null && mImageUrls.size() > 0;
    }

    public boolean checkPublicImageIdExist(){
        return mPublicImageId != null && mPublicImageId.size() > 0;
    }


    public ImageInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.mImageUrls);
        dest.writeStringList(this.mPublicImageId);
        dest.writeString(this.nextcursor);
        dest.writeString(this.categoryType);
    }

    public ImageInfo(Parcel in) {
        this.mImageUrls = in.createStringArrayList();
        this.mPublicImageId = in.createStringArrayList();
        this.nextcursor = in.readString();
        this.categoryType = in.readString();
    }

    public static final Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {
        @Override
        public ImageInfo createFromParcel(Parcel source) {
            return new ImageInfo(source);
        }

        @Override
        public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }
    };
}