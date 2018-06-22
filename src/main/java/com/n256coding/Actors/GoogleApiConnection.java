package com.n256coding.Actors;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.WebSearchResult;
import de.l3s.boilerpipe.BoilerpipeProcessingException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleApiConnection implements SearchEngineConnection {
    private List<WebSearchResult> resultList;
    private int paginationIndex;
    private int resultCursor;
    Customsearch customsearch;
    String searchQuery;
    private final String GOOGLE_API_KEY = "AIzaSyCdvDUeg6kR2tQhSoLXfEVAXiHz_wwHtPc";
    private final String SEARCH_ENGINE_ID = "018198584361994989762:zdl9lg_zqrk";

    public GoogleApiConnection() {
        paginationIndex = 0;
        resultCursor = -1;
    }

    @Override
    public void searchOnline(@Nullable String site, boolean isPdf, String... keywords) throws IOException {
        customsearch = new Customsearch(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            public void initialize(HttpRequest httpRequest) {
                // set connect and read timeouts
                httpRequest.setConnectTimeout(5000);
                httpRequest.setReadTimeout(5000);
            }
        });
        searchQuery = keywords[0];
        Customsearch.Cse.List list = customsearch.cse().list(keywords[0]);
        list.setKey(GOOGLE_API_KEY);
        list.setCx(SEARCH_ENGINE_ID);
        Search results = list.execute();
        addToWebSearchResult(results.getItems());
    }

    private List<WebSearchResult> addToWebSearchResult(List<Result> list) {
        List<WebSearchResult> resultList = new ArrayList<>();
        for (Result result : list) {
            resultList.add(
                    new WebSearchResult(
                            result.getFormattedUrl(),
                            null,
                            result.getSnippet(),
                            false
                    )
            );
        }
        return resultList;
    }

    @Override
    public List<WebSearchResult> getSearchResults() {
        return resultList;
    }

    @Override
    public List<String> getResultedUrls() {
        List<String> urls = new ArrayList<>();
        for (WebSearchResult result : resultList) {
            urls.add(result.getUrl());
        }
        return urls;
    }

    @Override
    public List<String> getWebCacheUrls() {
        return null;
    }

    @Override
    public String getResultPageAt(int index) throws IOException, BoilerpipeProcessingException {
        return resultList.get(index).getUrlContent();
    }

    @Override
    public String getWebCacheAt(int index) throws IOException {
        return null;
    }

    @Override
    public String getDescriptionAt(int index) {
        return resultList.get(index).getDescription();
    }

    @Override
    public int getResultCount() {
        return resultList.size();
    }

    @Override
    public void navigateToNextPagination() throws IOException {
        Customsearch.Cse.List list = customsearch.cse().list(searchQuery);
        list.setKey(GOOGLE_API_KEY);
        list.setCx(SEARCH_ENGINE_ID);
        list.setStart((long) ((++paginationIndex) * 10));
        Search results = list.execute();
        addToWebSearchResult(results.getItems());
        resultCursor = -1;
    }

    @Override
    public int getCurrentPaginationIndex() {
        return paginationIndex;
    }

    @Override
    public boolean hasMoreResults() {
        //TODO: Temporary changes paginationIndex to 2, Use 10 instead in production
        if (resultCursor < 9 && paginationIndex < 2)
            return true;
        else
            return false;
    }

    @Override
    public WebSearchResult nextResult() throws IOException {
        return resultList.get(++resultCursor);
    }
}
