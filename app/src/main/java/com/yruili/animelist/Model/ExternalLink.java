package com.yruili.animelist.Model;

import java.io.Serializable;

/**
 * Created by rui on 16/07/17.
 */
public class ExternalLink implements Serializable{
    private int id;
    private String url;
    private String site;

    public int getId() {
        return id;
    }
    public String getUrl() {
        return url;
    }
    public String getSite() {
        return site;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setSite(String site) {
        this.site = site;
    }
}
