package com.n256coding.Controllers;

import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.SearchInfo;
import com.n256coding.DatabaseModels.TrustedSites;
import com.n256coding.Dev.ApiAlgorithms.Algorithm9;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Models.InsiteSearchResult;
import com.n256coding.Models.OperationStatus;
import com.n256coding.Models.Rating;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;


@CrossOrigin(origins = Environments.CROSS_ORIGIN)
@RestController
@RequestMapping("/api/resource")
public class MainController {
    private DatabaseConnection db = new MongoDbConnection();

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

    @GetMapping("/trustedsites")
    public List<TrustedSites> viewTrustedSites(String query) {
        System.out.println(query);
        if(query == null || query.equalsIgnoreCase("undefined")){
            return db.getTutorialSites();
        }
        return db.getTutorialSites(query.split(" "));
    }

    @PostMapping("/trustedsites")
    public OperationStatus upsertTrustedSites(@RequestBody TrustedSites trustedSite) {
        if (db.addOrUpdateTutorialSites(trustedSite) > 0) {
            return new OperationStatus("ok");
        }
        return new OperationStatus("error");
    }

    @DeleteMapping("/trustedsites")
    public OperationStatus deleteTrustedSite(String keyword) {
        if (db.deleteTutorialSitesByKeyword(keyword) > 0) {
            return new OperationStatus("ok");
        }
        return new OperationStatus("error");
    }

    @GetMapping
    public InsiteSearchResult searchResults(@RequestParam("q") String query,
                                            @RequestParam("pdf") String isPdf,
                                            @RequestParam(value = "userId", required = false, defaultValue = "5b458b7daf2fc54bd8efa2af") String userId) {
        System.out.println("Search request found ------------------------------------------------------------------");
        userId = userId == null ? "" : userId;
        recordSearchResults(userId, query);
        Algorithm9 algorithm = new Algorithm9();
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
