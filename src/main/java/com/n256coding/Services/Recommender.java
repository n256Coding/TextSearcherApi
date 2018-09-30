package com.n256coding.Services;

import com.n256coding.Adapters.InsiteSearchResultItemAdapter;
import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.ResourceRating;
import com.n256coding.Dev.MongoDBDataModel;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Models.InsiteSearchResultItem;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.*;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Recommender {
    private MongoDBDataModel model;
    private DatabaseConnection database;

    public Recommender() {
        createDBConnection();
        database = new MongoDbConnection();
    }

    public void recommend() throws IOException, TasteException {
        DataModel dataModel = new FileDataModel(new File("D:\\My Software Projects\\Test Projects\\ZZ ------ Sandbox\\Mahout1\\dataModel.txt"));
//        DataModel dataModel = new FileDataModel(new File("D:\\My Software Projects\\Test Projects\\ZZ ------ Sandbox\\Mahout1\\dataModel_New.txt"));
//        dataModel = new GenericDataModel()
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
        UserNeighborhood userNeighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(2, 5);
        for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
        }
    }

    private InsiteSearchResultItem getResultItem(String itemId){
        return new InsiteSearchResultItemAdapter(database.getResourceById(itemId));
    }

    private List<String> getRatingList(){
        List<String> results = new ArrayList<>();
        List<ResourceRating> allRatings = database.getAllRatings();
        for (ResourceRating rating : allRatings) {
            results.add(model.fromIdToLong(rating.getUser_id(), true)+","+
                model.fromIdToLong(rating.getItem_id(), false)+","+
                rating.getPreference());
        }
        return results;
    }

    public void createDBConnection() {
        model = new MongoDBDataModel("127.0.0.1",
                Environments.MONGO_DB_PORT,
                "ResourceDB",
                "ResourceRating",
                false,
                false,
                null,
                Environments.MONGO_DB_CONNECTION_STRING);
    }

    public void createDBConnection(String username, String password) {
        model = new MongoDBDataModel("127.0.0.1",
                Environments.MONGO_DB_PORT,
                "ResourceDB",
                "ResourceRating",
                false,
                false,
                null,
                username,
                password);
    }

    public List<InsiteSearchResultItem> getUserBasedRecommendation(String userId) throws TasteException, IOException {
        List<InsiteSearchResultItem> results = new ArrayList<>();
        long longUserId = Long.parseLong(model.fromIdToLong(userId, true));
        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
        UserNeighborhood userNeighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
        UserBasedRecommender recommender = new GenericUserBasedRecommender(model, userNeighborhood, similarity);
        List<RecommendedItem> recommendations = recommender.recommend(longUserId, 5);
        for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
            results.add(getResultItem(model.fromLongToId(recommendation.getItemID())));
        }

        return results;
    }

    public List<InsiteSearchResultItem> getItemBasedRecommendation(String userId) throws TasteException {
        List<InsiteSearchResultItem> results = new ArrayList<>();
        ItemSimilarity itemSimilarity = new EuclideanDistanceSimilarity(model);
        GenericItemBasedRecommender itemBasedRecommender = new GenericItemBasedRecommender(model, itemSimilarity);
        String longString = model.fromIdToLong(userId, true);
        long longUserId = Long.parseLong(longString);
        List<RecommendedItem> recommendedItems = itemBasedRecommender.recommend(longUserId, 5);
        for (RecommendedItem recommendedItem : recommendedItems) {
            System.out.println(model.fromLongToId(recommendedItem.getItemID()));
            results.add(getResultItem(model.fromLongToId(recommendedItem.getItemID())));
        }
        return results;
    }

}
