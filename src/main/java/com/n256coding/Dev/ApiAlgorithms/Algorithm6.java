package com.n256coding.Dev.ApiAlgorithms;

import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.DatabaseModels.ResourceRating;
import com.n256coding.Helpers.DateEx;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Models.InsiteSearchResult;
import com.n256coding.Models.InsiteSearchResultItem;
import com.n256coding.Services.*;
import com.n256coding.Services.Filters.TextFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Algorithm6 {
    private TextFilter textFilter;
    private NLPProcessor nlpProcessor;
    private OntologyHandler ontologyHandler;
    private TextAnalyzer textAnalyzer;
    private DatabaseConnection database;
    private DateEx date;
    private OnlineDataHandler onlineDataHandler;
    private int dbKeywordMatchCount;

    public Algorithm6() {
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
    }

    public InsiteSearchResult api(String query, boolean isPdf) throws IOException {
        //QUERY ANALYSIS
        //String: Get search query of user
        //String: Replace with lemma
        query = textFilter.replaceWithLemmas(query);

        //Correct Spellings
        String spellCorrectedQuery = textAnalyzer.correctSpellingsV2(query);

        //List: Get nouns, verbs and adjectives     -> OriginalTokens:List
        List<String> originalTokens = new ArrayList<>();
        originalTokens.addAll(nlpProcessor.get(NLPProcessor.WordType.NOUN, query));
        originalTokens.addAll(nlpProcessor.get(NLPProcessor.WordType.VERB, query));
        originalTokens.addAll(nlpProcessor.get(NLPProcessor.WordType.ADJECTIVE, query));

        //List: Identify relative keywords          -> RelativeTokens:List
        List<String> relativeTokens = new ArrayList<>();
        relativeTokens.addAll(textAnalyzer.identifyRelatives(originalTokens.toArray(new String[originalTokens.size()])));
        for (String originalToken : originalTokens) {
            relativeTokens.addAll(ontologyHandler.getSubWordsOf(originalToken, 5));
            relativeTokens.addAll(ontologyHandler.getEquivalentWords(originalToken));
        }


        //DATABASE CHECKUP
        //Find resources in database (OriginalTokens, RelativeTokens)
        List<String> allTokens = new ArrayList<>();
        allTokens.addAll(originalTokens);
        allTokens.addAll(relativeTokens);

        //List: Got some results resources from database
//        List<Resource> localResources = database.getResourcesByKeywords(isPdf, allTokens.toArray(new String[allTokens.size()]));
        dbKeywordMatchCount = allTokens.size() > 5 ? 3 : 2;
        List<Resource> localResources = database.getPriorityResourcesByKeywords(
                isPdf,
                dbKeywordMatchCount,
                allTokens.toArray(new String[allTokens.size()]));

        //Filter-out old results
        for (Resource localResource : localResources) {
            if (date.isOlderThanMonths(localResource.getLastModified(), 3)) {
                localResources.remove(localResource);
            }
        }

        //is the list have more than 30 results that not older than 6 months?
        if ((isPdf && localResources.size() < 10) || (!isPdf && localResources.size() < 30)) {
            //If local storage does not have much information, request online information
            if(isPdf){
                onlineDataHandler.refreshLocalData_Ebook(query);
            }else{
                onlineDataHandler.refreshLocalData_Webpage(isPdf, query);
            }

            //Still no considerable amount of results?
            while(localResources.size() < 20 && dbKeywordMatchCount > 0){
                //Reduce number of keyword matches limit
                localResources = database.getPriorityResourcesByKeywords(isPdf, dbKeywordMatchCount--, allTokens.toArray(new String[allTokens.size()]));
            }
        }

        //for each result,
        for (Resource localResource : localResources) {
            textAnalyzer.getTFIDFWeightOfWords(database.countResources(),
                    localResources.size(),
                    localResource,
                    allTokens.toArray(new String[allTokens.size()]));
        }

        //calculate tf-idf value
        Map<String, Double> weightedTfIdf = textAnalyzer.calculateWeightedTfIdf(allTokens, originalTokens, localResources);

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
            if (ResourceRating.getRatingOfResource(localResource.getId()) != null) {
                rating = ResourceRating.getRatingOfResource(localResource.getId()).getRating();
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
                                rating,
                                localResource.getTitle() == null ? "" : localResource.getTitle(),
                                weightedTfIdf.get(localResource.getId())
                        )
                );
            }
            resultCount++;
        }



        //Sort results with TF-IDF weights
        results.sort();

        //for each result,
        // if title contains any of (OriginalTokens, RelativeTokens)
        //-> Give higher priority to it

        //If there is no considerable amount of results available
        //Search in the internet
        //Gather search results into database

        //Search in database again
        //for each result,
        //calculate tf-idf value
        //for each result,
        // if title contains any of (OriginalTokens, RelativeTokens)
        //-> Give higher priority to it

        //Prepare results to send to the client
        return results;
    }
}
