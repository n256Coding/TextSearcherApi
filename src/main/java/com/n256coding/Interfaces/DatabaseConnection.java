package com.n256coding.Interfaces;

import com.n256coding.DatabaseModels.Resource;

import java.util.List;

public interface DatabaseConnection {
    void connectToDatabase();
    void checkIsDatabaseWorking();
    void addResource(Resource resource);
    void ModifyResource(String oldResourceId, Resource newResource);
    void RemoveResource(String resourceId);
    List<Resource> getAllTextResources();
    List<Resource> getTextResourcesByKeywords(String... keywords);
    List<Resource> getTextResourcesByUrl(String url);
    List<Resource> getPdfResourcesByKeywords(String... keywords);
    List<Resource> getPdfResourcesByUrl(String url);

}
