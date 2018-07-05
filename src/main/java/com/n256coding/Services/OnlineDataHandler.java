package com.n256coding.Services;

import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.KeywordData;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Helpers.DateEx;
import com.n256coding.Helpers.StopWordHelper;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.FreeEbook;
import com.n256coding.Models.WebSearchResult;
import com.n256coding.Services.Filters.TextFilter;
import com.n256coding.Services.Filters.UrlFilter;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.jsoup.HttpStatusException;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OnlineDataHandler {
    private UrlFilter urlFilter;
    private TextFilter textFilter;
    private SearchEngineConnection searchEngine;
    private DatabaseConnection database;
    private DateEx date;
    private TextAnalyzer textAnalyzer;
    private StopWordHelper stopWordHelper;

    public OnlineDataHandler() {
        this.urlFilter = new UrlFilter();
        this.textFilter = new TextFilter();
        this.searchEngine = new GoogleConnection();
        this.database = new MongoDbConnection(Environments.MONGO_DB_HOSTNAME, Environments.MONGO_DB_PORT);
        this.date = new DateEx();
        this.textAnalyzer = new TextAnalyzer();
        this.stopWordHelper = new StopWordHelper();
    }

    @SuppressWarnings("Duplicates")
    public void refreshLocalData_Webpage(boolean isPdf, String query) {
        for (String tutorialSite : urlFilter.getTutorialSites(query)) {

            try {
                searchEngine.searchOnline(tutorialSite, isPdf, query);
            } catch (HttpStatusException ex) {
                if (ex.getStatusCode() == 503) {
                    //TODO: Replace with logger
                    System.out.println("Google block detected!");
                    try {
                        Thread.sleep(5000);
                        searchEngine.searchOnline(tutorialSite, isPdf, query);
                    } catch (InterruptedException e) {
                        //TODO: Replace with logger
                        e.printStackTrace();
                    } catch (HttpStatusException e) {
                        //TODO: Place info in a logger
                        System.out.println("Google block in second time");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Get web search results
            while (searchEngine.hasMoreResults()) {
                WebSearchResult result = null;
                try {
                    result = searchEngine.nextResult();
                    if (result == null)
                        break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //If selected resource (URL) is not in database or updated date is older than 3 months
                //Add or update resource information
                List<Resource> resources = database.getResourcesByUrl(isPdf, result.getUrl());
                if (resources.size() == 0 || date.isOlderThanMonths(resources.get(0).getLastModified(), 3)) {
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
                    try {
                        resource.setTitle(result.getPageTitle());
                    } catch (IOException e) {
                        //TODO: Replace with logger
                        e.printStackTrace();
                    }

                    List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
                    int wordCount = 0;
                    try {
                        String tempPage = result.getUrlContent();
                        tempPage = textFilter.replaceWithLemmas(tempPage);
                        wordCount = textAnalyzer.getWordCount(tempPage);
                        frequencies = textAnalyzer.getWordFrequency(tempPage);

                        //Skip web pages that not qualify with given conditions
                        if (!textFilter.isValidWebPage(tempPage, frequencies)) {
                            continue;
                        }
                    } catch (HttpStatusException httpErr) {
                        //TODO: Replace with logger
                        System.out.println("Error status: " + httpErr.getStatusCode());
                        continue;
                    } catch (UnknownHostException | MalformedURLException unkHostErr) {
                        //TODO: Replace with logger
                        System.out.println("Unknown host at: " + unkHostErr.getMessage());
                        continue;
                    } catch (FileNotFoundException fileNotErr) {
                        //TODO: Replace with logger
                        System.out.println("File not found: " + fileNotErr.getMessage());
                        continue;
                    } catch (IOException unknownErr) {
                        //TODO: Replace with logger
                        System.out.println("Unknown Error: " + unknownErr.getMessage());
                    } catch (BoilerpipeProcessingException e) {
                        //TODO: Replace with logger
                        continue;
                    } catch (SAXException e) {
                        //TODO: Replace with logger
                        e.printStackTrace();
                    }

                    //Here the keyword frequencies are reduced with a limit
                    //Current limit is 10
                    for (int j = 0, k = 0; k < 20 && j < frequencies.size(); j++, k++) {
                        Map.Entry<String, Integer> frequency = frequencies.get(j);

                        //Condition not required because lucene checks stopwords when tokenizing
//                        if (stopWordHelper.isStopWord(frequency.getKey())) {
//                            k--;
//                            continue;
//                        }

                        KeywordData keywordData = new KeywordData(frequency.getKey(),
                                frequency.getValue(),
                                ((double) frequency.getValue() / (double) wordCount));
                        keywordDataList.add(keywordData);
                    }
                    resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
                    //Store or update that information in database.
                    database.addResource(resource);
                }
            }

        }
    }

    @SuppressWarnings("Duplicates")
    public void refreshLocalData_Ebook() {
        RestTemplate restTemplate = new RestTemplate();
        FreeEbook[] ebooksResult = restTemplate.getForObject("https://oreilly-api.appspot.com/books", FreeEbook[].class);

        for (FreeEbook ebook : ebooksResult) {
            WebSearchResult searchResult = new WebSearchResult();
            if (ebook.getPdf() == null)
                continue;
            searchResult.setUrl(ebook.getPdf());
            searchResult.setDescription(ebook.getDescription());
            searchResult.setPdf(true);

            List<KeywordData> keywordDataList = new ArrayList<>();
            Resource resource = new Resource();
            resource.setUrl(ebook.getPdf());
            resource.setPdf(true);
            resource.setTitle(ebook.getTitle());
            resource.setLastModified(new Date());
            resource.setDescription(ebook.getDescription());

            List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
            int wordCount = 0;
            try {
                String tempPage = searchResult.getUrlContent();
                tempPage = textFilter.replaceWithLemmas(tempPage);
                wordCount = textAnalyzer.getWordCount(tempPage);
                frequencies = textAnalyzer.getWordFrequency(tempPage);

            } catch (BoilerpipeProcessingException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Here the keyword frequencies are reduced with a limit
            //Current limit is 10
            for (int j = 0, k = 0; k < 20 && j < frequencies.size(); j++, k++) {
                Map.Entry<String, Integer> frequency = frequencies.get(j);
                if (stopWordHelper.isStopWord(frequency.getKey())) {
                    k--;
                    continue;
                }
                KeywordData keywordData = new KeywordData(frequency.getKey(),
                        frequency.getValue(),
                        ((double) frequency.getValue() / (double) wordCount));
                keywordDataList.add(keywordData);
            }
            resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
            //Store or update that information in database.
            database.addResource(resource);
        }
    }
}
