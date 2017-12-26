package com.yruili.animelist.Model;

import java.io.Serializable;

/**
 * Created by rui on 16/07/17.
 */
public class Studio implements Serializable{
    private int id;
    private String studio_name;
    private String studio_wiki;
    private int main_studio;

    public int getId() {
        return id;
    }
    public String getStudio_name() {
        return studio_name;
    }
    public String getStudio_wiki() {
        return studio_wiki;
    }
    public int getMain_studio() {
        return main_studio;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setStudio_name(String studio_name) {
        this.studio_name = studio_name;
    }
    public void setStudio_wiki(String studio_wiki) {
        this.studio_wiki = studio_wiki;
    }
    public void setMain_studio(int main_studio) {
        this.main_studio = main_studio;
    }
}
