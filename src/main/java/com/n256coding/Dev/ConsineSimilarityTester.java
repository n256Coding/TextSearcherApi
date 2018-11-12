package com.n256coding.Dev;

import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Services.TextAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsineSimilarityTester {
    private DatabaseConnection database;
    private int totalDocuments;
    private List<Resource> matchingDocuments;

    public ConsineSimilarityTester() {
        database = new MongoDbConnection();
        totalDocuments = (int) database.countResources();
    }

    private List<DocumentInfo> getDocumentTfIdf(String query) {
        List<String> queryTokens = TextAnalyzer.getLuceneTokenizedList(query);
        matchingDocuments = database.getResourcesByKeywords(false, queryTokens.toArray(new String[queryTokens.size()]));
        HashMap<String, Double> allIdfValues = getAllIdfValues(queryTokens);
        List<DocumentInfo> tempList = new ArrayList<>();
        for (Resource matchingDocument : matchingDocuments) {
            for (String queryToken : queryTokens) {
                double tf = matchingDocument.getTfOf(queryToken);
                double idf = allIdfValues.get(queryToken);
                tempList.add(new DocumentInfo(matchingDocument.getId(), queryToken, tf * idf));
            }
        }
        return tempList;
    }

    private List<DocumentInfo> getDocumentTfIdf(List<String> queryTokens, List<Resource> localResources) {
        HashMap<String, Double> allIdfValues = getAllIdfValues(queryTokens);
        List<DocumentInfo> tempList = new ArrayList<>();
        matchingDocuments = localResources;
        for (Resource resource : localResources) {
            for (String queryToken : queryTokens) {
                double tf = resource.getTfOf(queryToken);
                double idf = allIdfValues.get(queryToken);
                tempList.add(new DocumentInfo(resource.getId(), queryToken, tf * idf));
            }
        }
        return tempList;
    }

    private HashMap<String, Double> getQueryTfIdf(String query) {
        List<String> queryTokens = TextAnalyzer.getLuceneTokenizedList(query);
        List<Map.Entry<String, Integer>> wordFrequency = TextAnalyzer.getWordFrequency(query);
        HashMap<String, Double> allIdfValues = getAllIdfValues(queryTokens);
        int wordCount = query.split(" ").length;
        HashMap<String, Double> tfidfList = new HashMap<>();
        for (Map.Entry<String, Integer> item : wordFrequency) {
            double tf = (double) item.getValue() / (double) wordCount;
            double idf = allIdfValues.get(item.getKey()) == null ? 0 : allIdfValues.get(item.getKey());
            tfidfList.put(item.getKey(), tf * idf);
        }
        return tfidfList;
    }

    private HashMap<String, Double> getQueryTfIdf(List<String> queryTokens) {
        List<Map.Entry<String, Integer>> wordFrequency = TextAnalyzer.getWordFrequency(queryTokens);
        HashMap<String, Double> allIdfValues = getAllIdfValues(queryTokens);
        int wordCount = queryTokens.size();
        HashMap<String, Double> tfidfList = new HashMap<>();
        for (Map.Entry<String, Integer> item : wordFrequency) {
            double tf = (double) item.getValue() / (double) wordCount;
            double idf = allIdfValues.get(item.getKey()) == null ? 0 : allIdfValues.get(item.getKey());
            tfidfList.put(item.getKey(), tf * idf);
        }
        return tfidfList;
    }

    private double getIdfOf(String word) {
        int matchingDocuments = database.getResourcesByKeywords(false, word).size();
        if (matchingDocuments == 0) {
            return 0;
        }
        return 1 + Math.log(totalDocuments / matchingDocuments);
    }

    private HashMap<String, Double> getAllIdfValues(List<String> tokens) {
        HashMap<String, Double> idfValues = new HashMap<>();
        for (String token : tokens) {
            idfValues.put(token, getIdfOf(token));
        }
        return idfValues;
    }

    private double getCosineSimilarityOfDocument(HashMap<String, Double> queryTfIdf, List<DocumentInfo> documentTfIdf) {

        /*
        Cosine Similarity (d1, d2) =  Dot product(d1, d2) / ||d1|| * ||d2||

        Dot product (d1,d2) = d1[0] * d2[0] + d1[1] * d2[1] * … * d1[n] * d2[n]
        ||d1|| = square root(d1[0]^2 + d1[1]^2 + ... + d1[n]^2)
        ||d2|| = square root(d2[0]^2 + d2[1]^2 + ... + d2[n]^2)
         */
        double dotProduct = 0.0;
        double sqrtDocument = 0.0;
        double sqrtQuery = 0.0;
        for (String queryToken : queryTfIdf.keySet()) {
            List<DocumentInfo> documents = documentTfIdf.stream().filter(doc -> doc.word.equals(queryToken)).collect(Collectors.toList());
            if (documents.size() == 0) {
                continue;
            }

            DocumentInfo document = documents.get(0);
            dotProduct += queryTfIdf.get(queryToken) * document.tfidf;
            sqrtQuery += Math.pow(queryTfIdf.get(queryToken), 2);
            sqrtDocument += Math.pow(document.tfidf, 2);
        }

        sqrtDocument = Math.sqrt(sqrtDocument);
        sqrtQuery = Math.sqrt(sqrtQuery);
        return dotProduct / (sqrtDocument * sqrtQuery);
    }

    private double getEuclideneSimilarityOfDocument(HashMap<String, Double> queryTfIdf, List<DocumentInfo> documentTfIdf) {

        double euclideneValue = 0;
        for (String queryToken : queryTfIdf.keySet()) {
            List<DocumentInfo> documents = documentTfIdf.stream().filter(doc -> doc.word.equals(queryToken)).collect(Collectors.toList());
            if (documents.size() == 0) {
                continue;
            }

            DocumentInfo document = documents.get(0);
            euclideneValue += Math.pow(document.tfidf - queryTfIdf.get(queryToken), 2);
        }
        return Math.sqrt(euclideneValue);
    }

    public HashMap<String, Double> cosineSimilarity(String query) {
        HashMap<String, Double> results = new HashMap<>();
        HashMap<String, Double> queryTfIdf = getQueryTfIdf(query);
        List<DocumentInfo> documentTfIdf = getDocumentTfIdf(query);
        for (Resource document : matchingDocuments) {
            List<DocumentInfo> documents = documentTfIdf.stream().filter(x -> x.documentId.equals(document.getId())).collect(Collectors.toList());
            results.put(document.getId(), getCosineSimilarityOfDocument(queryTfIdf, documents));
        }
        return results;
    }

    /**
     *
     * @param allTokens list of tokens that contains keywords in search query and identified relative keywords
     * @param originalTokens list of tokens that contains keywords in search query
     * @param localResources list of resources that matched with search query
     * @return ranking value of each search result
     */
    public HashMap<String, Double> rankResults(List<String> allTokens, List<String> originalTokens, List<Resource> localResources) {
        HashMap<String, Double> results = new HashMap<>();
        HashMap<String, Double> queryTfIdf = getQueryTfIdf(originalTokens);
        List<DocumentInfo> documentTfIdf = getDocumentTfIdf(originalTokens, localResources);
        for (Resource document : matchingDocuments) {
            List<DocumentInfo> documents = documentTfIdf.stream().filter(x -> x.documentId.equals(document.getId())).collect(Collectors.toList());
            results.put(document.getId(), getEuclideneSimilarityOfDocument(queryTfIdf, documents));
        }
        return results;
    }


    private class DocumentInfo {
        public String documentId = "";
        public String word = "";
        public double tfidf = 0.0;

        public DocumentInfo(String documentId, String word, double tfidf) {
            this.documentId = documentId;
            this.word = word;
            this.tfidf = tfidf;
        }
    }

}
