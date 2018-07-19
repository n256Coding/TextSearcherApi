package com.n256coding.Services;

import com.n256coding.Common.Environments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Helpers.StopWordHelper;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Models.TfIdfData;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermFrequencyAttribute;
import org.apache.lucene.index.Term;
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

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;

public class TextAnalyzer {
    private FileHandler fileHandler;
    private DatabaseConnection database;

    public TextAnalyzer() {
        this.database = new MongoDbConnection(Environments.MONGO_DB_HOSTNAME, Environments.MONGO_DB_PORT);
    }

    public List<String> identifyRelatives(String... keywords) {
        List<String> semanticallyRelatives = new ArrayList<>();
        File file = new File(FileHandler.WORD2VEC_MODEL_PATH);
        if (!file.exists()) {
            return semanticallyRelatives;
        }

        Word2Vec vec = WordVectorSerializer.readWord2VecModel(FileHandler.WORD2VEC_MODEL_PATH);
        for (String keyword : keywords) {
            Collection<String> wordsNearest = vec.wordsNearest(keyword, 5);
            semanticallyRelatives.addAll(wordsNearest);
        }

        return semanticallyRelatives;
    }

    public void trainWord2VecModel(String textCorpus) throws IOException {
        Word2Vec vec;
        File word2vecFile = new File(FileHandler.WORD2VEC_MODEL_PATH);
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
            vec = WordVectorSerializer.readWord2VecModel(FileHandler.WORD2VEC_MODEL_PATH);
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
        WordVectorSerializer.writeWord2VecModel(vec, FileHandler.WORD2VEC_MODEL_PATH);
    }

    public List<Map.Entry<String, Integer>> getWordFrequency(String corpus) {
        List<String> tokenizedList = getLuceneTokenizedList(corpus.toLowerCase());
        HashMap<String, Integer> dictionary = new HashMap<>();
        for (String token : tokenizedList) {
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

    @Deprecated
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
            if (match.getSuggestedReplacements().size() > 0) {
                buff.replace(match.getFromPos(), match.getToPos(), match.getSuggestedReplacements().get(0));
            }
        }
        return buff.toString();
    }

    public int getWordCount(String document) {
        return getTokenizedList(document, " ").size();
    }

    public double getTFIDFWeightOfWords(long totalNumberOfDocuments,
                                        int matchingNoOfDocuments,
                                        Resource resource,
                                        String... words) {
        return resource.getTfOf(words) * Math.log((double) totalNumberOfDocuments / (double) matchingNoOfDocuments);
    }

    public double getTFIDFWeight(long totalNumberOfDocuments,
                                 int matchingNoOfDocuments,
                                 double termFrequency) {
        return termFrequency * Math.log(((double) totalNumberOfDocuments) / ((double) matchingNoOfDocuments));
    }

    public List<String> getTokenizedList(String sentence, String delim) {
        List<String> tokens = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(sentence, delim);
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens;
    }

    public List<String> getLuceneTokenizedList(String sentence) {
        List<String> tokenList = new ArrayList<>();
        CharArraySet stopwordSet = new CharArraySet(new StopWordHelper().getStopWords(), false);
        StandardAnalyzer analyzer = new StandardAnalyzer(stopwordSet);
        try (TokenStream tokenStream = analyzer.tokenStream("content", sentence)) {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String word = tokenStream.getAttribute(CharTermAttribute.class).toString();
                tokenList.add(word);
            }
        } catch (IOException e) {
            //TODO: Replace with logger
            e.printStackTrace();
        }
        return tokenList;
    }

    public List<String> getNGramOf(String sentence, int n) {
        List<String> nGramList = new ArrayList<>();
        StringBuilder stringBuilder;
        String[] tokens = sentence.split(" ");
        for (int i = 0; i <= tokens.length - n; i++) {
            stringBuilder = new StringBuilder("");
            for (int j = i; j < i + n; j++) {
                stringBuilder.append(tokens[j]);
                stringBuilder.append(j == i + n - 1 ? "" : " ");
            }
            nGramList.add(stringBuilder.toString());
        }
        return nGramList;
    }


    public List<String> getIterativeNGram(String sentence, int min, int max) {
        List<String> nGrams = new ArrayList<>();
        if (min > max)
            return nGrams;
        for (int i = min - 1; i < max; i++) {
            nGrams.addAll(getNGramOf(sentence, i + 1));
        }
        return nGrams;
    }

    @SuppressWarnings("Duplicates")
    public Map<String, Double> calculateWeightedTfIdf(List<String> allTokens, List<String> originalQueryTokens, List<Resource> matchingDocuments) {
        //Experimenting code segment - Start////////////////////////////////////////////////////////////////////////////////////

//        //Get matching documents from database
//        List<Resource> matchingDocuments = database.getResourcesByKeywords(
//                allTokens.toArray(new String[allTokens.size()])
//        );


        //List Example Instance:
        //ObjectID               Keyword           TF_IDF               WeightedValue
        //sdfasdgws3423rfef      Java              0.02566261           5.0/2
        //sdfaefsdvcxvcxbbs      Java              0.05623123           5.0/2
        List<TfIdfData> tfIdfValues = new ArrayList<>();
        for (Resource document : matchingDocuments) {
            for (String keyword : allTokens) {
                double weightedValue = 0;
                if (originalQueryTokens.contains(keyword)) {
                    weightedValue = (double) 2.0 / (double) allTokens.size();
                } else {
                    weightedValue = (double) 1.0 / (double) allTokens.size();
                }
                if ((document.getTitle() == null ? "" : document.getTitle()).contains(keyword)) {
                    weightedValue++;
                }
                double tfidfWeight = getTFIDFWeight(database.countResources(),
                        matchingDocuments.size(),
                        document.getTfOf(keyword)
                );
                tfIdfValues.add(new TfIdfData(document.getId(),
                                keyword,
                                getTFIDFWeight(database.countResources(),
                                        matchingDocuments.size(),
                                        document.getTfOf(keyword)
                                ),
                                weightedValue
                        )
                );
            }
        }

        //Get averaging TF-IDF values for every identified document
        Map<String, Double> weightedTfIdf = tfIdfValues.stream()
                .collect(groupingBy(TfIdfData::getDocumentId, averagingDouble(TfIdfData::getWeightedTfIdfValue)));
        return weightedTfIdf;
        //Experimenting code segment - End/////////////////////////////////////////////////////////////////////////
    }


}
