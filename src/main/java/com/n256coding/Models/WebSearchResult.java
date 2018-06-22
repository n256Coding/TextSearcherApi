package com.n256coding.Models;

import com.n256coding.Actors.FileHandler;
import com.n256coding.Actors.PDFHandler;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;

public class WebSearchResult {
    private int id;
    private String url;
    private String webCacheUrl;
    private String description;
    private boolean isPdf;

    public WebSearchResult() {
    }

    public WebSearchResult(String url, @Nullable String webCacheUrl, String description, boolean isPdf) {
        this.url = url;
        this.webCacheUrl = webCacheUrl;
        this.description = description;
        this.isPdf = isPdf;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWebCacheUrl() {
        return webCacheUrl;
    }

    public void setWebCacheUrl(String webCacheUrl) {
        this.webCacheUrl = webCacheUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlContent() throws IOException, BoilerpipeProcessingException {
        if(url == null) {
            return "";
        }
        if(isPdf){
            PDFHandler pdfHandler = new PDFHandler();
            FileHandler fileHandler = new FileHandler();
            String downloadedFilePath = fileHandler.downloadFile(url,
                    FileHandler.TEMP_DOWNLOAD_DIR,
                    FileHandler.FileTypes.PDF);
            String textContent = pdfHandler.parseText(new File(downloadedFilePath));
            fileHandler.removeFile(downloadedFilePath);
            return textContent;
        }

        //Replaced with boilerpipe code
//        Document pageResult = Jsoup.connect(url)
//                .userAgent("Mozilla")
//                .get();
//        return pageResult.text();
        return ArticleExtractor.INSTANCE.getText(new URL(url));
    }

    public String getWebCacheContent() throws IOException, BoilerpipeProcessingException {
        if(webCacheUrl == null){
            return "";
        }
        if(isPdf){
            PDFHandler pdfHandler = new PDFHandler();
            FileHandler fileHandler = new FileHandler();
            String downloadedFilePath = fileHandler.downloadFile(webCacheUrl,
                    FileHandler.TEMP_DOWNLOAD_DIR,
                    FileHandler.FileTypes.PDF);
            String textContent = pdfHandler.parseText(new File(downloadedFilePath));
            fileHandler.removeFile(downloadedFilePath);
            return textContent;
        }

//        Document pageResult = Jsoup.connect(url)
//                .userAgent("Mozilla")
//                .get();
//
//        return pageResult.text();
        return ArticleExtractor.INSTANCE.getText(new URL(url));
    }

    public boolean isPdf() {
        return isPdf;
    }
}
