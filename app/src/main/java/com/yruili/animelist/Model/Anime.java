package com.yruili.animelist.Model;

import java.io.Serializable;
import java.util.*;
public class Anime implements Serializable {
    private int id; //Anilist ID
    private String title_english; //Title of the anime
    private String title_romaji;
    private String title_japanese;
    private int season;
    private int updated_at;
    private int start_date_fuzzy;
    private int end_date_fuzzy;
    private String description; //Summary of anime
    private int total_episodes; //Duration, and number of episodes
    private int duration;
    private String source; //Source (Manga, Light novel etc.)
    private String type; //Movie, TV Short, TV etc.
    private String airing_status; //Currently airing, finished
    private int popularity; //Popularity of anime
    private String image_url_lge;
    private String image_url_med;
    private String image_url_sml;
    private ArrayList<String> genres; //List of genres
    private ArrayList<ExternalLink> external_links; //List of external_links
    private ArrayList<Studio> studio;
    private ArrayList<String> synonyms;
    private Airing airing; //Airing data
    private String youtube_id;
    private boolean leftover;
    private double average_score;
    //Getters
    public int getId() {
        return id;
    }
    public String getTitle_english() {
        return title_english;
    }
    public String getTitle_romaji() {
        return title_romaji;
    }
    public String getTitle_japanese() {
        return title_japanese;
    }
    public int getStart_date_fuzzy() {
        return start_date_fuzzy;
    }
    public int getEnd_date_fuzzy() {
        return end_date_fuzzy;
    }
    public String getDescription(){
        return description;
    }
    public int getTotal_episodes(){
        return total_episodes;
    }
    public int getDuration(){
        return duration;
    }
    public String getSource(){
        return source;
    }
    public ArrayList<Studio> getStudio(){
        return studio;
    }
    public String getType(){
        return type;
    }
    public String getAiring_status(){
        return airing_status;
    }
    public int getPopularity(){
        return popularity;
    }
    public ArrayList<String> getGenres(){
        return genres;
    }
    public ArrayList<ExternalLink> getExternal_links(){
        return external_links;
    }
    public int getSeason() {
        return season;
    }
    public int getUpdated_at() {
        return updated_at;
    }
    public String getImage_url_lge() {
        return image_url_lge;
    }
    public String getImage_url_med() {
        return image_url_med;
    }
    public String getImage_url_sml() {
        return image_url_sml;
    }
    public Airing getAiring() {
        return airing;
    }
    public String getYoutube_id() {
        return youtube_id;
    }
    public boolean isLeftover() {
        return leftover;
    }
    public ArrayList<String> getSynonyms() {
        return synonyms;
    }
    public double getAverage_score() {
        return average_score;
    }

    //Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setStart_date_fuzzy(int start_date_fuzzy) {
        this.start_date_fuzzy = start_date_fuzzy;
    }
    public void setEnd_date_fuzzy(int end_date_fuzzy) {
        this.end_date_fuzzy = end_date_fuzzy;
    }
    public void setDescription(String s){
        description = s;
    }
    public void setTotal_episodes(int s){
        total_episodes = s;
    }
    public void setDuration(int s){
        duration = s;
    }
    public void setSource(String s){
        source = s;
    }
    public void setStudio(ArrayList<Studio> s){
        studio = s;
    }
    public void setType(String s){
        type = s;
    }
    public void setAiring_status(String s){
        airing_status = s;
    }
    public void setPopularity(int s){
        popularity = s;
    }
    public void setGenres(ArrayList<String> s){
        genres = s;
    }
    public void addGenre(String s){
        genres.add(s);
    }
    public void setExternal_links(ArrayList<ExternalLink> s){
        external_links = s;
    }
    public void addLink(ExternalLink s){
        external_links.add(s);
    }
    public void setSeason(int season) {
        this.season = season;
    }
    public void setUpdated_at(int updated_at) {
        this.updated_at = updated_at;
    }
    public void setTitle_english(String title_english) {
        this.title_english = title_english;
    }
    public void setTitle_romaji(String title_romanji) {
        this.title_romaji = title_romanji;
    }
    public void setTitle_japanese(String title_japanese) {
        this.title_japanese = title_japanese;
    }
    public void setImage_url_lge(String image_url_lge) {
        this.image_url_lge = image_url_lge;
    }
    public void setImage_url_med(String image_url_med) {
        this.image_url_med = image_url_med;
    }
    public void setImage_url_sml(String image_url_sml) {
        this.image_url_sml = image_url_sml;
    }
    public void setAiring(Airing airing) {
        this.airing = airing;
    }
    public void setYoutube_id(String youtube_id) {
        this.youtube_id = youtube_id;
    }
    public void setLeftover(boolean leftover) {
        this.leftover = leftover;
    }
    public void setSynonyms(ArrayList<String> synonyms) {
        this.synonyms = synonyms;
    }
    public void setAverage_score(double average_score) {
        this.average_score = average_score;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  Anime){
            Anime a = (Anime)obj;
            return this.id == a.getId();
        }
        return super.equals(obj);
    }
}