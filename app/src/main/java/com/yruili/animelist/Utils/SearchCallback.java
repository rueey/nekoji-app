package com.yruili.animelist.Utils;

/**
 * Created by rui on 23/08/17.
 */

public interface SearchCallback {
    void loadNextBatch(int page); //Load next batch from scroll listener
    int getPage(); //Returns the current page
    boolean getGrid();
    int getSort();
    int getAsc();
}
