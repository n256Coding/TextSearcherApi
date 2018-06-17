package com.n256coding.Dev;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GooglePaginationTester {
    public static void testPagination() throws IOException {
        Document googlePage = Jsoup.connect("https://www.google.com/search?q=" + "Java Threading")
                .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                //TODO: Remove this when fixed .userAgent("Mozilla")
                .timeout(5000)
                .get();
        Elements paginations = googlePage.select("a.fl[href]");
        List<String> urls = new ArrayList<>();
        for (Element pagination : paginations) {
            urls.add("https://www.google.com" + pagination.attr("href"));
        }
        String hello = "sdfasdf";
    }
}
