package com.n256coding.DatabaseModels;

import com.n256coding.Database.MongoDbConnection;
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
    private String imageUrl;

    public Resource() {
    }

    public Resource(String id, String url, KeywordData[] keywords, boolean isPdf, Date lastModified, String description, String title, String imageUrl) {
        this.id = id;
        this.url = url;
        this.keywords = keywords;
        this.isPdf = isPdf;
        this.lastModified = lastModified;
        this.description = description;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    @PersistenceConstructor
    public Resource(String url, KeywordData[] keywords, boolean isPdf, Date lastModified, String description, String title, String imageUrl) {
        this.url = url;
        this.keywords = keywords;
        this.isPdf = isPdf;
        this.lastModified = lastModified;
        this.description = description;
        this.title = title;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getTfOf(String... words) {
        for (KeywordData keyword : keywords) {
            for (String word : words) {
                if (keyword.getWord().contains(word)) {
                    return keyword.getTf();
                }
            }
        }
        return 0.0;
    }

    public double getTfOf(String word) {
        for (KeywordData keyword : keywords) {
            if (keyword.getWord().equalsIgnoreCase(word.trim())) {
                return keyword.getTf();
            }
        }
        return 0.0;
    }

    public static Resource getResourceById(String resourceId) {
        return new MongoDbConnection().getResourceById(resourceId);
    }

    public static boolean isResourceAvailable(String url) {
        return new MongoDbConnection().getResourcesByUrl(url).size() > 0;
    }
}
