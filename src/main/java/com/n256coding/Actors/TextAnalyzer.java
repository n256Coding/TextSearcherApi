package com.n256coding.Actors;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.lucene.queryparser.classic.ParseException;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TextAnalyzer {
    private FileHandler fileHandler;

    public TextAnalyzer() {
        this.fileHandler = new FileHandler();
    }

    public List<String> identifyRelatives(String... keywords) {
        List<String> synonyms = new ArrayList<>();
        File file = new File(fileHandler.WORD2VEC_MODEL_PATH);
        if (!file.exists()) {
            return synonyms;
        }

        Word2Vec vec = WordVectorSerializer.readWord2VecModel(fileHandler.WORD2VEC_MODEL_PATH);
        for (String keyword : keywords) {
            Collection<String> synonymCollection = vec.wordsNearest(keyword, 5);
            synonyms.addAll(synonymCollection);
        }

        return synonyms;
    }

    public void trainWord2VecModel(String textCorpus) throws IOException {
        Word2Vec vec;
        File word2vecFile = new File(fileHandler.WORD2VEC_MODEL_PATH);
        String tempFilePath = fileHandler.createNewCorpusFile(textCorpus);
        SentenceIterator iterator = new BasicLineIterator(tempFilePath);
        //TODO: fileHandler.removeCorpusFile(tempFilePath);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        VocabCache<VocabWord> cache = new AbstractCache<>();
        WeightLookupTable<VocabWord> table = new InMemoryLookupTable.Builder<VocabWord>()
                .vectorLength(100)
                .useAdaGrad(false)
                .cache(cache).build();

        if (word2vecFile.exists()) {
            vec = WordVectorSerializer.readWord2VecModel(fileHandler.WORD2VEC_MODEL_PATH);
            vec.setTokenizerFactory(tokenizerFactory);
            vec.setSentenceIterator(iterator);
        } else {
            fileHandler.createCorpusModelDirIfNotExists();
            vec = new Word2Vec.Builder()
                    .minWordFrequency(5)
                    .iterations(5)
                    .epochs(1)
                    .layerSize(100)
                    .seed(42)
                    .windowSize(5)
                    .iterate(iterator)
                    .tokenizerFactory(tokenizerFactory)
                    .lookupTable(table)
                    .vocabCache(cache)
                    .build();
        }
        vec.fit();
        WordVectorSerializer.writeWord2VecModel(vec, fileHandler.WORD2VEC_MODEL_PATH);
    }

    public List<Map.Entry<String, Integer>> getWordFrequency(String corpus) {
        StringTokenizer tokenizer = new StringTokenizer(corpus.toLowerCase());
        HashMap<String, Integer> dictionary = new HashMap<>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (dictionary.containsKey(token)) {
                int count = dictionary.get(token);
                count++;
                dictionary.put(token, count);
            } else {
                dictionary.put(token, 1);
            }
        }

        List<Map.Entry<String, Integer>> list = new LinkedList<>(dictionary.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        dictionary = null;
//        LinkedHashMap<String, Integer> sortedDictionary = new LinkedHashMap<>();
//        for (Map.Entry<String, Integer> entry : list) {
//            sortedDictionary.put(entry.getKey(), entry.getValue());
//        }
        return list;
    }

    public List<String> correctSpellings(String... keywords) throws IOException, ParseException, ClassNotFoundException {
        String correctedKeyword = "";
        SpellChecker spellChecker = new SpellChecker();
        for (String keyword : keywords) {
            correctedKeyword = correctedKeyword.concat(spellChecker.getCorrection(keyword));
        }

        StringTokenizer stringTokenizer = new StringTokenizer(correctedKeyword, " ");
        List<String> tokenizedWords = new ArrayList<>(stringTokenizer.countTokens());

        while (stringTokenizer.hasMoreTokens()) {
            tokenizedWords.add(stringTokenizer.nextToken());
        }

        return tokenizedWords;
    }

    public String correctSpellingsV2(String query) throws IOException {
        JLanguageTool langTool = new JLanguageTool(new AmericanEnglish());
        for (Rule rule : langTool.getAllRules()) {
            if (!rule.isDictionaryBasedSpellingRule()) {
                langTool.disableRule(rule.getId());
            }
        }
        StringBuffer buff = new StringBuffer(query);
        List<RuleMatch> matches = langTool.check(query);
        for (RuleMatch match : matches) {
            if(match.getSuggestedReplacements().size() > 0){
                buff.replace(match.getFromPos(), match.getToPos(), match.getSuggestedReplacements().get(0));
            }
        }
        return buff.toString();
    }

    public List<String> nlpGetNouns(String text){
        List<String> nouns = new ArrayList<>();
        Properties props = new Properties();
        props.setProperty("annotators","tokenize, ssplit, pos");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if(pos.startsWith("N")){
                    nouns.add(word);
                }
            }
        }
        return nouns;
    }

    public List<String> nlpGetLemmas(String text){
        List<String> lemmas = new ArrayList<>();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                lemmas.add(lemma);
            }
        }
        return lemmas;
    }

    public List<String> getTokenizedList(String sentence, String delim){
        List<String> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(sentence, delim);
        while (tokenizer.hasMoreTokens()){
            tokens.add(tokenizer.nextToken());
        }

        return tokens;
    }
}
