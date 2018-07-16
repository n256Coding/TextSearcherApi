package com.n256coding.Services;

import com.n256coding.Common.Environments;
import com.n256coding.Dev.MongoDBDataModel;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public class Recommender {

    public void recommend() throws IOException, TasteException {
        DataModel dataModel = new FileDataModel(new File("D:\\My Software Projects\\Test Projects\\ZZ ------ Sandbox\\Mahout1\\dataModel_large.txt"));
//        dataModel = new GenericDataModel()
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
        UserNeighborhood userNeighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(2, 5);
        for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
        }
    }

    public void testAuthRecommendation(String userId) throws UnknownHostException, TasteException {
        MongoDBDataModel mongoDBDataModel = new MongoDBDataModel(Environments.MONGO_DB_HOSTNAME,
                Environments.MONGO_DB_PORT,
                "ResourceDB",
                "ResourceRating",
                false,
                false,
                null,
                Environments.MONGO_DB_USERNAME,
                Environments.MONGO_DB_PASSWORD);
        //TODO: Replace with logger
        System.out.println("Logger connected to Mongo DB");
        ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(mongoDBDataModel);
        GenericItemBasedRecommender itemBasedRecommender = new GenericItemBasedRecommender(mongoDBDataModel, itemSimilarity);
        String longString = mongoDBDataModel.fromIdToLong(userId, true);
        long longUserId = Long.parseLong(longString);
        List<RecommendedItem> recommendedItems = itemBasedRecommender.recommend(longUserId, 3);
        for (RecommendedItem recommendedItem : recommendedItems) {
            System.out.println(mongoDBDataModel.fromLongToId(recommendedItem.getItemID()));
        }
//        MongoDBModel
    }

    public void testRecommendation(String userId) throws UnknownHostException, TasteException {
        MongoDBDataModel mongoDBDataModel = new MongoDBDataModel("127.0.0.1",
                Environments.MONGO_DB_PORT,
                "ResourceDB",
                "ResourceRating",
                false,
                false,
                null);

        //TODO: Replace with logger
        System.out.println("Logger connected to Mongo DB");

        ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(mongoDBDataModel);
        GenericItemBasedRecommender itemBasedRecommender = new GenericItemBasedRecommender(mongoDBDataModel, itemSimilarity);
        String longString = mongoDBDataModel.fromIdToLong(userId, true);
        long longUserId = Long.parseLong(longString);
        List<RecommendedItem> recommendedItems = itemBasedRecommender.recommend(longUserId, 1);
        for (RecommendedItem recommendedItem : recommendedItems) {
            System.out.println(mongoDBDataModel.fromLongToId(recommendedItem.getItemID()));
        }
    }
}
