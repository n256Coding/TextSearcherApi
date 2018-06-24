package com.n256coding.Actors;

import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.WebSearchResult;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleConnection implements SearchEngineConnection {
    private Document googlePage;
    private List<WebSearchResult> searchResults;
    private boolean isPdf;
    private int resultCursor;
    private int currentPaginationIndex;
    private List<String> paginationUrls;


    public GoogleConnection() {
        currentPaginationIndex = 0;
        resultCursor = -1;
        paginationUrls = new ArrayList<>();
        searchResults = new ArrayList<>();
    }

    @Override
    public void searchOnline(@Nullable String site, boolean isPdf, String... keywords) throws IOException {
        searchResults.clear();
        paginationUrls.clear();
        resultCursor = -1;
        currentPaginationIndex = 0;

        String searchKey = "";
        if (site != null) {
            searchKey = "site:" + site + " ";
        }
        this.isPdf = isPdf;
        if (isPdf) {
            searchKey = "filetype:pdf ";
        }
        for (String keyword : keywords) {
            searchKey = searchKey.concat(keyword).concat(" ");
        }
        googlePage = Jsoup.connect("https://www.google.com/search?q=" + searchKey)
                .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
//                .ignoreHttpErrors(true)
                .timeout(5000)
                .get();
        paginationUrls.add("https://www.google.com/search?q=" + searchKey);

        Elements paginations = googlePage.select("td > a.fl[href]");
        for (Element pagination : paginations) {
            paginationUrls.add("https://www.google.com" + pagination.attr("href"));
        }
        setResults();
    }


    private void setResults() {
        Elements searchResultElements = googlePage.select("div.g");
        for (Element searchResultElement : searchResultElements) {
            Elements urlElements = searchResultElement.select("h3.r > a[href]:not(.sla)");
            Elements cacheUrlElements = searchResultElement.select("li.mUpfKd > a.imx0m");
            Elements descriptionElements = searchResultElement.select("span.st");
            Elements slkTableElements = searchResultElement.select("table.slk");

            if (urlElements.size() == 0) {
                continue;
            }

            String url = urlElements.get(0).attr("href");
            url = UrlFilter.fixUrlEncoding(url);
            url = UrlFilter.extractUrl(url);
            if (!UrlFilter.isValidUrl(url)) {
                continue;
            }

            String webCacheUrl = null;
            for (Element cacheUrlElement : cacheUrlElements) {
                if (cacheUrlElement.ownText().equalsIgnoreCase("cached")) {
                    webCacheUrl = cacheUrlElement.attr("href");
                    webCacheUrl = UrlFilter.fixUrlEncoding(webCacheUrl);
                    webCacheUrl = UrlFilter.extractUrl(webCacheUrl);
                }
            }

            String description = descriptionElements.get(0).ownText();

            //TODO: Newly added part. Needs a review. SEE: Reason
            //Reason: not all urls directs to pdf documents
            if (isPdf && !url.endsWith(".pdf")) {
                continue;
            }
            //TODO: Newly added part. Needs to enhance the filter. SEE: Reason
            //Reason: Some search queries provide non web content like pdf, ppt, doc like things.
            //Needs to understand them and filter out them.
            if (!isPdf && url.endsWith(".pdf")) {
                continue;
            }

            searchResults.add(new WebSearchResult(url, webCacheUrl, description, isPdf));

            if (slkTableElements.size() > 0) {
                Elements slkUrlElements = slkTableElements.get(0).select("h3.r > a.sla");
                Elements slkDescriptions = slkTableElements.get(0).select("div.s.st");

                for (int i = 0; i < slkUrlElements.size(); i++) {
                    String slkUrl = slkUrlElements.get(i).attr("href");
                    slkUrl = UrlFilter.fixUrlEncoding(slkUrl);
                    slkUrl = UrlFilter.extractUrl(slkUrl);
                    if (!UrlFilter.isValidUrl(slkUrl)) {
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
    public String getResultPageAt(int index) throws IOException, BoilerpipeProcessingException, SAXException {
        return searchResults.get(index).getUrlContent();
    }

    @Override
    public String getWebCacheAt(int index) throws IOException, BoilerpipeProcessingException {
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
    public void navigateToNextPagination() throws IOException {
        if ((currentPaginationIndex + 1) >= (paginationUrls.size())) {
            return;
        }
        String paginationUrl = paginationUrls.get(currentPaginationIndex + 1);
        searchResults.clear();
        googlePage = Jsoup.connect(paginationUrl)
                .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
//                .ignoreHttpErrors(true)
                .timeout(5000)
                .get();
        setResults();
        currentPaginationIndex++;
        resultCursor = -1;
    }

    @Override
    public int getCurrentPaginationIndex() {
        return currentPaginationIndex;
    }

    @Override
    public boolean hasMoreResults() {
        //TODO: Change commented code
//        if (((currentPaginationIndex + 1) >= paginationUrls.size()) && ((resultCursor + 1) >= searchResults.size())) {
        if (((currentPaginationIndex + 1) >= 2) && ((resultCursor + 1) >= searchResults.size())) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public WebSearchResult nextResult() throws IOException {
        if (hasMoreResults()) {
            if ((resultCursor + 1) < searchResults.size()) {
                return searchResults.get(++resultCursor);
            } else {
                navigateToNextPagination();
                return searchResults.get(++resultCursor);
            }
        }
        return null;
    }

}
