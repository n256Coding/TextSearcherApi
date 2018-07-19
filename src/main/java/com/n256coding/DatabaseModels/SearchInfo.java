package com.n256coding.DatabaseModels;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "SearchInfo")
public class SearchInfo {
    @Id
    private String id;

    private String userId;
    private String query;
    private Date searched_at;

    @PersistenceConstructor
    public SearchInfo(String id, String userId, String query, Date searched_at) {
        this.id = id;
        this.userId = userId;
        this.query = query;
        this.searched_at = searched_at;
    }

    public SearchInfo(String userId, String query, Date searched_at) {
        this.userId = userId;
        this.query = query;
        this.searched_at = searched_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Date getSearched_at() {
        return searched_at;
    }

    public void setSearched_at(Date searched_at) {
        this.searched_at = searched_at;
    }
}
