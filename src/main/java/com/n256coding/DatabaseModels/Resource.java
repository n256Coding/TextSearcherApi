package com.n256coding.DatabaseModels;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nullable;
import java.util.Date;

@Document(collection = "Resource")
public class Resource {

    @Id
    public String id;

    public String url;
    public KeywordData[] keywords;
    public boolean isPdf;
    public Date lastModified;
    public String description;

    public Resource() {
    }

    @PersistenceConstructor
    public Resource(String url, KeywordData[] keywords, boolean isPdf, Date lastModified, String description) {
        this.url = url;
        this.keywords = keywords;
        this.isPdf = isPdf;
        this.lastModified = lastModified;
        this.description = description;
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
}
