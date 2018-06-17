package com.n256coding.Dev.ApiAlgorithms;

import com.n256coding.Actors.*;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.KeywordData;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.DatabaseModels.ResourceRating;
import com.n256coding.Dev.Trainer;
import com.n256coding.Helpers.DateEx;
import com.n256coding.Helpers.StopWord;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.InsiteSearchResult;
import com.n256coding.Models.InsiteSearchResultItem;
import com.n256coding.Models.WebSearchResult;
import org.jsoup.HttpStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Algorithm2 {
    @SuppressWarnings("Duplicates")
    public InsiteSearchResult api(String query) throws IOException {
        Trainer trainer = new Trainer();
        DateEx date = new DateEx();
        StopWord stopWord = new StopWord();
        SearchEngineConnection searchEngine = new GoogleConnection();
        DatabaseConnection database = new MongoDbConnection();
        FileHandler fileHandler = new FileHandler();
        PDFHandler pdfHandler = new PDFHandler();
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        NLPProcessor nlpProcessor = new NLPProcessor();


        //Get user keywords
        //String keywords = "Java Threading";
        boolean isPdf = false;

        //Filter and Identify spell mistakes
        List<String> tokens = new ArrayList<>();

        String spellCorrectedQuery = textAnalyzer.correctSpellingsV2(query);
        List<String> nouns = nlpProcessor.get(NLPProcessor.WordType.NOUN, spellCorrectedQuery);
        List<String> verbs = nlpProcessor.get(NLPProcessor.WordType.VERB, spellCorrectedQuery);
        List<String> questions = nlpProcessor.get(NLPProcessor.WordType.QUESTION, spellCorrectedQuery);
        List<String> adjectives = nlpProcessor.get(NLPProcessor.WordType.ADJECTIVE, spellCorrectedQuery);
        List<String> lemmas = nlpProcessor.get(NLPProcessor.WordType.LEMMA, spellCorrectedQuery);

        tokens.addAll(nouns);
        tokens.addAll(adjectives);
        for (String lemma : lemmas) {
            if(!tokens.contains(lemma) && !lemma.equalsIgnoreCase("be")){
                tokens.add(lemma);
            }
        }


        //Identify related keywords
        List<String> relatives = textAnalyzer.identifyRelatives(tokens.toArray(new String[tokens.size()]));

        //Search in the web
        List<String> webSearchKeywords = new ArrayList<>();
        webSearchKeywords.addAll(tokens);
        webSearchKeywords.addAll(relatives);

        for (String tutorialSite : SearchEngineConnection.TUTORIAL_SITES) {
            searchEngine.searchOnline(tutorialSite, isPdf, query);

            //Get web search results
            while (searchEngine.hasMoreResults()) {
                WebSearchResult result = searchEngine.nextResult();
                //If search results is not in database or updated date is far than 6 months
                List<Resource> resources = database.getTextResourcesByUrl(result.getUrl());
                if (resources.size() == 0 || date.isOlderThanMonths(resources.get(0).getLastModified(), 6)) {
                    //boolean testWord = resources.get(0).getLastModified().getTime() > new Date().getTime();
                    //Extract text content from URL or the result.
                    //Look for term frequency of that result.
                    //TODO: Needs to handle resources that older more than 6 months in another way. SEE:Reason
                    //Reason: Does not need to add as a new resources but need to update frequencies and modified date

                    List<KeywordData> keywordDataList = new ArrayList<>();
                    Resource resource = new Resource();
                    resource.setUrl(result.getUrl());
                    resource.setPdf(isPdf);
                    resource.setLastModified(new Date());
                    resource.setDescription(result.getDescription());

                    List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
                    try {
                        frequencies = textAnalyzer.getWordFrequency(result.getUrlContent());
                    } catch (HttpStatusException httpErr) {
                        //TODO: Replace with logger
                        System.out.println("Error status: " + httpErr.getStatusCode());
                        continue;
                    } catch (UnknownHostException unkHostErr) {
                        //TODO: Replace with logger
                        System.out.println("Unknown host at: " + unkHostErr.getMessage());
                        continue;
                    } catch (FileNotFoundException fileNotErr) {
                        //TODO: Replace with logger
                        System.out.println("File not found: " + fileNotErr.getMessage());
                        continue;
                    } catch (IOException unknownErr) {
                        //TODO: Replace with logger
                        System.out.println("Invalid File: " + unknownErr.getMessage());
                    }

                    //Here the keyword frequencies are reduced with a limit
                    for (int j = 0, k = 0; k < 20 && j < frequencies.size(); j++, k++) {
                        Map.Entry<String, Integer> frequency = frequencies.get(j);
                        if (stopWord.isStopWord(frequency.getKey())) {
                            k--;
                            continue;
                        }
                        KeywordData keywordData = new KeywordData(frequency.getKey(), frequency.getValue(), 0);
                        keywordDataList.add(keywordData);
                    }
                    resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
                    //Store or update that information in database.
                    database.addResource(resource);
                }
            }

        }


        //Search in the database
        List<Resource> localResources = database.getTextResourcesByKeywords(webSearchKeywords.toArray(new String[webSearchKeywords.size()]));

        //Collect all relevant information from database
        String test = "asdfasdf";

        //Calculate TF-IDF values for each information to rank them.

        //If the selected documents have ranks more than 10, make recommendation

        //Send results to user
        InsiteSearchResult results = new InsiteSearchResult();
        results.setOriginalQuery(query);
        results.setSpellCorrectedQuery(spellCorrectedQuery);
        for (Resource localResource : localResources) {
            int rating = 0;
            if (ResourceRating.getRatingOfResource(localResource.getId()) != null) {
                rating = ResourceRating.getRatingOfResource(localResource.getId()).getRating();
            }
            results.addResultItem(new InsiteSearchResultItem(
                    localResource.getId(),
                    localResource.getUrl(),
                    localResource.getDescription(),
                    rating
            ));
        }
        return results;
    }
}
