package com.n256coding.Actors.Filters;

import com.n256coding.Actors.NLPProcessor;
import com.n256coding.Actors.TextAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextFilter {
    private TextAnalyzer analyzer;
    private List<String> disqualifiedWords;
    private NLPProcessor nlpProcessor;

    public TextFilter() {
        this.analyzer = new TextAnalyzer();
        this.nlpProcessor = new NLPProcessor();
        disqualifiedWords = new ArrayList<>();
        addDisqualifiedWords("quiz");
        addDisqualifiedWords("quizzes");
    }

    private void addDisqualifiedWords(String word){
        disqualifiedWords.add(word);
    }

    public boolean isValidWebPage(String pageContent, List<Map.Entry<String, Integer>> frequencies){
        int wordCount = analyzer.getWordCount(pageContent);
        List<String> tokenizedList = analyzer.getTokenizedList(pageContent, " ");

        if(wordCount < 150){
            return false;
        }
        else if(tokenizedList.containsAll(disqualifiedWords)){
            return false;
        }
        return true;
    }

    public String replaceLemmas(String pageContent){
        return nlpProcessor.replaceWithLemma(pageContent);
    }

}
