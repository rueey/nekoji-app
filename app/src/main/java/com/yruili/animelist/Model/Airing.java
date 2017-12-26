package com.yruili.animelist.Model;

import java.io.Serializable;

/**
 * Created by rui on 16/07/17.
 */
public class Airing implements Serializable{
    private String time;
    private long countdown;
    private int next_episode;

    public String getTime() {
        return time;
    }
    public int getNext_episode() {
        return next_episode;
    }
    public long getCountdown() {
        return countdown;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setNext_episode(int next_episode) {
        this.next_episode = next_episode;
    }
    public void setCountdown(long countdown) {
        this.countdown = countdown;
    }
}
