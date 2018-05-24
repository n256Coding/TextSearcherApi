package com.n256coding.Actors;

public class UrlFilter {
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
}
