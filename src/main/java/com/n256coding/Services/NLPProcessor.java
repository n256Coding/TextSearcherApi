package com.n256coding.Services;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class NLPProcessor {

    public enum WordType {
        NOUN,
        VERB,
        ADJECTIVE,
        LEMMA,
        QUESTION
    }

    public static String getRootWord(String text) {
        // set up properties
        Properties props = new Properties();
        props.setProperty("ssplit.eolonly", "true");
        props.setProperty("annotators", "tokenize, ssplit, pos, depparse");
        // set up pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // get contents from file

        //System.out.println(content);
        // read in a product review per line
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        SemanticGraph tree = sentences.get(0).get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
        IndexedWord root = tree.getFirstRoot();
        return root.toString();
    }

    public static List<String> get(WordType type, String text) {
        String identifier = "";
        List<String> results = new ArrayList<>();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        switch (type) {
            case NOUN:
                identifier = "N";
                break;
            case VERB:
                identifier = "V";
                break;
            case ADJECTIVE:
                identifier = "J";
                break;
            case QUESTION:
                identifier = "W";
                break;
            case LEMMA:
                for (CoreMap sentence : sentences) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        results.add(lemma);
                    }
                }
                return results;
        }
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (pos.startsWith(identifier)) {
                    results.add(word);
                }
            }
        }
        return results;
    }

    public static String replaceWithLemma(String text) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                if (!token.originalText().equalsIgnoreCase(lemma)) {
                    try {
                        text = text.replaceAll("\\b" + token.originalText() + "\\b", lemma);
                    } catch (IllegalArgumentException ex) {
                        continue;
                    }
                }
            }
        }
        return text;
    }
}
