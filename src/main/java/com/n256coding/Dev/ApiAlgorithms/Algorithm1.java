package com.n256coding.Dev.ApiAlgorithms;

import com.n256coding.Actors.FileHandler;
import com.n256coding.Actors.GoogleConnection;
import com.n256coding.Actors.PDFHandler;
import com.n256coding.Actors.TextAnalyzer;
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
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.jsoup.HttpStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Deprecated
public class Algorithm1 {
    public InsiteSearchResult api(String keywords) throws IOException {
        Trainer trainer = new Trainer();
        DateEx date = new DateEx();
        StopWord stopWord = new StopWord();
        SearchEngineConnection searchEngine = new GoogleConnection();
        DatabaseConnection database = new MongoDbConnection();
        FileHandler fileHandler = new FileHandler();
        PDFHandler pdfHandler = new PDFHandler();
        TextAnalyzer textAnalyzer = new TextAnalyzer();


        //Get user keywords
        //String keywords = "Java Threading";
        boolean isPdf = false;

        //Filter and Identify spell mistakes
        List<String> tokens = textAnalyzer.getTokenizedList(keywords, " ");
        //tokens = textAnalyzer.correctSpellings(tokens.toArray(new String[tokens.size()]));
        String correctedKeywords = textAnalyzer.correctSpellingsV2(keywords);

        //Identify related keywords
        List<String> relatives = textAnalyzer.identifyRelatives(tokens.toArray(new String[tokens.size()]));

        //Search in the web
        List<String> webSearchKeywords = new ArrayList<>();
        webSearchKeywords.addAll(tokens);
        webSearchKeywords.addAll(relatives);

        for (String tutorialSite : SearchEngineConnection.TUTORIAL_SITES) {
            searchEngine.searchOnline(tutorialSite, isPdf, keywords);

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
                    } catch (BoilerpipeProcessingException e) {
                        e.printStackTrace();
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
        results.setOriginalQuery(keywords);
        results.setSpellCorrectedQuery(correctedKeywords);
        for (Resource localResource : localResources) {
            int rating = 0;
            if (ResourceRating.getRatingOfResource(localResource.getId()) != null) {
                rating = ResourceRating.getRatingOfResource(localResource.getId()).getRating();
            }
            results.addResultItem(new InsiteSearchResultItem(
//                    localResource.getId(),
//                    localResource.getUrl(),
//                    localResource.getDescription(),
//                    rating
            ));
        }
        return results;
    }
}
