package com.n256coding.Actors;

import java.util.HashSet;

public class UrlFilter {
    HashSet<String> videoSites;
    public UrlFilter() {
        videoSites = new HashSet<>();
        videoSites.add("http://www.acfun.cn");
        videoSites.add("");
    }

    static String fixUrlEncoding(String url) {
        return url
                .toLowerCase()
                .replaceAll("%3f", "?")
                .replaceAll("%3d", "=");
    }

    static boolean isValidUrl(String url){
        //TODO: Look around the regex
//        return url.matches("/(?:(?:https?):\\/\\/|\\b(?:[a-z\\d]+\\.))(?:(?:[^\\s()<>]+|\\((?:[^\\s()<>]+|(?:\\([^\\s()<>]+\\)))?\\))+(?:\\((?:[^\\s()<>]+|(?:\\(?:[^\\s()<>]+\\)))?\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))?/ig");
        return url.contains("http");
    }

    static String extractUrl(String url){
        return url.substring(url.indexOf("?q=") + 3, url.indexOf("&"));
    }

    boolean isValidUrl(String url, int version){
        for (String videoSite : videoSites) {
            if(url.contains(videoSite)){
                return false;
            }
        }
        return true;
    }
}
