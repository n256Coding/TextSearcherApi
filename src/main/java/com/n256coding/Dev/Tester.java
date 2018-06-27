package com.n256coding.Dev;

import com.n256coding.Services.GoogleConnection;
import com.n256coding.Services.NLPProcessor;
import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.WebSearchResult;

import java.io.IOException;
import java.util.List;

public class Tester {
    public static void testNLP(String sentence){
        sentence = "what is concurrent process in java";
        NLPProcessor processor = new NLPProcessor();
        List<String> nouns = processor.get(NLPProcessor.WordType.NOUN, sentence);
        List<String> verb = processor.get(NLPProcessor.WordType.VERB, sentence);
        List<String> question = processor.get(NLPProcessor.WordType.QUESTION, sentence);
        List<String> adjective = processor.get(NLPProcessor.WordType.ADJECTIVE, sentence);
        List<String> lemma = processor.get(NLPProcessor.WordType.LEMMA, sentence);
        String root = processor.getRootWord(sentence);
        String test = "sdfasdf";
    }

    public static void googleTester() throws IOException {
        SearchEngineConnection searchEngine = new GoogleConnection();
        for (String tutorialSite : SearchEngineConnection.TUTORIAL_SITES) {
            searchEngine.searchOnline(tutorialSite, false, "Java threading");
            while(searchEngine.hasMoreResults()){
                WebSearchResult result = searchEngine.nextResult();
                String test = "asdfasdf";
            }
        }
    }
}
