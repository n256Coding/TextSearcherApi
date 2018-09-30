package com.n256coding.Interfaces;

import com.n256coding.Models.WebSearchResult;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public interface SearchEngineConnection {
    String[] TUTORIAL_SITES = new String[]{
            "tutorialspoint.com",
            "javatpoint.com",
            "javaworld.com"
    };

    void searchOnline(@Nullable String site, boolean isPdf, String... keywords) throws IOException;

    List<WebSearchResult> getSearchResults();

    List<String> getResultedUrls();

    List<String> getWebCacheUrls();

    String getResultPageAt(int index) throws IOException, BoilerpipeProcessingException, SAXException;

    String getWebCacheAt(int index) throws IOException, BoilerpipeProcessingException;

    String getDescriptionAt(int index);

    int getResultCount();

    boolean navigateToNextPagination() throws IOException;

    int getCurrentPaginationIndex();

    boolean hasMoreResults();

    WebSearchResult nextResult() throws IOException;
}
