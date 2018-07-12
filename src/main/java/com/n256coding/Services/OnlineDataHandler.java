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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    private void addResourceToDB(String url, String description, String title, boolean isPdf) {

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
    public void refreshLocalData_Ebook(String query) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        FreeEbook[] ebooksResult = restTemplate.getForObject("https://oreilly-api.appspot.com/books", FreeEbook[].class);
        List<String> freeProgrammingBooks = programmingBookComDownload(query);

//        for (FreeEbook ebook : ebooksResult) {
//            WebSearchResult searchResult = new WebSearchResult();
//            if (ebook.getPdf() == null)
//                continue;
//            searchResult.setUrl(ebook.getPdf());
//            searchResult.setDescription(ebook.getDescription());
//            searchResult.setPdf(true);
//
//            List<KeywordData> keywordDataList = new ArrayList<>();
//            Resource resource = new Resource();
//            resource.setUrl(ebook.getPdf());
//            resource.setPdf(true);
//            resource.setTitle(ebook.getTitle());
//            resource.setLastModified(new Date());
//            resource.setDescription(ebook.getDescription());
//
//            List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
//            int wordCount = 0;
//            try {
//                String tempPage = searchResult.getUrlContent();
//                tempPage = textFilter.replaceWithLemmas(tempPage);
//                wordCount = textAnalyzer.getWordCount(tempPage);
//                frequencies = textAnalyzer.getWordFrequency(tempPage);
//
//            } catch (BoilerpipeProcessingException e) {
//                e.printStackTrace();
//            } catch (SAXException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            //Here the keyword frequencies are reduced with a limit
//            //Current limit is 10
//            for (int j = 0, k = 0; k < 20 && j < frequencies.size(); j++, k++) {
//                Map.Entry<String, Integer> frequency = frequencies.get(j);
//                if (stopWordHelper.isStopWord(frequency.getKey())) {
//                    k--;
//                    continue;
//                }
//                KeywordData keywordData = new KeywordData(frequency.getKey(),
//                        frequency.getValue(),
//                        ((double) frequency.getValue() / (double) wordCount));
//                keywordDataList.add(keywordData);
//            }
//            resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
//            //Store or update that information in database.
//            database.addResource(resource);
//        }

        System.out.println("Starting to analyze URL contents");
        Thread[] analyzerThread = new Thread[freeProgrammingBooks.size()];

        for (int i = 0; i < freeProgrammingBooks.size(); i++) {
            final int j = i;
            analyzerThread[i] = new Thread(() -> analyzeUrlContent(freeProgrammingBooks.get(j)));
            analyzerThread[i].start();
        }
        for (int i = 0; i < analyzerThread.length; i++) {
            try {
                analyzerThread[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void analyzeUrlContent(String bookUrl) {
        System.out.println("Working on " + bookUrl + "-------------------------------------------------");
        WebSearchResult searchResult = new WebSearchResult();

        searchResult.setUrl(bookUrl);
        searchResult.setDescription("");
        searchResult.setPdf(true);

        List<KeywordData> keywordDataList = new ArrayList<>();
        Resource resource = new Resource();
        resource.setUrl(bookUrl);
        resource.setPdf(true);
        resource.setTitle("");
        resource.setLastModified(new Date());
        resource.setDescription("");

        List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
        int wordCount = 0;
        try {
            String tempPage = searchResult.getUrlContent();
            System.out.println("Downloaded book " + searchResult.getUrl());
            wordCount = textAnalyzer.getWordCount(tempPage);
            System.out.println("Word counted: " + wordCount);
//                tempPage = textFilter.replaceWithLemmas(tempPage);
//                System.out.println("Lemma fixed");
            frequencies = textAnalyzer.getWordFrequency(tempPage);
            System.out.println("Frequency collected");

        } catch (BoilerpipeProcessingException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
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


    public List<String> programmingBookComDownload(String query) throws IOException {
        List<String> bookUrlList = new ArrayList<>();
        List<String> bookUrlListStep2 = new ArrayList<>();
        List<String> bookUrlListStep3 = new ArrayList<>();
        List<String> paginationUrls = new ArrayList<>();

        Document resultPage = Jsoup.connect("http://www.programming-book.com/?s=" + query)
                .timeout(10000)
                .userAgent("Mozilla")
                .get();

        paginationUrls.add("http://www.programming-book.com/?s=" + query);
        //If true, site contains pagination bar
        if (resultPage.select("div.wp-pagenavi").size() > 0) {

            Element paginationBar = resultPage.select("div.wp-pagenavi").get(0);
            //If class="last" is in the pagination bar
            if (paginationBar.select("a.last").size() > 0) {
                Element lastLink = paginationBar.select("a.last").get(0);
                String lastLinkUrl = lastLink.attr("href");
                int startIndex = lastLinkUrl.lastIndexOf("/page/") + 6;
                int endIndex = lastLinkUrl.lastIndexOf("/?s");
                int lastIndex = Integer.parseInt(lastLinkUrl.substring(startIndex, endIndex));

                for (int i = 2; i <= lastIndex; i++) {
                    paginationUrls.add("http://www.programming-book.com/page/" + i + "/?s=" + query);
                }
            }
        }

        for (int i = 0; i < paginationUrls.size() && i < 2; i++) {
            System.out.println("Working on pagination " + i);
            if (i != 0) {
                resultPage = Jsoup.connect(paginationUrls.get(i))
                        .timeout(10000)
                        .userAgent("Mozilla")
                        .get();
            }
            Elements urls = resultPage.select("a.imghover");
            Elements metaInfo = resultPage.select("div.doc-meta");
            for (int j = 0; j < metaInfo.size(); j++) {
                if (metaInfo.get(j).text().trim().equalsIgnoreCase("Pages 1 |".trim())) {
                    continue;
                }
                String href = urls.get(j).attr("href");
                bookUrlList.add(href);
            }

            System.out.println("Book url list: step 1");
            for (String url : bookUrlList) {
                Document tempDoc = Jsoup.connect(url)
                        .timeout(10000)
                        .userAgent("Mozilla")
                        .get();
                Elements select = tempDoc.select("a#download");
                if (select.size() == 0) {
                    continue;
                }

                String href = select.get(0).attr("href");
                bookUrlListStep2.add(href);
            }

            System.out.println("Book url list: step 2");
            for (String url : bookUrlListStep2) {
                Document tempDoc = Jsoup.connect(url)
                        .timeout(10000)
                        .userAgent("Mozilla")
                        .get();
                Elements script = tempDoc.select("script");
                if (script.size() < 13) {
                    continue;
                }
                String substring = script.get(12).data().substring(
                        script.get(12).data().indexOf("window.location.replace(\"") + 25,
                        script.get(12).data().indexOf(".pdf\")") + 4
                );
                bookUrlListStep3.add(substring);
                System.out.println("Identified: " + substring);
            }
            bookUrlList.clear();
            bookUrlListStep2.clear();
        }


        return bookUrlListStep3;
    }
}
