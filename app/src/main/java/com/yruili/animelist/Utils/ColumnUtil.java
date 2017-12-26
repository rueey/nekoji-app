package com.yruili.animelist.Utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by rui on 17/08/17.
 */

public class ColumnUtil {
    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 136); //120dp + 16 dp for both margins
        return noOfColumns;
    }
}
