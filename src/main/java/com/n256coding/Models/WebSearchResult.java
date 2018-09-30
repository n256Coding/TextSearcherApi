package com.n256coding.Models;

import com.n256coding.Services.FileHandler;
import com.n256coding.Services.PDFHandler;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

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

    public String getUrlContent() throws IOException, BoilerpipeProcessingException, SAXException {
        if (url == null) {
            return "";
        }
        if (isPdf) {
            String downloadedFilePath = FileHandler.downloadFile(url,
                    FileHandler.TEMP_DOWNLOAD_DIR,
                    FileHandler.FileTypes.PDF);
            String textContent = PDFHandler.parseText(new File(downloadedFilePath));
            FileHandler.removeFile(downloadedFilePath);
            return textContent;
        }

        //Replaced with boilerpipe code
        Document pageResult = Jsoup.connect(url)
                .userAgent("Mozilla")
                .get();
//        return pageResult.text();
//        final HTMLDocument htmlDoc = HTMLFetcher.fetch(new URL(url));
//        final TextDocument doc = new BoilerpipeSAXInput(htmlDoc.toInputSource()).getTextDocument();
//        return CommonExtractors.ARTICLE_EXTRACTOR.getText();
        return ArticleExtractor.INSTANCE.getText(pageResult.html());
    }

    public String getWebCacheContent() throws IOException, BoilerpipeProcessingException {
        if (webCacheUrl == null) {
            return "";
        }
        if (isPdf) {
            String downloadedFilePath = FileHandler.downloadFile(webCacheUrl,
                    FileHandler.TEMP_DOWNLOAD_DIR,
                    FileHandler.FileTypes.PDF);
            String textContent = PDFHandler.parseText(new File(downloadedFilePath));
            FileHandler.removeFile(downloadedFilePath);
            return textContent;
        }

        Document pageResult = Jsoup.connect(url)
                .userAgent("Mozilla")
                .get();
//
        return pageResult.text();
//        return ArticleExtractor.INSTANCE.getText(pageResult.html());
    }

    public String getPageTitle() throws IOException {
        if (isPdf) {
            return "";
        }
        return Jsoup.connect(url)
                .userAgent("Mozilla")
                .get().title();
    }

    public void setPdf(boolean isPdf) {
        this.isPdf = isPdf;
    }

    public boolean isPdf() {
        return isPdf;
    }
}
