package com.n256coding.Database;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
import com.n256coding.Common.Environments;
import com.n256coding.DatabaseModels.*;
import com.n256coding.Interfaces.DatabaseConnection;
import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class MongoDbConnection implements DatabaseConnection {

    private MongoOperations mongoOperations;

    private MongoClient getMongoClient() {
        return MongoClientSingleton.getInstance().getMongoClient();
    }

    public MongoDbConnection() {
        mongoOperations = new MongoTemplate(getMongoClient(), Environments.MONGO_DB_NAME);
    }


    @Override
    public void connectToDatabase() {

    }

    @Override
    public boolean checkIsDatabaseWorking() {
        if (mongoOperations.exists(null, Resource.class) &&
                mongoOperations.exists(null, ResourceRating.class)
                ) {
            return true;
        }
        return false;
    }

    @Override
    public List<TrustedSites> getTutorialSites(){
        return mongoOperations.findAll(TrustedSites.class);
    }

    @Override
    public List<TrustedSites> getTutorialSites(String... keywords){
        Query query = new Query(where("synons").in(keywords));
        return mongoOperations.find(query, TrustedSites.class);
    }

    @Override
    public long addOrUpdateTutorialSites(TrustedSites trustedSites){
        Query query = new Query(where("keyword").is(trustedSites.getKeyword()));
        Update update = new Update();
        update.set("keyword", trustedSites.getKeyword());
        update.set("synons", trustedSites.getSynons());
        update.set("sites", trustedSites.getSites());
        return mongoOperations.upsert(query, update, TrustedSites.class).getModifiedCount();
    }

    @Override
    public long deleteTutorialSitesByKeyword(String keyword){
        Query query = new Query(where("keyword").is(keyword));
        return mongoOperations.remove(query, TrustedSites.class).getDeletedCount();
    }


    @Override
    public String addResource(Resource resource) {
        Query query = new Query(where("url").is(resource.getUrl()));
        Update update = new Update();
        update.set("keywords", resource.getKeywords());
        update.set("lastModified", resource.getLastModified());
        update.set("isPdf", resource.isPdf());
        update.set("url", resource.getUrl());
        update.set("description", resource.getDescription());
        update.set("title", resource.getTitle());
        update.set("imageUrl", resource.getImageUrl());
        mongoOperations.upsert(query, update, Resource.class);
        return resource.getId();
    }

    @Override
    public void modifyResource(String oldResourceId, Resource newResource) {
        Query query = new Query();
        query.addCriteria(where("_id").is(oldResourceId));
        Update update = new Update();
        update.addToSet("url", newResource.getUrl())
                .addToSet("keywords", newResource.getKeywords())
                .addToSet("isPdf", newResource.isPdf())
                .addToSet("lastModified", newResource.getDescription())
                .addToSet("title", newResource.getTitle())
                .addToSet("imageUrl", newResource.getImageUrl());
        mongoOperations.updateFirst(query, update, Resource.class);
    }

    @Override
    public void removeResource(String resourceId) {
        mongoOperations.remove(query(where("id").in(resourceId)), Resource.class);
    }

    @Override
    public long countResources() {
        Query query = new Query(where("_id").exists(true));
        return mongoOperations.count(query, Resource.class);
    }

    @Override
    public List<Resource> getAllResources() {
        return mongoOperations.findAll(Resource.class);
    }

    @Override
    public List<Resource> getAllResources(boolean isPdf) {
        Query query = new Query(where("isPdf").is(isPdf));
        return mongoOperations.find(query, Resource.class);
    }

    @Override
    public List<Resource> getResourcesByKeywords(boolean isPdf, String... keywords) {
        Query query = new Query();
        query.addCriteria(where("keywords.word")
                .in(keywords).and("isPdf").is(isPdf)
        );
        return mongoOperations.find(query, Resource.class);
    }

    @Override
    public List<Resource> getPriorityResourcesByKeywords(boolean isPdf, int numberOfMatches, String... keywords) {
        BasicDBList intersectionList = new BasicDBList();
        intersectionList.add("$keywords.word");
        intersectionList.add(keywords);

        BasicDBList matchList = new BasicDBList();
        matchList.add(new BasicDBObject("matchedTags",
                new BasicDBObject("$gte", numberOfMatches)));
        matchList.add(new BasicDBObject("isPdf", isPdf));

        AggregateIterable<Document> aggregate = null;
        aggregate = getMongoClient().getDatabase("ResourceDB").getCollection("Resource").aggregate(
                Arrays.asList(
                        new BasicDBObject("$addFields",
                                new BasicDBObject("matchedTags",
                                        new BasicDBObject("$size",
                                                new BasicDBObject("$setIntersection", intersectionList)))),
                        new BasicDBObject("$match",
                                new BasicDBObject("$and", matchList)),
                        new BasicDBObject("$sort",
                                new BasicDBObject("matchedTags", -1))
                )
        );

        List<Resource> resources = new ArrayList<>();
        MongoCursor<Document> iterator = aggregate.iterator();

        while (iterator.hasNext()) {
            Document document = iterator.next();
            ArrayList keywordList = document.get("keywords", ArrayList.class);
            List<KeywordData> keywordData = new ArrayList<>();
            for (Document data : (ArrayList<Document>) keywordList) {
                keywordData.add(new KeywordData(
                        data.getString("word"),
                        data.getInteger("freq"),
                        data.getDouble("tf")
                ));
            }
            resources.add(new Resource(
                    document.getObjectId("_id").toString(),
                    document.getString("url"),
                    keywordData.toArray(new KeywordData[keywordData.size()]),
                    document.getBoolean("isPdf"),
                    document.getDate("lastModified"),
                    document.getString("description"),
                    document.getString("title"),
                    document.getString("imageUrl")
            ));
        }
        return resources;
    }

    @Override
    public List<Resource> getScoredResourcesByKeywords(boolean isPdf, int numberOfMatches, String... keywords){
        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(keywords);
        Query query = TextQuery
                .queryText(criteria)
                .sortByScore();
        query.addCriteria(where("isPdf").is(isPdf));

        return mongoOperations.find(query, Resource.class);
    }

    @Override
    public List<Resource> getResourcesWhereTitleContains(boolean isPdf, String... keywords) {
        List<Resource> results = new ArrayList<>();
        Query query;
        for (String keyword : keywords) {
            query = new Query(where("isPdf").is(isPdf).and("title").regex(keyword));
            results.addAll(mongoOperations.find(query, Resource.class));
        }
        return results;
    }

    @Override
    public List<Resource> getResourcesByUrl(boolean isPdf, String url) {
        Query query = new Query(where("isPdf").is(isPdf).and("url").is(url));
        return mongoOperations.find(query, Resource.class);
    }

    @Override
    public List<Resource> getResourcesByUrl(String url) {
        Query query = new Query(where("url").is(url));
        return mongoOperations.find(query, Resource.class);
    }

    @Override
    public Resource getResourceById(String resourceId) {
        Query query = new Query(where("_id").is(resourceId));
        return mongoOperations.findOne(query, Resource.class);
    }

    @Override
    public List<ResourceRating> getAllRatings() {
        return mongoOperations.findAll(ResourceRating.class);
    }

    @Override
    public ResourceRating getRatingOfResource(String resourceId) {
        List<ResourceRating> results = mongoOperations.find(query(where("item_id").in(resourceId)), ResourceRating.class);
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    public ResourceRating getRatingOfResourceByUser(String resourceId, String userId) {
        Query query = new Query(where("item_id").is(resourceId).and("user_id").is(userId));
        List<ResourceRating> results = mongoOperations.find(query, ResourceRating.class);
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void upsertResourceRating(String item_id, String user_id, int preference, Date created_at) {
        Query query = new Query();
        query.addCriteria(where("item_id").is(item_id).and("user_id").is(user_id));
        Update update = new Update();
        update.set("preference", preference);
        update.set("created_at", created_at);

        mongoOperations.upsert(query, update, ResourceRating.class);
    }


    @Override
    public void addUser(User user) {
        mongoOperations.insert(user);
    }

    @Override
    public void updateUserPassword(String userId, String password) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.set("password", password);

        mongoOperations.updateFirst(query, update, User.class);
    }

    @Override
    public void removeUser(String userId) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));

        mongoOperations.remove(query, User.class);
    }

    @Override
    public List<User> getAllUsers() {
        return mongoOperations.findAll(User.class);
    }

    @Override
    public void addSubjectsToUser(String userId, String... subjects) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.pushAll("subjects", subjects);

        mongoOperations.updateFirst(query, update, User.class);
    }

    @Override
    public void removeSubjectsOfUser(String userId, String... subjects) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.pullAll("subjects", subjects);

        mongoOperations.updateFirst(query, update, User.class);
    }

    @Override
    public void addSearchHistoryOfUser(SearchInfo searchInfo) {
        mongoOperations.insert(searchInfo);
    }
}
