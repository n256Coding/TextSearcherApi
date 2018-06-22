package com.n256coding.Models;

public class InsiteSearchResultItem {
    protected String _id;
    protected String url;
    protected String description;
    protected int rating;
    protected double tf_idf;

    public InsiteSearchResultItem() {
    }

    public InsiteSearchResultItem(String _id, String url, String description, int rating, double tf_idf) {
        this._id = _id;
        this.url = url;
        this.description = description;
        this.rating = rating;
        this.tf_idf = tf_idf;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public double getTf_idf() {
        return tf_idf;
    }

    public void setTf_idf(double tf_idf) {
        this.tf_idf = tf_idf;
    }
}
