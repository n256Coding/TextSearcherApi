package com.n256coding.Common;


public interface Environments {
    String MONGO_DB_HOSTNAME = "localhost";
    int MONGO_DB_PORT = 27017;
    String MONGO_DB_NAME = "ResourceDB";
    String MONGO_DB_USERNAME = "";
    String MONGO_DB_PASSWORD = "";
    String MONGO_DB_CONNECTION_STRING = "mongodb://text:sWarg37cqC5tRzex@lmmsproject-shard-00-00-r8wzc.mongodb.net:27017,lmmsproject-shard-00-01-r8wzc.mongodb.net:27017,lmmsproject-shard-00-02-r8wzc.mongodb.net:27017/test?ssl=true&replicaSet=LMMSProject-shard-0&authSource=admin&retryWrites=true";
//    String CROSS_ORIGIN ="http://34.217.10.5:8085";
    String CROSS_ORIGIN ="*";

    //Google custom search api credentials for text search component
    String GOOGLE_API_KEY = "AIzaSyBGOnB-CFP06uJ1deOV0A2IDmuvY7k5u9k";
    String SEARCH_ENGINE_ID = "000091703355649038529:airvt_vszyo";
}
