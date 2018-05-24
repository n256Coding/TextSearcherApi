package com.n256coding.Interfaces;

import com.n256coding.Models.WebSearchResult;

import java.io.IOException;
import java.util.List;

public interface SearchEngineConnection {
    void searchOnline(boolean isPdf, String... keywords) throws IOException;
    List<WebSearchResult> getSearchResults();
    List<String> getResultedUrls();
    List<String> getWebCacheUrls();
    String getResultPageAt(int index) throws IOException;
    String getWebCacheAt(int index) throws IOException;
    String getDescriptionAt(int index);
    int getResultCount();
    void navigateToNextResultList();
    int getPaginationIndex();
}
