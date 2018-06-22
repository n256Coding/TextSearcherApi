package com.n256coding.Dev;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import java.util.ArrayList;
import java.util.List;

public class GoogleCustomSearchTester {
    private final String GOOGLE_API_KEY = "AIzaSyCdvDUeg6kR2tQhSoLXfEVAXiHz_wwHtPc";
    private final String SEARCH_ENGINE_ID = "018198584361994989762:zdl9lg_zqrk";


    public List<Result> search(String keyword){
        Customsearch customsearch= null;
        try {
            customsearch = new Customsearch(new NetHttpTransport(),new JacksonFactory(), new HttpRequestInitializer() {
                public void initialize(HttpRequest httpRequest) {
                    try {
                        // set connect and read timeouts
                        httpRequest.setConnectTimeout(5000);
                        httpRequest.setReadTimeout(5000);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Result> resultList=null;
        try {
            Customsearch.Cse.List list=customsearch.cse().list(keyword);
            list.setKey(GOOGLE_API_KEY);
            list.setCx(SEARCH_ENGINE_ID);
            Search results=list.execute();
            resultList=results.getItems();
        }
        catch (  Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public void getContent(String query){
        List<Result> results = new ArrayList<>();

        try {
            results = search(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(Result result : results){
            System.out.println(result.getDisplayLink());
            System.out.println(result.getTitle());
            // all attributes:
            System.out.println(result.toString());
        }
    }
}
