package com.n256coding.DatabaseModels;

import com.n256coding.Database.MongoDbConnection;
import com.n256coding.Interfaces.DatabaseConnection;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "ResourceRating")
public class ResourceRating {
    @Id
    public String id;
    public String user_id;
    public String item_id;
    public int preference;
    public Date created_at;

    public ResourceRating() {
    }

    @PersistenceConstructor
    public ResourceRating(String id, String user_id, String item_id, int preference, Date created_at) {
        this.id = id;
        this.user_id = user_id;
        this.item_id = item_id;
        this.preference = preference;
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
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

    public static ResourceRating getRatingOfResource(String resourceId) {
        DatabaseConnection database = new MongoDbConnection();
        return database.getRatingOfResource(resourceId);
    }

    public static ResourceRating getRatingOfResourceByUser(String resourceId, String userId) {
        DatabaseConnection database = new MongoDbConnection();
        return database.getRatingOfResourceByUser(resourceId, userId);
    }
}
