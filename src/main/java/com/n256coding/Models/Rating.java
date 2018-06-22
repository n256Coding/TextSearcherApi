package com.n256coding.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Rating {
    private String resourceId;
    private String userId;
    private int rating;

    @JsonCreator
    public Rating(@JsonProperty("resourceId") String resourceId, @JsonProperty("userId") String userId, @JsonProperty("rating") int rating) {
        this.resourceId = resourceId;
        this.userId = userId;
        this.rating = rating;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
