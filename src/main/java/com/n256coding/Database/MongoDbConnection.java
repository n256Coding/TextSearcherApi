package com.n256coding.Database;

import com.mongodb.MongoClient;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Interfaces.DatabaseConnection;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoDbConnection implements DatabaseConnection {

    private MongoOperations mongoOperations;

    public MongoDbConnection() {
        mongoOperations = new MongoTemplate(new MongoClient(), "ResourceDB");
    }

    @Override
    public void connectToDatabase() {

    }

    @Override
    public void checkIsDatabaseWorking() {

    }

    @Override
    public void addResource(Resource resource) {
        mongoOperations.insert(resource);
    }

    @Override
    public void ModifyResource(String oldResourceId, Resource newResource) {

    }

    @Override
    public void RemoveResource(String resourceId) {

    }

    @Override
    public List<Resource> getAllTextResources() {
        return mongoOperations.findAll(Resource.class);
    }

    @Override
    public List<Resource> getTextResourcesByKeywords(String... keywords) {
        List<Resource> result = mongoOperations.find(query(where("keywords.word").in(keywords)), Resource.class);
        return result;
    }

    @Override
    public List<Resource> getTextResourcesByUrl(String url) {
        return mongoOperations.find(query(where("url").in(url)), Resource.class);
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


}
