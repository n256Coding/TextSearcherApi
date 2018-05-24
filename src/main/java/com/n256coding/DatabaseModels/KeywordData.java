package com.n256coding.DatabaseModels;

public class KeywordData {
    public String word;
    public int freq;
    public double tfidf;

    public KeywordData(String word, int freq, double tfidf) {
        this.word = word;
        this.freq = freq;
        this.tfidf = tfidf;
    }
}
