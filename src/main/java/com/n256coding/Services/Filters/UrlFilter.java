package com.n256coding.Services.Filters;

import com.n256coding.Services.TextAnalyzer;

import java.util.*;

public class UrlFilter {
    final HashMap<String, List<String>> SITES = new HashMap<>();
    private TextAnalyzer analyzer = new TextAnalyzer();


    public UrlFilter() {
        SITES.put("default", Arrays.asList(
                "tutorialspoint.com",
                "javatpoint.com"
        ));
        SITES.put(" java java-ee java-se ", Arrays.asList(
                "tutorialspoint.com/java",
                "javatpoint.com",
                "javaworld.com"
        ));
        SITES.put(" c-sharp c# c_sharp csharp ", Arrays.asList(
                "completecsharptutorial.com",
                "tutorialspoint.com/csharp",
                "javatpoint.com",
                "guru99.com",
                "tutorialsteacher.com"
        ));
        SITES.put(" c++ cpp c-plus-plus ", Arrays.asList(
                "cplusplus.com/doc/tutorial/",
                "cplusplus.com/reference/",
                "learncpp.com",
                "tutorialspoint.com",
                "javatpoint.com"
        ));
        SITES.put(" web html css javascript angularjs angular ", Arrays.asList(
                "w3schools.com",
                "tutorialspoint.com",
                "javatpoint.com"
        ));
        SITES.put(" sql_db database db mysql sqlserver ", Arrays.asList(
                "w3schools.com/sql",
                "tutorialspoint.com/sql",
                "tutorialspoint.com/dbms"
        ));
        SITES.put(" nosql mongodb mongo_db ", Arrays.asList(
                "javatpoint.com",
                "tutorialspoint.com/mongodb",
                "tutorialspoint.com/dbms"
        ));
        SITES.put(" nodejs node_js ", Arrays.asList(
                "w3schools.com/nodejs"
        ));
        SITES.put(" python ", Arrays.asList(
                "w3schools.com/python",
                "tutorialspoint.com/python"
        ));
    }

    public String fixUrlEncoding(String url) {
        return url
                .toLowerCase()
                .replaceAll("%3f", "?")
                .replaceAll("%3d", "=");
    }

    public boolean isValidUrl(String url) {
        return url.contains("http");
    }

    public String extractUrl(String url) {
        return url.substring(url.indexOf("?q=") + 3, url.indexOf("&"));
    }

    public List<String> getTutorialSites(String query){
        List<String> siteList = new ArrayList<>();
        List<String> tokens = analyzer.getTokenizedList(query, " ");
        for (String token : tokens) {
            for (String key : SITES.keySet()) {
                if(key.matches(".* " + token + " .*")){
                    siteList.addAll(SITES.get(key));
                }
            }
        }
        if(siteList.size() == 0){
            siteList.addAll(SITES.get("default"));
        }
        return siteList;
    }
}
