package com.n256coding.Dev.ApiAlgorithms;

import com.google.common.collect.Lists;
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
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Algorithm9 {
    private OntologyHandler ontologyHandler;
    private DatabaseConnection database;
    private OnlineDataHandler onlineDataHandler;
    private int dbKeywordMatchCount;
    private Recommender recommender;
    private Logger logger;

    public Algorithm9() {
        ontologyHandler = new OntologyHandler(FileHandler.ONTOLOGY_FILE_PATH, Environments.ONTOLOGY_BASE_URL);
        database = new MongoDbConnection();
        onlineDataHandler = new OnlineDataHandler();
        recommender = new Recommender();
        logger = LocalLogger.getInstance().logger;
    }

    @SuppressWarnings("Duplicates")
    public InsiteSearchResult api(String query, boolean isPdf, String userId) throws IOException {
        long startTime = new Date().getTime();
        System.out.println("Start time: " + startTime);
        System.out.println("found search query: " + query);
        logger.info("found search query: " + query);
        //QUERY ANALYSIS
        //String: Get search query of user
        //String: Replace with lemma
        query = NLPProcessor.replaceEachWithLemma(query);
        System.out.println("lemma replaced: " + query);
        logger.info("lemma replaced: " + query);

        //Correct Spellings
        String spellCorrectedQuery = TextAnalyzer.correctSpellingsV2(query);
        System.out.println("Spell corrected");
        logger.info("Spell corrected");

        //List: Get nGram of user query and filter out tokens which not important     -> OriginalTokens:List
        List<String> originalTokens;
        originalTokens = TextAnalyzer.getNGramOf(query, 1, 3);
        //TODO: Remove this
//        originalTokens = TextAnalyzer.getLuceneTokenizedList(query);
        System.out.println("query analyzed with NGram");
        System.out.println(originalTokens);
        logger.info("query analyzed with NGram " + originalTokens.toString());

        for (int i = 0; i < originalTokens.size(); i++) {
            String originalToken = originalTokens.get(i);
            if (NLPProcessor.get(NLPProcessor.WordType.NOUN, originalToken).size() == 0 &&
                    NLPProcessor.get(NLPProcessor.WordType.VERB, originalToken).size() == 0 &&
                    NLPProcessor.get(NLPProcessor.WordType.ADJECTIVE, originalToken).size() == 0) {
                originalTokens.remove(originalToken);
            }
        }
        System.out.println("Applied NLP for query");
        logger.info("Applied NLP for query");

        //List: Identify relative keywords          -> RelativeTokens:List
        Set<String> relativeTokens = new HashSet<>();
        relativeTokens.addAll(TextAnalyzer.identifyRelatives(originalTokens.toArray(new String[originalTokens.size()])));
        Set<String> validTokens = new HashSet<>();
        for (String originalToken : Lists.reverse(originalTokens)) {
            relativeTokens.addAll(ontologyHandler.getEquivalentWords(originalToken.replace(" ", "_")));
            if (validTokens.stream().anyMatch(validToken -> validToken.contains(originalToken))) {
                continue;
            }
            List<String> subWords = ontologyHandler.getSubWordsOf(originalToken.replace(" ", "_"), 8);
            if (subWords.size() > 0) {
                validTokens.add(originalToken);
                relativeTokens.addAll(subWords);
            }
        }
        System.out.println("Identified relative tokens: ");
        System.out.println(relativeTokens);
        logger.info("Identified relative tokens: " + relativeTokens.toString());

        originalTokens.removeIf(token -> token.contains(" "));

        //DATABASE CHECKUP
        //Find resources in database (OriginalTokens, RelativeTokens)
        List<String> allTokens = new ArrayList<>();
        allTokens.addAll(originalTokens);
        allTokens.addAll(relativeTokens);
        System.out.println("All tokens created successfully" + allTokens);
        logger.info("All tokens created successfully");

        //List: Got some results resources from database
//        List<Resource> localResources = database.getResourcesByKeywords(isPdf, allTokens.toArray(new String[allTokens.size()]));
        dbKeywordMatchCount = allTokens.size() > 7 ? 5 : 3;
        List<Resource> localResources = database.getPriorityResourcesByKeywords(
                isPdf,
                dbKeywordMatchCount,
                allTokens.toArray(new String[allTokens.size()]));
        System.out.println(localResources.size() + "results got from database for alltoken");
        logger.info(localResources.size() + "results got from database for alltoken");

        //If not enough information available in database
        for (int i = 0; localResources.size() < 20 && dbKeywordMatchCount - i > 0; i++) {
            //Reduce number of keyword matches limit
            localResources = database.getPriorityResourcesByKeywords(isPdf, dbKeywordMatchCount - i, allTokens.toArray(new String[allTokens.size()]));
            System.out.println(localResources.size() + " Results found for " + (dbKeywordMatchCount - i) + " keywords");
            logger.info(localResources.size() + " Results found for " + (dbKeywordMatchCount - i) + " keywords");
        }

        //Filter-out old results
        localResources.removeIf(localResource -> DateEx.isOlderThanMonths(localResource.getLastModified(), 3));
        System.out.println("Old results filtered");
        logger.info("Old results filtered");

        //is the list have more than 30 results that not older than 6 months?
        if ((isPdf && localResources.size() < 10) || (!isPdf && localResources.size() < 30)) {
            System.out.println("Connecting to internet for refreshing data...");
            logger.info("Connecting to internet for refreshing data...");

            //If local storage does not have much information, request online information
            if (isPdf) {
                onlineDataHandler.refreshLocalData_Ebook(query);
            } else {
                onlineDataHandler.refreshLocalData_Webpage(isPdf, query);
            }

            System.out.println("Data collected from internet");
            logger.info("Data collected from internet");

            //Still no considerable amount of results?
            while (localResources.size() < 20 && dbKeywordMatchCount > 0) {
                //Reduce number of keyword matches limit
                localResources = database.getPriorityResourcesByKeywords(isPdf, dbKeywordMatchCount--, allTokens.toArray(new String[allTokens.size()]));
                System.out.println(localResources.size() + " Results found for " + dbKeywordMatchCount + " keywords");
                logger.info(localResources.size() + " Results found for " + dbKeywordMatchCount + " keywords");
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
                if ((localResource.getTitle() == null ? "" : localResource.getTitle()).matches("\\b" + token + "\\b")) {
                    matchCount++;
                }
            }
            if (matchCount > 2) {
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
            } else {
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

        long endTime = new Date().getTime();
        System.out.println("Time taken to show (seconds): " + (endTime - startTime) / 1000);
        //Prepare results to send to the client
        return results;
    }
}
