package com.n256coding.DatabaseModels;

public class KeywordData {
    private String word;
    private int freq;
    private double tf;

    public KeywordData(String word, int freq, double tf) {
        this.word = word;
        this.freq = freq;
        this.tf = tf;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public double getTf() {
        return tf;
    }

    public void setTf(double tf) {
        this.tf = tf;
    }
}
