package com.n256coding.DatabaseModels;

import com.n256coding.Common.Environments;
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
    public String userId;
    public String resourceId;
    public int rating;
    public Date lastModified;

    public ResourceRating() {
    }

    @PersistenceConstructor
    public ResourceRating(String id, String userId, String resourceId, int rating, Date lastModified) {
        this.id = id;
        this.userId = userId;
        this.resourceId = resourceId;
        this.rating = rating;
        this.lastModified = lastModified;
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

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public static ResourceRating getRatingOfResource(String resourceId){
        DatabaseConnection database = new MongoDbConnection("localhost", Environments.MONGO_DB_PORT);
        return database.getRatingOfResource(resourceId);
    }
}
