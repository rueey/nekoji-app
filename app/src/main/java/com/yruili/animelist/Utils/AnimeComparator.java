package com.yruili.animelist.Utils;

import com.yruili.animelist.Model.Anime;

import java.util.Comparator;

/**
 * Created by rui on 11/08/17.
 */

public class AnimeComparator implements Comparator<Anime> {

    /**
     * Sorting Types
     * 0 = Popularity
     * 1 = Title
     * 2 = Next Airing Ep
     * 3 = Avg Score
     * 4 = Start Date
     *
     * Sorting Ascending/Descending
     * -1 = Descending
     * 1 = Ascending
     */

    private int type;
    private int ad;
    private final String LOWEST = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
    private final String HIGHEST = "0";

    public AnimeComparator(int type, int ad){
        this.ad = ad;
        this.type = type;
    }
    @Override
    public int compare(Anime o1, Anime o2) {
        if (type == 0) {
            return (o1.getPopularity() - o2.getPopularity()) * ad;
        } else if (type == 1) {
            return o1.getTitle_romaji().compareToIgnoreCase(o2.getTitle_romaji()) * ad;
        } else if (type == 2) {
            String am = ad == -1 ? HIGHEST : LOWEST;
            String o1String = o1.getAiring() == null ? am : o1.getAiring().getTime();
            String o2String = o2.getAiring() == null ? am : o2.getAiring().getTime();
            return o1String.compareToIgnoreCase(o2String)*ad;
        } else if (type == 3) {
            return (int)((o1.getAverage_score()-o2.getAverage_score())*ad);
        } else {
            return (o1.getStart_date_fuzzy() - o2.getStart_date_fuzzy()) * ad;
        }
    }

}
