package com.n256coding.Actors;

import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.WebSearchResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleConnection implements SearchEngineConnection {
    private Document googlePage;
    private List<WebSearchResult> searchResults;
    private int paginationIndex;

    public GoogleConnection() {
        paginationIndex = 0;
        searchResults = new ArrayList<>();
    }

    @Override
    public void searchOnline(boolean isPdf, String... keywords) throws IOException {
        searchResults.clear();
        String searchKey = "";
        if(isPdf){
            searchKey = "filetype:pdf ";
        }
        for (String keyword : keywords) {
            searchKey = searchKey.concat(keyword).concat(" ");
        }
        googlePage = Jsoup.connect("https://www.google.com/search?q=" + searchKey)
                .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                //TODO: Remove this when fixed .userAgent("Mozilla")
                .timeout(5000)
                .get();

        Elements searchResultElements = googlePage.select("div.g");
        for (Element searchResultElement : searchResultElements) {
            Elements urlElements = searchResultElement.select("h3.r > a[href]:not(.sla)");
            Elements cacheUrlElements = searchResultElement.select("li.mUpfKd > a.imx0m");
            Elements descriptionElements = searchResultElement.select("span.st");
            Elements slkTableElements = searchResultElement.select("table.slk");

            if(urlElements.size() == 0){
                continue;
            }

            String url = urlElements.get(0).attr("href");
            url = UrlFilter.fixUrlEncoding(url);
            url = UrlFilter.extractUrl(url);
            if(!UrlFilter.isValidUrl(url)){
                continue;
            }

            String webCacheUrl = null;
            for (Element cacheUrlElement : cacheUrlElements) {
                if(cacheUrlElement.ownText().equalsIgnoreCase("cached")){
                    webCacheUrl = cacheUrlElement.attr("href");
                    webCacheUrl = UrlFilter.fixUrlEncoding(webCacheUrl);
                    webCacheUrl = UrlFilter.extractUrl(webCacheUrl);
                }
            }

            String description = descriptionElements.get(0).ownText();

            //TODO: Newly added part. Needs a review (Reason: not all urls directs to pdf documents)
            if(isPdf && !url.endsWith(".pdf")){
                continue;
            }
            if(!isPdf && url.endsWith(".pdf")){
                continue;
            }

            searchResults.add(new WebSearchResult(url, webCacheUrl, description, isPdf));

            if(slkTableElements.size() > 0){
                Elements slkUrlElements = slkTableElements.get(0).select("h3.r > a.sla");
                Elements slkDescriptions = slkTableElements.get(0).select("div.s.st");

                for (int i = 0; i < slkUrlElements.size(); i++) {
                    String slkUrl = slkUrlElements.get(i).attr("href");
                    slkUrl = UrlFilter.fixUrlEncoding(slkUrl);
                    slkUrl = UrlFilter.extractUrl(slkUrl);
                    if(!UrlFilter.isValidUrl(slkUrl)){
                        continue;
                    }

                    String slkDescription = slkDescriptions.get(i).ownText();
                    searchResults.add(new WebSearchResult(slkUrl, null, slkDescription, isPdf));
                }
            }
        }
    }

    @Override
    public List<WebSearchResult> getSearchResults() {
        return this.searchResults;
    }


    @Override
    public List<String> getResultedUrls() {
        List<String> urls = new ArrayList<>();
        for (WebSearchResult searchResult : this.searchResults) {
            urls.add(searchResult.getUrl());
        }
        return urls;
    }

    @Override
    public List<String> getWebCacheUrls() {
        List<String> webCacheUrls = new ArrayList<>();
        for (WebSearchResult searchResult : this.searchResults) {
            webCacheUrls.add(searchResult.getWebCacheUrl());
        }
        return webCacheUrls;
    }

    @Override
    public String getResultPageAt(int index) throws IOException {
        return searchResults.get(index).getUrlContent();
    }

    @Override
    public String getWebCacheAt(int index) throws IOException {
        return searchResults.get(index).getWebCacheContent();
    }

    @Override
    public String getDescriptionAt(int index) {
        return searchResults.get(index).getDescription();
    }

    @Override
    public int getResultCount() {
        return this.searchResults.size();
    }

    @Override
    public void navigateToNextResultList() {
        //TODO: Implement functionality to navigate next pagination
    }

    @Override
    public int getPaginationIndex() {
        return paginationIndex;
    }


}
