package com.n256coding.Interfaces;

import java.io.IOException;
import java.util.List;

public interface BookDownloader {
    void searchBooks(String query) throws IOException;
    List<String> getResultedBookUrls();
    String getTitleOf(String bookUrl);
    String getCoverImageUrlOf(String bookUrl);
    String getDescriptionOf(String bookUrl);
    String getContentOf(String bookUrl) throws IOException;
    BookDownloader clone();
}
