package com.n256coding.DatabaseModels;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "TrustedSites")
public class TrustedSites {
    @org.springframework.data.annotation.Id
    private String Id;

    private String keyword;
    private List<String> synons;
    private List<String> sites;

    public TrustedSites() {
    }

    public TrustedSites(String keyword, List<String> synons, List<String> sites) {
        this.keyword = keyword;
        this.synons = synons;
        this.sites = sites;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<String> getSites() {
        return sites;
    }

    public void setSites(List<String> sites) {
        this.sites = sites;
    }

    public List<String> getSynons() {
        return synons;
    }

    public void setSynons(List<String> synons) {
        this.synons = synons;
    }
}
