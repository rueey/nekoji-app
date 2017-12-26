package com.yruili.animelist.Utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.yruili.animelist.Model.Anime;

import java.io.Reader;
import java.util.ArrayList;

/**
 * Created by rui on 18/07/17.
 *
 * JSON PARSER CLASS TO READ IN NETWORK RESPONSES AND CONVERT INTO ANIME OBJECTS
 *
 */

public class JSONParse {
    public static final Gson gson = new Gson();

    public static Anime parseJsonForAnimePage(Reader JSON){
        Anime a = gson.fromJson(JSON, Anime.class);
        return a;
    }
    public static ArrayList<Anime> parseJsonForList(Reader JSON){
        try {
            ArrayList<Anime> list = new ArrayList<>();
            JsonElement ele = new JsonParser().parse(JSON);
            JsonArray arr = ele.getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).getAsJsonObject().get("adult").getAsBoolean()) continue;
                Anime a = gson.fromJson(arr.get(i).getAsJsonObject(), Anime.class);
                list.add(a);
            }
            return list;
        } catch (Exception e){
            return null;
        }
    }

    private JSONParse(){}
}
