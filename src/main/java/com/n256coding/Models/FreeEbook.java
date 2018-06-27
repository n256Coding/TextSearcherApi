package com.n256coding.Models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FreeEbook {
    private String category;
    private String mobi;
    private String description;
    private String title;
    private String url;
    private String pdf;
    private String thumbnail;
    private String epub;
    private String subcategory;

    @JsonCreator
    public FreeEbook(@JsonProperty("category") String category,
                     @JsonProperty("mobi") String mobi,
                     @JsonProperty("description") String description,
                     @JsonProperty("title") String title,
                     @JsonProperty("url") String url,
                     @JsonProperty("pdf") String pdf,
                     @JsonProperty("thumbnail") String thumbnail,
                     @JsonProperty("epub") String epub,
                     @JsonProperty("subcategory") String subcategory) {
        this.category = category;
        this.mobi = mobi;
        this.description = description;
        this.title = title;
        this.url = url;
        this.pdf = pdf;
        this.thumbnail = thumbnail;
        this.epub = epub;
        this.subcategory = subcategory;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMobi() {
        return mobi;
    }

    public void setMobi(String mobi) {
        this.mobi = mobi;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPdf() {
        if(pdf == null){
            return null;
        }
        return "https://www.oreilly.com/programming/free/files/"+pdf.substring(pdf.lastIndexOf('/')+1);
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getEpub() {
        return epub;
    }

    public void setEpub(String epub) {
        this.epub = epub;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }
}
