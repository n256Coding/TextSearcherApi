package com.n256coding.Controllers;

import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.SearchInfo;
import com.n256coding.Dev.ApiAlgorithms.Algorithm7;
import com.n256coding.Dev.ApiAlgorithms.Algorithm8;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.InsiteSearchResult;
import com.n256coding.Models.OperationStatus;
import com.n256coding.Models.Rating;
import com.n256coding.Services.GoogleConnection;
import com.n256coding.Services.TextAnalyzer;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.Random;


@CrossOrigin(origins = Environments.CROSS_ORIGIN)
@RestController
@RequestMapping("/api/resource")
public class MainController {
    private DatabaseConnection db = new MongoDbConnection(Environments.MONGO_DB_HOSTNAME, Environments.MONGO_DB_PORT);

    @GetMapping("/check")
    public String intialMethod() {
        Random rand = new Random(5);
        int randomValue = rand.nextInt();
        String message = "";
        switch (randomValue) {
            case 0:
                message = "Whats up?";
                break;
            case 1:
                message = "Are you happy now?";
                break;
            case 2:
                message = "Do not disturb!";
                break;
            case 3:
                message = "Connection established";
                break;
            default:
                message = "Awesome!";
        }
        return "<h1>" + message + "</h1>";
    }

    @PutMapping("/rating")
    public OperationStatus addRating(@RequestBody Rating rating) {
        db.upsertResourceRating(rating.getItem_id(), rating.getUser_id(), rating.getPreference(), new Date());
        return new OperationStatus("ok");
    }

    @GetMapping
    public InsiteSearchResult searchResults(@RequestParam("q") String query,
                                            @RequestParam("pdf") String isPdf,
                                            @RequestParam(value = "userId", required = false) String userId) {

        userId = userId == null ? "" : userId;
        recordSearchResults(userId, query);
        Algorithm8 algorithm = new Algorithm8();
        try {
            return algorithm.api(query, isPdf.equals("true"), userId);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return new InsiteSearchResult();
    }

    private void recordSearchResults(String userId, String query) {
        db.addSearchHistoryOfUser(new SearchInfo(userId, query, new Date()));
    }

}
