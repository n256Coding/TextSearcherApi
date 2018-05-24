package com.n256coding.Actors;

import com.n256coding.Adapters.InsiteSearchResultItemAdapter;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.InsiteSearchResult;
import com.n256coding.Models.WebSearchResult;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

public class TextSearcher {
    public InsiteSearchResult searchContent(String keywords, boolean isPdf) throws ParseException, IOException, ClassNotFoundException {
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        SearchEngineConnection searchEngine = new GoogleConnection();
        DatabaseConnection database = new MongoDbConnection();
        InsiteSearchResult results = new InsiteSearchResult();

        List<String> spellCorrected = textAnalyzer.correctSpellings(keywords);
        results.setSpellCorrectedQuery(String.join(" ", spellCorrected));
        results.setOriginalQuery(keywords);

        searchEngine.searchOnline(isPdf, spellCorrected.toArray(new String[spellCorrected.size()]));

        List<WebSearchResult> searchResults = searchEngine.getSearchResults();
        List<Resource> localResources = database.getTextResourcesByKeywords(spellCorrected.toArray(new String[spellCorrected.size()]));


        for (WebSearchResult searchResult : searchResults) {
            results.addResultItem(new InsiteSearchResultItemAdapter(searchResult));
        }
        for (Resource localResource : localResources) {
            results.addResultItem(new InsiteSearchResultItemAdapter(localResource));
        }

        return results;
    }
}
