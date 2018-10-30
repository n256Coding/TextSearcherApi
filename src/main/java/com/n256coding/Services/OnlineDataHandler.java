package com.n256coding.Services;

import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.KeywordData;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Helpers.DateEx;
import com.n256coding.Helpers.StopWordHelper;
import com.n256coding.Interfaces.BookDownloader;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.WebSearchResult;
import com.n256coding.Services.Filters.TextFilter;
import com.n256coding.Services.Filters.UrlFilter;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.jsoup.HttpStatusException;
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
    private TextFilter textFilter;
    private SearchEngineConnection searchEngine;
    private DatabaseConnection database;
    private StopWordHelper stopWordHelper;

    public OnlineDataHandler() {
        this.textFilter = new TextFilter();
        this.searchEngine = new GoogleConnection();
        this.database = new MongoDbConnection();
        this.stopWordHelper = new StopWordHelper();
    }

    @SuppressWarnings("Duplicates")
    public void refreshLocalData_Webpage(boolean isPdf, String query) {
        for (String tutorialSite : UrlFilter.getTutorialSites(query)) {

            try {
                searchEngine.searchOnline(tutorialSite, isPdf, query);
            } catch (HttpStatusException ex) {
                if (ex.getStatusCode() == 503) {
                    System.out.println("Google block detected!");
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
                if (resources.size() == 0 || DateEx.isOlderThanMonths(resources.get(0).getLastModified(), 3)) {
                    //boolean testWord = resources.get(0).getCreated_at().getTime() > new Date().getTime();
                    //Extract text content from URL or the result.
                    //Look for term frequency of that result.

                    List<KeywordData> keywordDataList = new ArrayList<>();
                    Resource resource = new Resource();
                    resource.setUrl(result.getUrl());
                    resource.setPdf(isPdf);
                    resource.setLastModified(new Date());
                    resource.setDescription(result.getDescription());
                    try {
                        resource.setTitle(result.getPageTitle());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
                    int wordCount = 0;
                    try {
                        String tempPage = result.getUrlContent();
                        tempPage = textFilter.replaceWithLemmas(tempPage);
                        wordCount = TextAnalyzer.getWordCount(tempPage);
                        frequencies = TextAnalyzer.getWordFrequency(tempPage);

                        //Skip web pages that not qualify with given conditions
                        if (!textFilter.isValidWebPage(tempPage, frequencies)) {
                            continue;
                        }
                    } catch (HttpStatusException httpErr) {
                        System.out.println("Error status: " + httpErr.getStatusCode());
                        continue;
                    } catch (UnknownHostException | MalformedURLException unkHostErr) {
                        System.out.println("Unknown host at: " + unkHostErr.getMessage());
                        continue;
                    } catch (FileNotFoundException fileNotErr) {
                        System.out.println("File not found: " + fileNotErr.getMessage());
                        continue;
                    } catch (IOException unknownErr) {
                        System.out.println("Unknown Error: " + unknownErr.getMessage());
                    } catch (BoilerpipeProcessingException e) {
                        continue;
                    } catch (SAXException e) {
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
    public void refreshLocalData_Ebook(String query) throws IOException {
        BookDownloader programmingBooks = new ProgrammingBooksDownloader();
        BookDownloader safari = new SafariDownloader(Environments.SAFARI_USERNAME, Environments.SAFARI_PASSWORD);
        ((SafariDownloader) safari).login();
        ((SafariDownloader) safari).setHeaders();

        Thread programmingBooksThread = new Thread(() -> {
            try {
                programmingBooks.searchBooks(query);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Thread safariThread = new Thread(() -> {
            try {
                safari.searchBooks(query);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        programmingBooksThread.start();
        safariThread.start();
        try {
            safariThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            programmingBooksThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println("Starting to analyze URL contents");
        Thread[] analyzerThread = new Thread[programmingBooks.getResultedBookUrls().size() + safari.getResultedBookUrls().size()];

        int i = 0;
        for (; i < safari.getResultedBookUrls().size(); i++) {
            String url = safari.getResultedBookUrls().get(i);
            //If resource is currently available in database, no point of re analysing.
            //So simply skip that operation
            if (Resource.isResourceAvailable(url)) {
                continue;
            }

            analyzerThread[i] = new Thread(() -> analyzeBookUrlContent(url, safari.clone()));
            analyzerThread[i].start();
//            analyzeBookUrlContent(url, safari);
        }

        for (int j = 0; j < programmingBooks.getResultedBookUrls().size(); i++, j++) {
            String url = programmingBooks.getResultedBookUrls().get(j);
            //If resource is currently available in database, no point of re analysing.
            //So simply skip that operation
            if (Resource.isResourceAvailable(url)) {
                continue;
            }

            analyzerThread[i] = new Thread(() -> analyzeBookUrlContent(url, programmingBooks.clone()));
            analyzerThread[i].start();
//            analyzeBookUrlContent(url, programmingBooks);
        }

        for (int j = 0; j < analyzerThread.length; j++) {
            try {
                analyzerThread[j].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }
        }
    }

    public void analyzeBookUrlContent(String bookUrl, BookDownloader bookDownloader) {
        System.out.println("Working on " + bookUrl + "-------------------------------------------------");
        if(bookDownloader instanceof SafariDownloader)
            ((SafariDownloader) bookDownloader).setHeaders();


        List<KeywordData> keywordDataList = new ArrayList<>();
        Resource resource = new Resource();
        resource.setUrl(bookUrl);
        resource.setPdf(true);
        resource.setImageUrl(bookDownloader.getCoverImageUrlOf(bookUrl));
        resource.setTitle(bookDownloader.getTitleOf(bookUrl));
        resource.setLastModified(new Date());
        resource.setDescription(bookDownloader.getDescriptionOf(bookUrl));

        List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
        int wordCount = 0;
        try {
            String tempPage = bookDownloader.getContentOf(bookUrl);
            System.out.println("Downloaded book " + bookUrl);
            wordCount = TextAnalyzer.getWordCount(tempPage);
            System.out.println("Word counted: " + wordCount);
//                tempPage = textFilter.replaceWithLemmas(tempPage);
//                System.out.println("Lemma fixed");
            frequencies = TextAnalyzer.getWordFrequency(tempPage);
            System.out.println("Frequency collected");

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Here the keyword frequencies are reduced with a limit
        //Current limit is 10
        for (int j = 0, k = 0; k < 60 && j < frequencies.size(); j++, k++) {
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
        System.out.println("Frequency reduction completed");
        resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
        //Store or update that information in database.
        database.addResource(resource);
    }


}
