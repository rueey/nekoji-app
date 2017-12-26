package com.yruili.animelist.Utils;

import android.graphics.Bitmap;

import com.yruili.animelist.Model.AnimeList;

import java.util.HashMap;

/**
 * Created by rui on 17/07/17.
 *
 * STATIC CLASS TO STORE THE CURRENTLY LOADED ANIMELIST FOR DISPLAY
 *
 */

public class ListContent {
    private static AnimeList list = new AnimeList(); //List of anime
    private static HashMap<Integer, Bitmap> map = new HashMap<>(); //Map of images to be paired with list items
    private static String currentSeason = "";
    private static String currentYear = "";
    private ListContent(){}
    public synchronized static AnimeList getList() {
        return list;
    }
    public synchronized static void setList(AnimeList list) {
        ListContent.list = list;
    }

    public synchronized static String getCurrentSeason() {
        return currentSeason;
    }

    public synchronized static void setCurrentSeason(String currentSeason) {
        ListContent.currentSeason = currentSeason;
    }

    public synchronized static String getCurrentYear() {
        return currentYear;
    }

    public synchronized static void setCurrentYear(String currentYear) {
        ListContent.currentYear = currentYear;
    }
}
