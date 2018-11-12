package com.n256coding.Services.Filters;

public class UrlFilter {


    public UrlFilter() {
    }

    public static String decodeUrl(String url) {
        return url
                .toLowerCase()
                .replaceAll("%3f", "?")
                .replaceAll("%3d", "=");
    }

    public static String encodeUrl(String url) {
        return url
                .replaceAll(" ", "%20");
    }

    public static boolean isValidUrl(String url) {
        return url.contains("http");
    }

    public static String extractUrl(String url) {
        return url.substring(url.indexOf("?q=") + 3, url.indexOf("&"));
    }


}
