package com.n256coding.Dev.ApiAlgorithms;

import com.n256coding.Helpers.StopWordHelper;
import com.n256coding.Services.*;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.KeywordData;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.DatabaseModels.ResourceRating;
import com.n256coding.Dev.Trainer;
import com.n256coding.Helpers.DateEx;
import com.n256coding.Helpers.SortHelper;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Interfaces.SearchEngineConnection;
import com.n256coding.Models.InsiteSearchResult;
import com.n256coding.Models.InsiteSearchResultItem;
import com.n256coding.Models.WebSearchResult;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.jsoup.HttpStatusException;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Deprecated
public class Algorithm2 {
    @SuppressWarnings("Duplicates")
    public InsiteSearchResult api(String query) throws IOException {
        Trainer trainer = new Trainer();
        DateEx date = new DateEx();
        SortHelper sort = new SortHelper();
        OntologyHandler ontology = new OntologyHandler(
                "D:\\SLIIT\\Year 4 Sem 1\\CDAP\\Research Project\\Resources\\Ontologies\\My_Programming.owl",
                "http://www.semanticweb.org/nishan/ontologies/2018/5/Programming");
        StopWordHelper stopWordHelper = new StopWordHelper();
        SearchEngineConnection searchEngine = new GoogleConnection();
        DatabaseConnection database = new MongoDbConnection();
        FileHandler fileHandler = new FileHandler();
        PDFHandler pdfHandler = new PDFHandler();
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        NLPProcessor nlpProcessor = new NLPProcessor();


        //Get user keywords
        //String keywords = "Java Threading";
        boolean isPdf = false;

        //Filter and Identify spell mistakes
        List<String> tokens = new ArrayList<>();

        //Correct spellings
        String spellCorrectedQuery = textAnalyzer.correctSpellingsV2(query);

        //Use NLP to identify most important words
        List<String> nouns = nlpProcessor.get(NLPProcessor.WordType.NOUN, query);
        List<String> verbs = nlpProcessor.get(NLPProcessor.WordType.VERB, query);
        List<String> questions = nlpProcessor.get(NLPProcessor.WordType.QUESTION, query);
        List<String> adjectives = nlpProcessor.get(NLPProcessor.WordType.ADJECTIVE, query);
        List<String> lemmas = nlpProcessor.get(NLPProcessor.WordType.LEMMA, query);

        tokens.addAll(nouns);
        tokens.addAll(verbs);
        tokens.addAll(adjectives);
        for (String lemma : lemmas) {
            if (!tokens.contains(lemma) && !lemma.equalsIgnoreCase("be")) {
                tokens.add(lemma);
            }
        }

        //Identify related keywords
        List<String> relatives = textAnalyzer.identifyRelatives(tokens.toArray(new String[tokens.size()]));
        for (String token : tokens) {
            relatives.addAll(ontology.getSubWordsOf(token, 3));
        }

        //Search in the web
        List<String> webSearchKeywords = new ArrayList<>();
        webSearchKeywords.addAll(tokens);
        webSearchKeywords.addAll(relatives);

        for (String tutorialSite : SearchEngineConnection.TUTORIAL_SITES) {

            try {
                searchEngine.searchOnline(tutorialSite, isPdf, query);
            } catch (HttpStatusException ex) {
                if (ex.getStatusCode() == 503) {
                    //TODO: Replace with logger
                    System.out.println("Google block detected!");
                    try {
                        Thread.sleep(5000);
                        searchEngine.searchOnline(tutorialSite, isPdf, query);
                    } catch (InterruptedException e) {
                        //TODO: Replace with logger
                        e.printStackTrace();
                    } catch (HttpStatusException e) {
                        //TODO: Place info in a logger
                        System.out.println("Google block in second time");
                    }
                }
            }

            //Get web search results
            while (searchEngine.hasMoreResults()) {
                WebSearchResult result = searchEngine.nextResult();
                //If search results is not in database or updated date is far than 6 months
                List<Resource> resources = database.getTextResourcesByUrl(result.getUrl());
                if (resources.size() == 0 || date.isOlderThanMonths(resources.get(0).getLastModified(), 6)) {
                    //boolean testWord = resources.get(0).getLastModified().getTime() > new Date().getTime();
                    //Extract text content from URL or the result.
                    //Look for term frequency of that result.
                    //TODO: Needs to handle resources that older more than 6 months in another way. SEE:Reason
                    //Reason: Does not need to add as a new resources but need to update frequencies and modified date

                    List<KeywordData> keywordDataList = new ArrayList<>();
                    Resource resource = new Resource();
                    resource.setUrl(result.getUrl());
                    resource.setPdf(isPdf);
                    resource.setLastModified(new Date());
                    resource.setDescription(result.getDescription());

                    List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
                    int wordCount = 0;
                    try {
                        String tempPage = result.getUrlContent();
                        wordCount = textAnalyzer.getWordCount(tempPage);
                        frequencies = textAnalyzer.getWordFrequency(tempPage);
                    } catch (HttpStatusException httpErr) {
                        //TODO: Replace with logger
                        System.out.println("Error status: " + httpErr.getStatusCode());
                        continue;
                    } catch (UnknownHostException unkHostErr) {
                        //TODO: Replace with logger
                        System.out.println("Unknown host at: " + unkHostErr.getMessage());
                        continue;
                    } catch (FileNotFoundException fileNotErr) {
                        //TODO: Replace with logger
                        System.out.println("File not found: " + fileNotErr.getMessage());
                        continue;
                    } catch (IOException unknownErr) {
                        //TODO: Replace with logger
                        System.out.println("Invalid File: " + unknownErr.getMessage());
                    } catch (BoilerpipeProcessingException e) {
                        //TODO: Replace with logger
                        continue;
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }

                    //Here the keyword frequencies are reduced with a limit
                    for (int j = 0, k = 0; k < 20 && j < frequencies.size(); j++, k++) {
                        Map.Entry<String, Integer> frequency = frequencies.get(j);
                        if (stopWordHelper.isStopWord(frequency.getKey())) {
                            k--;
                            continue;
                        }
                        KeywordData keywordData = new KeywordData(frequency.getKey(), frequency.getValue(), ((double) frequency.getValue() / (double) wordCount));
                        keywordDataList.add(keywordData);
                    }
                    resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
                    //Store or update that information in database.
                    database.addResource(resource);
                }
            }

        }

        //Search in the database
        String[] searchKeywords = webSearchKeywords.toArray(new String[webSearchKeywords.size()]);
        List<Resource> localResources = database.getTextResourcesByKeywords(searchKeywords);
        long totalNumberOfDocuments = database.countResources();

        //Collect all relevant information from database
        String test = "asdfasdf";

        //Calculate TF-IDF values for each information to rank them.
        for (Resource localResource : localResources) {
            localResource.getTfOf(webSearchKeywords.toArray(new String[webSearchKeywords.size()]));
        }

        //If the selected documents have ranks more than 10, make recommendation
        //TODO: implement recommendation part

        //Send results to user
        InsiteSearchResult results = new InsiteSearchResult();
        results.setOriginalQuery(query);
        results.setSpellCorrectedQuery(spellCorrectedQuery);
        for (Resource localResource : localResources) {
            int rating = 0;
            if (ResourceRating.getRatingOfResource(localResource.getId()) != null) {
                rating = ResourceRating.getRatingOfResource(localResource.getId()).getRating();
            }
            results.addResultItem(new InsiteSearchResultItem(
                    localResource.getId(),
                    localResource.getUrl(),
                    localResource.getDescription(),
                    rating,
                    textAnalyzer.getTFIDFWeightOfWords(totalNumberOfDocuments,
                            localResources.size(),
                            localResource,
                            searchKeywords)
            ));
        }
        //Sort results with TF-IDF weights
        results.sort();

        return results;
    }
}
