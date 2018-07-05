package com.n256coding.DatabaseModels;

import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.Interfaces.DatabaseConnection;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "Resource")
public class Resource {

    @Id
    public String id;

    private String url;
    private KeywordData[] keywords;
    private boolean isPdf;
    private Date lastModified;
    private String description;
    private String title;

    public Resource() {
    }

    public Resource(String id, String url, KeywordData[] keywords, boolean isPdf, Date lastModified, String description, String title) {
        this.id = id;
        this.url = url;
        this.keywords = keywords;
        this.isPdf = isPdf;
        this.lastModified = lastModified;
        this.description = description;
        this.title = title;
    }

    @PersistenceConstructor
    public Resource(String url, KeywordData[] keywords, boolean isPdf, Date lastModified, String description, String title) {
        this.url = url;
        this.keywords = keywords;
        this.isPdf = isPdf;
        this.lastModified = lastModified;
        this.description = description;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public KeywordData[] getKeywords() {
        return keywords;
    }

    public void setKeywords(KeywordData[] keywords) {
        this.keywords = keywords;
    }

    public boolean isPdf() {
        return isPdf;
    }

    public void setPdf(boolean pdf) {
        isPdf = pdf;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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

    public double getTfOf(String... words){
        for (KeywordData keyword : keywords) {
            for (String word : words) {
                if(keyword.getWord().contains(word)){
                    return keyword.getTf();
                }
            }
        }
        return 0.0;
    }

    public double getTfOf(String word){
        for (KeywordData keyword : keywords) {
            if(keyword.getWord().equalsIgnoreCase(word.trim())){
                return keyword.getTf();
            }
        }
        return 0.0;
    }

    public static Resource getResourceById(String resourceId){
        DatabaseConnection database = new MongoDbConnection(Environments.MONGO_DB_HOSTNAME, Environments.MONGO_DB_PORT);
        return database.getResourceById(resourceId);
    }
}
