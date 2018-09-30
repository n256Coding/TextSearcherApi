package com.n256coding.Database;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.n256coding.Common.Environments;

public class MongoClientSingleton {
    private static volatile MongoClientSingleton ourInstance = new MongoClientSingleton();
//    private MongoClientURI uri = new MongoClientURI(Environments.MONGO_DB_CONNECTION_STRING);
//    private MongoClient mongoClient = new MongoClient(uri);
    private MongoClient mongoClient = new MongoClient("localhost", 27017);

    private MongoClientSingleton() {
    }

    public static MongoClientSingleton getInstance(){
        return ourInstance;
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

}
