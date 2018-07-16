package com.n256coding.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Rating {
    private String item_id;
    private String user_id;
    private int preference;
    private Date created_at;

    @JsonCreator
    public Rating(@JsonProperty("item_id") String item_id, @JsonProperty("user_id") String user_id, @JsonProperty("preference") int preference, @JsonProperty("created_at") Date created_at) {
        this.item_id = item_id;
        this.user_id = user_id;
        this.preference = preference;
        this.created_at = created_at;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getPreference() {
        return preference;
    }

    public void setPreference(int preference) {
        this.preference = preference;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
