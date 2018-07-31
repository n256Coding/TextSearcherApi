package com.n256coding.Dev.ApiAlgorithms;

import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.DatabaseModels.ResourceRating;
import com.n256coding.Dev.ConsineSimilarityTester;
import com.n256coding.Helpers.DateEx;
import com.n256coding.Helpers.LocalLogger;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Models.InsiteSearchResult;
import com.n256coding.Models.InsiteSearchResultItem;
import com.n256coding.Services.*;
import com.n256coding.Services.Filters.TextFilter;
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Algorithm8 {
    private TextFilter textFilter;
    private NLPProcessor nlpProcessor;
    private OntologyHandler ontologyHandler;
    private TextAnalyzer textAnalyzer;
    private DatabaseConnection database;
    private DateEx date;
    private OnlineDataHandler onlineDataHandler;
    private int dbKeywordMatchCount;
    private Recommender recommender;
    private Logger logger;

    public Algorithm8() {
        textFilter = new TextFilter();
        nlpProcessor = new NLPProcessor();
        ontologyHandler = new OntologyHandler(
                FileHandler.ONTOLOGY_FILE_PATH,
                "http://www.semanticweb.org/nishan/ontologies/2018/5/Programming"
        );
        textAnalyzer = new TextAnalyzer();
        database = new MongoDbConnection(Environments.MONGO_DB_HOSTNAME, Environments.MONGO_DB_PORT);
        date = new DateEx();
        onlineDataHandler = new OnlineDataHandler();
        recommender = new Recommender();
        logger = LocalLogger.getInstance().logger;
    }

    @SuppressWarnings("Duplicates")
    public InsiteSearchResult api(String query, boolean isPdf, String userId) throws IOException {
        System.out.println("found search query: "+query);
        logger.info("found search query: "+query);
        //QUERY ANALYSIS
        //String: Get search query of user
        //String: Replace with lemma
        query = textFilter.replaceWithLemmas(query);
        System.out.println("lemma replaced: "+query);
        logger.info("lemma replaced: "+query);

        //Correct Spellings
        String spellCorrectedQuery = textAnalyzer.correctSpellingsV2(query);
        System.out.println("Spell corrected");
        logger.info("Spell corrected");

        //List: Get nGram of user query and filter out tokens which not important     -> OriginalTokens:List
        List<String> originalTokens;
        originalTokens = textAnalyzer.getNGramOf(query, 1, 3);
        //TODO: Remove this
        originalTokens = textAnalyzer.getLuceneTokenizedList(query);
        System.out.println("query analyzed with NGram");
        System.out.println(originalTokens);
        logger.info("query analyzed with NGram "+originalTokens.toString());

        for (String originalToken : originalTokens) {
            if(nlpProcessor.get(NLPProcessor.WordType.NOUN, originalToken).size() == 0 &&
                    nlpProcessor.get(NLPProcessor.WordType.VERB, originalToken).size() == 0 &&
                    nlpProcessor.get(NLPProcessor.WordType.ADJECTIVE, originalToken).size() == 0){
                originalTokens.remove(originalToken);
            }
        }
        System.out.println("Applied NLP for query");
        logger.info("Applied NLP for query");

        //List: Identify relative keywords          -> RelativeTokens:List
        List<String> relativeTokens = new ArrayList<>();
//        relativeTokens.addAll(textAnalyzer.identifyRelatives(originalTokens.toArray(new String[originalTokens.size()])));
        for (String originalToken : originalTokens) {
            relativeTokens.addAll(ontologyHandler.getSubWordsOf(originalToken, 5));
            relativeTokens.addAll(ontologyHandler.getEquivalentWords(originalToken));
        }
        System.out.println("Identified relative tokens: ");
        System.out.println(relativeTokens);
        logger.info("Identified relative tokens: "+relativeTokens.toString());


        //DATABASE CHECKUP
        //Find resources in database (OriginalTokens, RelativeTokens)
        List<String> allTokens = new ArrayList<>();
        allTokens.addAll(originalTokens);
        allTokens.addAll(relativeTokens);
        System.out.println("All tokens created successfully");
        logger.info("All tokens created successfully");

        //List: Got some results resources from database
//        List<Resource> localResources = database.getResourcesByKeywords(isPdf, allTokens.toArray(new String[allTokens.size()]));
        dbKeywordMatchCount = allTokens.size() > 5 ? 3 : 2;
        List<Resource> localResources = database.getPriorityResourcesByKeywords(
                isPdf,
                dbKeywordMatchCount,
                allTokens.toArray(new String[allTokens.size()]));
        System.out.println("Got results from database for alltoken");
        logger.info("Got results from database for alltoken");

        //If not enough information available in database
//        for(int i=0; localResources.size() < 20 && dbKeywordMatchCount - i > 0; i++){
//            //Reduce number of keyword matches limit
//            localResources = database.getPriorityResourcesByKeywords(isPdf, dbKeywordMatchCount - i, allTokens.toArray(new String[allTokens.size()]));
//            System.out.println("Results not enough, Got database results again with "+(dbKeywordMatchCount - i)+" keywords");
//            logger.info("Results not enough, Got database results again with "+(dbKeywordMatchCount - i)+" keywords");
//        }

        //Filter-out old results
        for (Resource localResource : localResources) {
            if (date.isOlderThanMonths(localResource.getLastModified(), 3)) {
                localResources.remove(localResource);
            }
        }
        System.out.println("Old results filtered");
        logger.info("Old results filtered");

        //is the list have more than 30 results that not older than 6 months?
        if ((isPdf && localResources.size() < 10) || (!isPdf && localResources.size() < 30)) {
            System.out.println("Connecting to internet for refreshing data...");
            logger.info("Connecting to internet for refreshing data...");

            //If local storage does not have much information, request online information
            if(isPdf){
                onlineDataHandler.refreshLocalData_Ebook(query);
            }else{
                onlineDataHandler.refreshLocalData_Webpage(isPdf, query);
            }

            System.out.println("Data collected from internet");
            logger.info("Data collected from internet");

            //Still no considerable amount of results?
            while(localResources.size() < 20 && dbKeywordMatchCount > 0){
                //Reduce number of keyword matches limit
                localResources = database.getPriorityResourcesByKeywords(isPdf, dbKeywordMatchCount--, allTokens.toArray(new String[allTokens.size()]));
                System.out.println("Results not enough, Got database results again with "+dbKeywordMatchCount+" keywords");
                logger.info("Results not enough, Got database results again with "+dbKeywordMatchCount+" keywords");
            }
        }

        //for each result,
//        for (Resource localResource : localResources) {
//            textAnalyzer.getTFIDFWeightOfWords(database.countResources(),
//                    localResources.size(),
//                    localResource,
//                    allTokens.toArray(new String[allTokens.size()]));
//        }

        //calculate tf-idf value
        //TODO: rating algorithm changed
//        Map<String, Double> weightedTfIdf = textAnalyzer.calculateWeightedTfIdf(allTokens, originalTokens, localResources);
        Map<String, Double> weightedTfIdf = new ConsineSimilarityTester().rankResults(allTokens, originalTokens, localResources);
        System.out.println("Results ranked successfully");
        logger.info("Results ranked successfully");


        //Send results to user
        InsiteSearchResult results = new InsiteSearchResult();
        results.setOriginalQuery(query);
        results.setSpellCorrectedQuery(spellCorrectedQuery);

        int resultCount = 0;
        for (Resource localResource : localResources) {
            if (resultCount >= 20) {
                break;
            }
            int rating = 0;
            if (ResourceRating.getRatingOfResourceByUser(localResource.getId(), userId) != null) {
                rating = ResourceRating.getRatingOfResourceByUser(localResource.getId(), userId).getPreference();
            }
            int matchCount = 0;
            for (String token : originalTokens) {
                if((localResource.getTitle() == null ? "" : localResource.getTitle()).matches("\\b"+token+"\\b")){
                    matchCount++;
                }
            }
            if(matchCount > 2){
                results.addResultItem(new InsiteSearchResultItem(
                                localResource.getId(),
                                localResource.getUrl(),
                                localResource.getDescription(),
                                localResource.getImageUrl() == null ? "" : localResource.getImageUrl(),
                                rating,
                                localResource.getTitle() == null ? "" : localResource.getTitle(),
                                weightedTfIdf.get(localResource.getId()) + 2
                        )
                );
            }else{
                results.addResultItem(new InsiteSearchResultItem(
                                localResource.getId(),
                                localResource.getUrl(),
                                localResource.getDescription(),
                                localResource.getImageUrl() == null ? "" : localResource.getImageUrl(),
                                rating,
                                localResource.getTitle() == null ? "" : localResource.getTitle(),
                                weightedTfIdf.get(localResource.getId())
                        )
                );
            }
            resultCount++;
        }
        System.out.println("Second step ranking completed");
        logger.info("Second step ranking completed");

        //Make recommendations for the user
        try {
            results.setRecommendations(recommender.getItemBasedRecommendation(userId));
        } catch (TasteException e) {
            logger.log(Level.SEVERE, "Error in recommendation", e);
        }
        System.out.println("Recommendation completed");
        logger.info("Recommendation completed");


        //Sort results with TF-IDF weights
        results.sort(true);
        System.out.println("Results sorted");
        logger.info("Results sorted");
        logger.info("-----------------------------------------------------------------------");

        //Prepare results to send to the client
        return results;
    }
}
