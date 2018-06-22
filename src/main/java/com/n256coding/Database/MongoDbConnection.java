package com.n256coding.Database;

import com.mongodb.MongoClient;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.DatabaseModels.ResourceRating;
import com.n256coding.DatabaseModels.User;
import com.n256coding.Interfaces.DatabaseConnection;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoDbConnection implements DatabaseConnection {

    private MongoOperations mongoOperations;

    public MongoDbConnection() {
        mongoOperations = new MongoTemplate(new MongoClient(), "ResourceDB");
    }

    public MongoDbConnection(String hostname) {
        mongoOperations = new MongoTemplate(new MongoClient(hostname), "ResourceDB");
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
        mongoOperations.insert(resource);
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
        List<Resource> result = mongoOperations.find(query(where("keywords.word").in(keywords)), Resource.class);
        return result;
    }

    @Override
    public List<Resource> getTextResourcesByUrl(String url) {
        return mongoOperations.find(query(where("url").in(url)), Resource.class);
    }

    @Override
    public Resource getResourceById(String resourceId){
        Query query = new Query(where("_id").is(resourceId));
        return mongoOperations.findOne(query, Resource.class);
    }

    @Override
    public List<Resource> getPdfResourcesByKeywords(String... keywords) {
        //TODO: Not tested
        return mongoOperations.find(query(where("keywords.word").in(keywords).andOperator(where("isPdf").in("true"))), Resource.class);
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
