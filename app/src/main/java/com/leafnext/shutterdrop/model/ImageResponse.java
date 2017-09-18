package com.leafnext.shutterdrop.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by syedehteshamuddin on 2017-09-18.
 */

public class ImageResponse {

    @SerializedName("resources")
    @Expose
    private List<Resource> resources = null;
    @SerializedName("next_cursor")
    @Expose
    private String nextCursor;

    public List<Resource> getResources() {
        return resources;
    }

//    public void setResources(List<Resource> resources) {
//        this.resources = resources;
//    }

    public String getNextCursor() {
        return nextCursor;
    }

//    public void setNextCursor(String nextCursor) {
//        this.nextCursor = nextCursor;
//    }

}
