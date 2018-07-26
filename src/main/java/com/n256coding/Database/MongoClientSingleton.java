package com.n256coding.Database;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.n256coding.Common.Environments;

public class MongoClientSingleton {
    private volatile static MongoClientURI uri = new MongoClientURI(Environments.MONGO_DB_CONNECTION_STRING);
    private volatile static MongoClient mongoClient = new MongoClient(uri);
//    private volatile static MongoClient mongoClient = new MongoClient("localhost", 27017);

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    private MongoClientSingleton() {
    }
}
