package com.n256coding.Interfaces;

import com.n256coding.DatabaseModels.*;

import java.util.Date;
import java.util.List;

public interface DatabaseConnection {
    void connectToDatabase();

    boolean checkIsDatabaseWorking();

    List<TrustedSites> getTutorialSites();

    List<TrustedSites> getTutorialSites(String... keywords);

    long addOrUpdateTutorialSites(TrustedSites trustedSites);

    long deleteTutorialSitesByKeyword(String keyword);

    String addResource(Resource resource);

    void modifyResource(String oldResourceId, Resource newResource);

    void removeResource(String resourceId);

    long countResources();

    List<Resource> getAllResources();

    List<Resource> getAllResources(boolean isPdf);

    List<Resource> getResourcesByKeywords(boolean isPdf, String... keywords);

    List<Resource> getPriorityResourcesByKeywords(boolean isPdf, int numberOfMatches, String... keywords);

    List<Resource> getScoredResourcesByKeywords(boolean isPdf, int numberOfMatches, String... keywords);

    List<Resource> getResourcesWhereTitleContains(boolean isPdf, String... keywords);

    List<Resource> getResourcesByUrl(boolean isPdf, String url);

    List<Resource> getResourcesByUrl(String url);

    Resource getResourceById(String resourceId);

    List<ResourceRating> getAllRatings();

    ResourceRating getRatingOfResource(String resourceId);

    ResourceRating getRatingOfResourceByUser(String resourceId, String userId);

    void upsertResourceRating(String resourceId, String userId, int rating, Date created_at);

    void addUser(User user);

    void updateUserPassword(String userId, String password);

    void removeUser(String userId);

    List<User> getAllUsers();

    void addSubjectsToUser(String userId, String... subjects);

    void removeSubjectsOfUser(String userId, String... subjects);

    void addSearchHistoryOfUser(SearchInfo searchInfo);
}
