package com.yruili.animelist.Utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by rui on 20/07/17.
 */

public class SeasonUtil {
    private static String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private static String[] x = {"Winter", "Spring", "Summer", "Fall"};
    private static ArrayList<String> seasons = new ArrayList<>(Arrays.asList(x));
    private SeasonUtil(){}
    public static String checkMonth(int month){
        String result;
        if(month == 11 || month < 2){
            result = "Winter";
        } else if(month >= 2 && month <= 4){
            result = "Spring";
        } else if(month >= 5 && month <= 7){
            result = "Summer";
        } else {
            result = "Fall";
        }
        return result;
    }
    public static String[] prevSeason(String season, String year){
        int x = Integer.parseInt(year);
        String[] result = new String[2];
        int idx = seasons.indexOf(season);
        if(idx == 0){
            idx = seasons.size()-1;
            x--;
        } else {
            idx--;
        }
        result[0] = seasons.get(idx);
        result[1] = String.valueOf(x);
        return result;
    }
    public static String[] nextSeason(String season, String year){
        int x = Integer.parseInt(year);
        String[] result = new String[2];
        int idx = seasons.indexOf(season);
        if(idx == seasons.size()-1){
            idx = 0;
            x++;
        } else {
            idx++;
        }
        result[0] = seasons.get(idx);
        result[1] = String.valueOf(x);
        return result;
    }
    public static String getSubtitle(String season){
        if (season.toLowerCase().equals("winter")) {
            return "December - February";
        } else if(season.toLowerCase().equals("spring")){
            return "March - May";
        } else if(season.toLowerCase().equals("summer")){
            return "June - August";
        } else {
            return "September - November";
        }
    }
    public static String getMonth(int id){
        if(id-1 < 12) {
            return months[(id-1)];
        } else {
            return "error";
        }
    }
    public static String getSeasonText(int idx){
        return x[(idx-1)];
    }
}
