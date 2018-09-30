package com.n256coding.Services;

import com.n256coding.Interfaces.BookDownloader;
import com.n256coding.Models.WebSearchResult;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProgrammingBooksDownloader implements BookDownloader {
    private List<String> bookUrlList = new ArrayList<>();
    private List<String> bookUrlListStep2 = new ArrayList<>();
    private HashMap<String, String[]> bookUrlListStep3 = new HashMap<>();
    private List<String> paginationUrls = new ArrayList<>();
    private Document resultPage;

    public ProgrammingBooksDownloader() {
    }

    private ProgrammingBooksDownloader(List<String> bookUrlList, List<String> bookUrlListStep2, HashMap<String, String[]> bookUrlListStep3, List<String> paginationUrls, Document resultPage) {
        this.bookUrlList = bookUrlList;
        this.bookUrlListStep2 = bookUrlListStep2;
        this.bookUrlListStep3 = bookUrlListStep3;
        this.paginationUrls = paginationUrls;
        this.resultPage = resultPage;
    }

    public void searchBooks(String query) throws IOException {
        this.resultPage = Jsoup.connect("http://www.programming-book.com/?s=" + query)
                .timeout(50000)
                .userAgent("Mozilla")
                .get();

        paginationUrls.add("http://www.programming-book.com/?s=" + query);
        //If true, site contains pagination bar
        if (resultPage.select("div.wp-pagenavi").size() > 0) {

            Element paginationBar = resultPage.select("div.wp-pagenavi").get(0);
            //If class="last" is in the pagination bar
            if (paginationBar.select("a.last").size() > 0) {
                Element lastLink = paginationBar.select("a.last").get(0);
                String lastLinkUrl = lastLink.attr("href");
                int startIndex = lastLinkUrl.lastIndexOf("/page/") + 6;
                int endIndex = lastLinkUrl.lastIndexOf("/?s");
                int lastIndex = Integer.parseInt(lastLinkUrl.substring(startIndex, endIndex));

                for (int i = 2; i <= lastIndex; i++) {
                    paginationUrls.add("http://www.programming-book.com/page/" + i + "/?s=" + query);
                }
            }
        }

        for (int i = 0; i < paginationUrls.size() && i < 2; i++) {
            System.out.println("Working on pagination " + i);
            if (i != 0) {
                resultPage = Jsoup.connect(paginationUrls.get(i))
                        .timeout(10000)
                        .userAgent("Mozilla")
                        .get();
            }
            Elements urls = resultPage.select("a.imghover");
            Elements metaInfo = resultPage.select("div.doc-meta");
            for (int j = 0; j < metaInfo.size(); j++) {
                if (metaInfo.get(j).text().trim().equalsIgnoreCase("Pages 1 |".trim())) {
                    continue;
                }
                String href = urls.get(j).attr("href");
                bookUrlList.add(href);
            }

            System.out.println("Book url list: step 1");
            for (String url : bookUrlList) {
                Document tempDoc = Jsoup.connect(url)
                        .timeout(10000)
                        .userAgent("Mozilla")
                        .get();
                Elements select = tempDoc.select("a#download");
                if (select.size() == 0) {
                    continue;
                }

                String href = select.get(0).attr("href");
                bookUrlListStep2.add(href);
            }

            System.out.println("Book url list: step 2");
            for (String url : bookUrlListStep2) {
                String[] bookData = new String[2];
                Document tempDoc = Jsoup.connect(url)
                        .timeout(10000)
                        .userAgent("Mozilla")
                        .get();
                Elements script = tempDoc.select("script");
                if (script.size() < 13) {
                    continue;
                }

                //Get code segment in last script tag within the page
                String originalUrl = script.get(script.size()-1).data().substring(
                        script.get(script.size()-1).data().indexOf("window.location.replace(\"") + 25,
                        script.get(script.size()-1).data().indexOf(".pdf\")") + 4
                );
                bookData[0] = "http://www.programming-book.com/doc-images/" + url.substring(url.lastIndexOf("=") + 1) + ".png";
                Elements bookInfos = tempDoc.select("div#full-width-content > h1");
                if (bookInfos.size() > 0) {
                    bookData[1] = bookInfos.get(0).ownText();
                }
                bookUrlListStep3.put(originalUrl, bookData);
                System.out.println("Identified: " + originalUrl);
            }
            bookUrlList.clear();
            bookUrlListStep2.clear();
        }
    }

    public List<String> getResultedBookUrls() {
        return new ArrayList<>(bookUrlListStep3.keySet());
    }

    public String getTitleOf(String bookUrl) {
        String[] bookInfo = bookUrlListStep3.get(bookUrl);
        return bookInfo[1];
    }

    public String getCoverImageUrlOf(String bookUrl) {
        String[] bookInfo = bookUrlListStep3.get(bookUrl);
        return bookInfo[0];
    }

    public String getDescriptionOf(String bookUrl) {
        return "";
    }

    public String getContentOf(String bookUrl) throws IOException {
        WebSearchResult searchResult = new WebSearchResult();
        searchResult.setUrl(bookUrl);
        searchResult.setDescription("");
        searchResult.setPdf(true);

        try {
            return searchResult.getUrlContent();
        } catch (BoilerpipeProcessingException | SAXException e) {
            //TODO: Add logger
            e.printStackTrace();
        }

        return "";
    }

    public BookDownloader clone(){
        return new ProgrammingBooksDownloader(this.bookUrlList,
                this.bookUrlListStep2,
                this.bookUrlListStep3,
                this.paginationUrls,
                this.resultPage);
    }
}
