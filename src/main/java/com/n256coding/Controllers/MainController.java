package com.n256coding.Controllers;

import com.n256coding.Actors.GoogleConnection;
import com.n256coding.Actors.TextAnalyzer;
import com.n256coding.Common.Enviorenments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.KeywordData;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Dev.ApiAlgorithms.Algorithm3;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.InsiteSearchResult;
import com.n256coding.Models.OperationStatus;
import com.n256coding.Models.Rating;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;


@CrossOrigin(origins = Enviorenments.CROSS_ORIGIN)
@RestController
@RequestMapping("/api/resource")
public class MainController {
    private SearchEngineConnection searchEngine = new GoogleConnection();
    private DatabaseConnection db = new MongoDbConnection(Enviorenments.MONGO_DB_HOSTNAME);
    private TextAnalyzer textAnalyzer = new TextAnalyzer();

    @GetMapping("/check")
    public String intialMethod() {
        return "<h1>Its working right now</h1>";
    }

    @PutMapping("/rating")
    public OperationStatus addRating(@RequestBody Rating rating){
        db.upsertResourceRating(rating.getResourceId(), rating.getUserId(), rating.getRating());
        return new OperationStatus("ok");
    }

    @GetMapping
    public InsiteSearchResult searchResults(@RequestParam("q") String query, @RequestParam("pdf") String isPdf) {
//        TextSearcher searcher = new TextSearcher();
//        TextAnalyzer analyzer = new TextAnalyzer();
//
//        List<String> tokenizedQuery = null;
//        List<String> relativeWords = null;
//
//        //Correct spelling if available query
//        try {
//            tokenizedQuery = textAnalyzer.correctSpellings(query);
//        } catch (IOException | ParseException | ClassNotFoundException e) {
//            e.printStackTrace();
//            tokenizedQuery = textAnalyzer.getTokenizedList(query, " ");
//        }
//
//        //Check relative words to query
//        relativeWords = textAnalyzer.identifyRelatives(tokenizedQuery.toArray(new String[tokenizedQuery.size()]));
//
//
//        InsiteSearchResult results = new InsiteSearchResult();
//        try {
//            results = searcher.searchContent(query, isPdf.equals("true"));
//        } catch (ParseException | IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        return results;
        Algorithm3 algorithm = new Algorithm3();
        try {
            return algorithm.api(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new InsiteSearchResult();
    }

    public void recordSearchResults(String keyword, boolean isPdf) throws IOException, BoilerpipeProcessingException {
        searchEngine.searchOnline(null, isPdf, keyword);

        for (int i = 0; i < searchEngine.getResultedUrls().size(); i++) {
            //To avoid overwriting existing data in database
            if (db.getTextResourcesByUrl(searchEngine.getResultedUrls().get(i)).size() != 0) {
                continue;
            }

            List<Map.Entry<String, Integer>> frequency = textAnalyzer.getWordFrequency(searchEngine.getResultPageAt(i));
            KeywordData[] keywordData = new KeywordData[5];

            for (int j = 0; j < frequency.size() && j < 5; j++) {
                keywordData[j] = new KeywordData(frequency.get(j).getKey(), frequency.get(j).getValue(), 0);
            }

//          TODO: Use this method  Resource resource = new Resource(searchEngine.getResultedUrls().get(i), keywordData, isPdf);
            Resource resource = new Resource(searchEngine.getResultedUrls().get(i),
                    keywordData,
                    isPdf,
                    new Date(),
                    searchEngine.getDescriptionAt(i));
            db.addResource(resource);
        }
    }

    public String getSearchResults(String keyword) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Resource> resources = db.getTextResourcesByKeywords(keyword);

        for (Resource resource : resources) {
            stringBuilder.append("<a href='" + resource.getUrl() + "'>" + resource.getUrl() + "</a><br>");
        }
        return stringBuilder.toString();
    }
}
