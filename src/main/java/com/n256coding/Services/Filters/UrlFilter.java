package com.n256coding.Services.Filters;

import com.n256coding.Services.TextAnalyzer;

import java.util.*;

public class UrlFilter {
    final static HashMap<String, List<String>> SITES = new HashMap<String, List<String>>(){{
        put("default", Arrays.asList(
                "tutorialspoint.com",
                "javatpoint.com"
        ));
        put(" java java-ee java-se ee ", Arrays.asList(
                "tutorialspoint.com/java",
                "javatpoint.com",
                "javaworld.com",
                "javatutorial.net"
        ));
        put(" c-sharp c# c_sharp csharp ", Arrays.asList(
                "completecsharptutorial.com",
                "tutorialspoint.com/csharp",
                "dotnetperls.com",
                "javatpoint.com",
                "tutorialsteacher.com"
        ));
        put(" c++ cpp c-plus-plus ", Arrays.asList(
                "cplusplus.com/doc/tutorial/",
                "cplusplus.com/reference/",
                "learncpp.com",
                "tutorialspoint.com",
                "javatpoint.com"
        ));
        put(" web html css javascript angularjs angular ", Arrays.asList(
                "w3schools.com",
                "tutorialspoint.com",
                "javatpoint.com"
        ));
        put(" sql_db database db mysql sqlserver ", Arrays.asList(
                "w3schools.com/sql",
                "tutorialspoint.com/sql",
                "tutorialspoint.com/dbms"
        ));
        put(" nosql mongodb mongo_db ", Arrays.asList(
                "javatpoint.com",
                "tutorialspoint.com/mongodb",
                "tutorialspoint.com/dbms"
        ));
        put(" nodejs node_js ", Arrays.asList(
                "w3schools.com/nodejs"
        ));
        put(" python ", Arrays.asList(
                "w3schools.com/python",
                "tutorialspoint.com/python"
        ));
    }};



    public UrlFilter() {
    }

    public static String decodeUrl(String url) {
        return url
                .toLowerCase()
                .replaceAll("%3f", "?")
                .replaceAll("%3d", "=");
    }

    public static String encodeUrl(String url){
        return url
                .replaceAll(" ", "%20");
    }

    public static boolean isValidUrl(String url) {
        return url.contains("http");
    }

    public static String extractUrl(String url) {
        return url.substring(url.indexOf("?q=") + 3, url.indexOf("&"));
    }

    public static List<String> getTutorialSites(String query){
        List<String> siteList = new ArrayList<>();
        for (String token : query.split(" ")) {
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
