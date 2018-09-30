package com.n256coding.Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.n256coding.Interfaces.BookDownloader;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;

public class SafariDownloader implements BookDownloader {
    private final String USERNAME;
    private final String PASSWORD;
    private Map<String, String> browserCookies;
    private Map<String, String> headersForBookInfo;
    private Map<String, Map<String, String>> searchResults = new HashMap<>();


    private SafariDownloader(String USERNAME, String PASSWORD, Map<String, String> browserCookies, Map<String, String> headersForBookInfo, Map<String, Map<String, String>> searchResults) {
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.browserCookies = browserCookies;
        this.headersForBookInfo = headersForBookInfo;
        this.searchResults = searchResults;
    }

    public SafariDownloader(String USERNAME, String PASSWORD) {
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
    }

    public void setHeaders() {
        this.headersForBookInfo = new HashMap<>();
        this.headersForBookInfo.put("Host", "www.safaribooksonline.com");
        this.headersForBookInfo.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0");
        this.headersForBookInfo.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        this.headersForBookInfo.put("Accept-Language", "en-US,en;q=0.5");
        this.headersForBookInfo.put("Accept-Encoding", "gzip, deflate, br");
        this.headersForBookInfo.put("Connection", "keep-alive");
        this.headersForBookInfo.put("Upgrade-Insecure-Requests", "1");
        this.headersForBookInfo.put("If-None-Match", "W/\"02f283e891729eeba9d422f8939f37b0");
    }

    public void setHeadersForSearching() {
        this.headersForBookInfo = new HashMap<>();
        this.headersForBookInfo.put("Host", "www.safaribooksonline.com");
        this.headersForBookInfo.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0");
        this.headersForBookInfo.put("Accept", "*/*");
        this.headersForBookInfo.put("Accept-Language", "en-US,en;q=0.5");
        this.headersForBookInfo.put("Accept-Encoding", "gzip, deflate, br");
        this.headersForBookInfo.put("Content-Type", "application/json");
        this.headersForBookInfo.put("Content-Length", "319");
        this.headersForBookInfo.put("Connection", "keep-alive");
        this.headersForBookInfo.put("TE", "Trailers");
    }

    public void login() throws IOException {
        Connection.Response loginFormResponse = Jsoup.connect("https://www.safaribooksonline.com/accounts/login/")
                .method(Connection.Method.GET)
                .execute();

        Connection.Response document = Jsoup.connect("https://www.safaribooksonline.com/accounts/login/")
                .data("csrfmiddlewaretoken", loginFormResponse.cookie("csrfsafari"))
                .data("email", this.USERNAME)
                .data("login", "Sign+In")
                .data("password1", this.PASSWORD)

                .header("Host", "www.safaribooksonline.com")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:62.0) Gecko/20100101 Firefox/62.0")
                .header("Accept", "")
                .header("Referer", "https://www.safaribooksonline.com/accounts/login/")
                .cookies(loginFormResponse.cookies())
                .method(Connection.Method.POST)
                .execute();

        this.browserCookies = loginFormResponse.cookies();
        this.browserCookies.putAll(document.cookies());
    }


    private void searchBooks(String searchQuery, int page) throws IOException {
        JsonArray formats = new JsonArray();
        formats.add("book");

        JsonObject json = new JsonObject();
        json.addProperty("extended_publisher_data", true);
        json.add("formats", formats);
        json.addProperty("highlight", true);
        json.addProperty("include_assessments", false);
        json.addProperty("include_case_studies", true);
        json.addProperty("include_courses", true);
        json.addProperty("include_orioles", true);
        json.addProperty("include_playlists", true);
        json.addProperty("is_academic_institution_account", false);
        json.add("languages", new JsonArray());
        json.addProperty("page", page);
        json.add("publishers", new JsonArray());
        json.addProperty("query", searchQuery);
        json.addProperty("source", "user");
        json.add("topics", new JsonArray());
        String jsonOutput = Jsoup.connect("https://www.safaribooksonline.com/api/v2/search/")
                .method(Connection.Method.POST)
                .cookies(this.browserCookies)
                .headers(this.headersForBookInfo)
                .ignoreContentType(true)
                .requestBody(json.toString())
                .execute()
                .body();

        JsonObject resultObject = new JsonParser().parse(jsonOutput).getAsJsonObject();
        JsonArray resultsArray = resultObject.getAsJsonArray("results");

        //No results found
        if (resultsArray.size() == 0) {
            return;
        }

        Iterator<JsonElement> resultsIterator = resultsArray.iterator();
        HashMap<String, String> bookInfo;
        while (resultsIterator.hasNext()) {
            bookInfo = new HashMap<>();
            JsonObject resultJson = resultsIterator.next().getAsJsonObject();
            String descriptionHtml = "";
            //Some search results contains only a chapter of a book. So needs to retrieve full book
            if (resultJson.get("archive") != null) {
                resultJson = getJsonFrom(resultJson.get("archive").getAsString());
            }

            //Even in some book results, does not contains chapters. So they needs to retrieve
            if(resultJson.get("chapters") == null && resultJson.get("url") != null){
                resultJson = getJsonFrom(resultJson.get("url").getAsString());
            }

            bookInfo.put("description", parseHtml(resultJson.get("description").getAsString()));
            bookInfo.put("cover_url", resultJson.get("cover").getAsString());
            bookInfo.put("title", resultJson.get("title").getAsString());
            bookInfo.put("url", resultJson.get("url").getAsString());
            this.searchResults.put(resultJson.get("web_url").getAsString(), bookInfo);
        }
    }

    private String getUrlByWebUrl(String webUrl) {
        return this.searchResults.get(webUrl).get("url");
    }

    private JsonObject getJsonFrom(String url) throws IOException {
        String jsonOutput = Jsoup.connect(url)
                .cookies(this.browserCookies)
                .headers(this.headersForBookInfo)
                .timeout(50000)
                .ignoreContentType(true)
                .execute()
                .body();
        return new JsonParser().parse(jsonOutput).getAsJsonObject();
    }

    private String getContentOfUrl(String url) throws IOException {
        return Jsoup.connect(url)
                .cookies(this.browserCookies)
                .headers(this.headersForBookInfo)
                .get()
                .text();
    }

    private String parseHtml(String htmlDocument) {
        return Jsoup.parse(htmlDocument).text();
    }

    public void searchBooks(String query) throws IOException {
        setHeadersForSearching();
        for (int i = 0; i < 2; i++) {
            searchBooks(query, i);
        }
    }

    public List<String> getResultedBookUrls() {
        return new ArrayList<>(this.searchResults.keySet());
    }

    public String getTitleOf(String bookUrl) {
        return this.searchResults.get(bookUrl).get("title");
    }

    public String getCoverImageUrlOf(String bookUrl) {
        return this.searchResults.get(bookUrl).get("cover_url");
    }

    public String getDescriptionOf(String bookUrl) {
        String description = this.searchResults.get(bookUrl).get("description");

        //Longer descriptions does not needs to show in search results. So it will truncated to match the size
        if(description.length() > 260){
            description = description.substring(0, 260).concat(" ...");
        }
        return description;
    }

    public String getContentOf(String bookUrl) throws IOException {
        String url = getUrlByWebUrl(bookUrl);
        JsonObject bookInfoJson = getJsonFrom(url);

        StringBuilder textContent = new StringBuilder();
        JsonArray chaptersArrayJson = bookInfoJson.getAsJsonArray("chapters");
        Iterator<JsonElement> chaptersIterator = chaptersArrayJson.iterator();
        while (chaptersIterator.hasNext()) {
            String chapterUrl = chaptersIterator.next().getAsString();
            JsonObject chapterInfoJson = getJsonFrom(chapterUrl);
            String webUrl = chapterInfoJson.get("web_url").getAsString();
            textContent.append(getContentOfUrl(webUrl));
        }

        return textContent.toString();
    }

    public BookDownloader clone(){
        return new SafariDownloader(this.USERNAME,
                this.PASSWORD,
                this.browserCookies,
                this.headersForBookInfo,
                this.searchResults);
    }
}
