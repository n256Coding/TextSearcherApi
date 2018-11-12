package com.n256coding.Services.Filters;

import com.n256coding.Services.TextAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TextFilter {
    private List<String> disqualifiedWords;

    public TextFilter() {
        disqualifiedWords = new ArrayList<>();
        addDisqualifiedWords("quiz");
        addDisqualifiedWords("quizzes");
    }

    private void addDisqualifiedWords(String word) {
        disqualifiedWords.add(word);
    }

    public boolean isValidWebPage(String pageContent, List<Map.Entry<String, Integer>> frequencies) {
        int wordCount = TextAnalyzer.getWordCount(pageContent);
        List<String> tokenizedList = TextAnalyzer.getLuceneTokenizedList(pageContent);

        if (wordCount < 150) {
            return false;
        } else if (tokenizedList.containsAll(disqualifiedWords)) {
            return false;
        }
        return true;
    }

    public String joinListToString(List<String> wordList, String joiner) {
        return String.join(joiner, wordList.toArray(new String[wordList.size()]));
    }

    public List<String> splitString(String sentence, String delemeter) {
        return Arrays.asList(sentence.split(delemeter));
    }

    public List<String> replaceString(List<String> sentenceList, String target, String replacement) {
        List<String> outputList = new ArrayList<>();
        for (String sentence : sentenceList) {
            outputList.add(sentence.replace(target, replacement));
        }
        return outputList;
    }

    public String replaceString(String sentence, String target, String replacement) {
        return sentence.replace(target, replacement);
    }
}
