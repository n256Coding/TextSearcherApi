package com.n256coding.Database;

import com.mongodb.MongoClient;
import com.n256coding.Common.Environments;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.DatabaseModels.ResourceRating;
import com.n256coding.DatabaseModels.User;
import com.n256coding.Interfaces.DatabaseConnection;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoDbConnection implements DatabaseConnection {

    private MongoOperations mongoOperations;

    public MongoDbConnection() {
        mongoOperations = new MongoTemplate(new MongoClient(), Environments.MONGO_DB_NAME);
    }

    public MongoDbConnection(String hostname, int port) {
        mongoOperations = new MongoTemplate(new MongoClient(hostname, port), Environments.MONGO_DB_NAME);
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
    public String addResource(Resource resource) {
        Query query = new Query(where("url").is(resource.getUrl()));
        Update update = new Update();
        update.set("keywords", resource.getKeywords());
        update.set("lastModified", resource.getLastModified());
        update.set("isPdf", resource.isPdf());
        update.set("url", resource.getUrl());
        update.set("description", resource.getDescription());
        mongoOperations.upsert(query, update, Resource.class);
        return resource.getId();
    }

    @Override
    public void ModifyResource(String oldResourceId, Resource newResource) {

    }

    @Override
    public void removeResource(String resourceId) {
        mongoOperations.remove(query(where("id").in(resourceId)), Resource.class);
    }

    @Override
    public long countResources(){
        Query query = new Query(where("_id").exists(true));
        return mongoOperations.count(query, Resource.class);
    }

    @Override
    public List<Resource> getAllTextResources() {
        return mongoOperations.findAll(Resource.class);
    }

    @Override
    public List<Resource> getTextResourcesByKeywords(String... keywords) {
        //TODO: Could be better to replace with like operator, See other relevant places also
        //TODO: Match elements
//        List<Resource> result = mongoOperations.find(query(where("keywords.word").all(keywords)), Resource.class);
        Query query = new Query();
        query.addCriteria(where("keywords.word")
                .in(keywords).and("isPdf").is(false)
        );
        List<Resource> result = mongoOperations.find(query, Resource.class);
        return result;
    }

    @Override
    public List<Resource> getTextResourcesByUrl(String url) {
        return mongoOperations.find(query(where("url").is(url)), Resource.class);
    }

    @Override
    public Resource getResourceById(String resourceId){
        Query query = new Query(where("_id").is(resourceId));
        return mongoOperations.findOne(query, Resource.class);
    }

    @Override
    public List<Resource> getPdfResourcesByKeywords(String... keywords) {
        Query query = new Query(where("keywords.word").in(keywords).and("isPdf").is(true));
        return mongoOperations.find(query(where("keywords.word").in(keywords).andOperator(where("isPdf").is(true))), Resource.class);
    }

    @Override
    public List<Resource> getPdfResourcesByUrl(String url) {
        return null;
    }

    @Override
    public ResourceRating getRatingOfResource(String resourceId) {
        List<ResourceRating> results = mongoOperations.find(query(where("resourceId").in(resourceId)), ResourceRating.class);
        if (results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void upsertResourceRating(String resourceId, String userId, int rating) {
        Query query = new Query();
        query.addCriteria(where("resourceId").is(resourceId).and("userId").is(userId));
        Update update = new Update();
        update.set("rating", rating);

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
}
