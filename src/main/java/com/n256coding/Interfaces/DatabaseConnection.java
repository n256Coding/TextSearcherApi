package com.n256coding.Interfaces;

import com.n256coding.DatabaseModels.Resource;
import com.n256coding.DatabaseModels.ResourceRating;
import com.n256coding.DatabaseModels.User;

import java.util.List;

public interface DatabaseConnection {
    void connectToDatabase();
    boolean checkIsDatabaseWorking();
    void addResource(Resource resource);
    void ModifyResource(String oldResourceId, Resource newResource);
    void RemoveResource(String resourceId);
    List<Resource> getAllTextResources();
    List<Resource> getTextResourcesByKeywords(String... keywords);
    List<Resource> getTextResourcesByUrl(String url);
    List<Resource> getPdfResourcesByKeywords(String... keywords);
    List<Resource> getPdfResourcesByUrl(String url);
    ResourceRating getRatingOfResource(String resourceId);
    void upsertResourceRating(String resourceId, String userId, int rating);
    void addUser(User user);
    void updateUserPassword(String userId, String password);
    void removeUser(String userId);
    List<User> getAllUsers();
    void addSubjectsToUser(String userId, String... subjects);
    void removeSubjectsOfUser(String userId, String... subjects);

}
