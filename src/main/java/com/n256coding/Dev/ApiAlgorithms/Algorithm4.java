package com.n256coding.Dev.ApiAlgorithms;

import com.n256coding.Helpers.StopWordHelper;
import com.n256coding.Services.*;
import com.n256coding.Services.Filters.TextFilter;
import com.n256coding.Common.Environments;
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
import com.n256coding.Models.TfIdfData;
import com.n256coding.Models.WebSearchResult;
import com.n256coding.Services.Filters.UrlFilter;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import org.jsoup.HttpStatusException;
import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.groupingBy;

public class Algorithm4 {
    Trainer trainer = new Trainer();
    DateEx date = new DateEx();
    SortHelper sort = new SortHelper();
    OntologyHandler ontology = new OntologyHandler(
            "D:\\SLIIT\\Year 4 Sem 1\\CDAP\\Research Project\\Resources\\Ontologies\\My_Programming.owl",
            "http://www.semanticweb.org/nishan/ontologies/2018/5/Programming");
    StopWordHelper stopWordHelper = new StopWordHelper();
    SearchEngineConnection searchEngine = new GoogleConnection();
    DatabaseConnection database = new MongoDbConnection(Environments.MONGO_DB_HOSTNAME, Environments.MONGO_DB_PORT);
    FileHandler fileHandler = new FileHandler();
    PDFHandler pdfHandler = new PDFHandler();
    TextAnalyzer textAnalyzer = new TextAnalyzer();
    NLPProcessor nlpProcessor = new NLPProcessor();
    TextFilter textFilter = new TextFilter();
    UrlFilter urlFilter = new UrlFilter();

    @SuppressWarnings("Duplicates")
    public InsiteSearchResult api(String query) throws IOException {


        //Get user keywords
        //String keywords = "Java Threading";
        boolean isPdf = false;

        List<String> originalQueryTokens = new ArrayList<>();

        //Filter and Identify spell mistakes
        //Correct spellings
        String spellCorrectedQuery = textAnalyzer.correctSpellingsV2(query);

        //Use NLP to identify most important words
        List<String> nouns = nlpProcessor.get(NLPProcessor.WordType.NOUN, query);
        List<String> verbs = nlpProcessor.get(NLPProcessor.WordType.VERB, query);
        List<String> adjectives = nlpProcessor.get(NLPProcessor.WordType.ADJECTIVE, query);
        List<String> lemmas = nlpProcessor.get(NLPProcessor.WordType.LEMMA, query);

        //Add identified nouns, verbs, adjectives and lemmas to search
        originalQueryTokens.addAll(nouns);
        originalQueryTokens.addAll(verbs);
        originalQueryTokens.addAll(adjectives);
        for (String lemma : lemmas) {
            if (!originalQueryTokens.contains(lemma) && !lemma.equalsIgnoreCase("be")) {
                originalQueryTokens.add(lemma);
            }
        }

        //Identify related keywords for user given keywords
        //Use words vectors to get semantically relative words
        List<String> relatives = textAnalyzer.identifyRelatives(originalQueryTokens.toArray(new String[originalQueryTokens.size()]));
        //Use pre-defined ontology to get relative words
        for (String token : originalQueryTokens) {
            relatives.addAll(ontology.getSubWordsOf(token, 5));
        }


        List<String> webSearchKeywords = new ArrayList<>();
        webSearchKeywords.addAll(originalQueryTokens);
        webSearchKeywords.addAll(relatives);


        String[] searchKeywords = webSearchKeywords.toArray(new String[webSearchKeywords.size()]);
        //Search in the database
        List<Resource> localResources = database.getTextResourcesByKeywords(searchKeywords);

        //Filter out old resources
        for (Resource localResource : localResources) {
            if(date.isOlderThanMonths(localResource.getLastModified(), 6)){
                localResources.remove(localResource);
            }
        }

        //If local database does not contains considerable amount of results, search in internet
        if(localResources.size() < 40){
            searchInternet(isPdf, query);
            localResources = database.getTextResourcesByKeywords(searchKeywords);
        }


        //If the selected documents have ranks more than 10, make recommendation
        //TODO: implement recommendation part


        //Calculate TF-IDF values to rank results
        Map<String, Double> weightedTfIdf = calculateWeightedTfIdf(webSearchKeywords, originalQueryTokens, localResources);

        //Send results to user
        InsiteSearchResult results = new InsiteSearchResult();
        results.setOriginalQuery(query);
        results.setSpellCorrectedQuery(spellCorrectedQuery);

        int resultCount = 0;
        for (Resource localResource : localResources) {
            if(resultCount >= 20){
                break;
            }
            int rating = 0;
            if (ResourceRating.getRatingOfResource(localResource.getId()) != null) {
                rating = ResourceRating.getRatingOfResource(localResource.getId()).getRating();
            }
            results.addResultItem(new InsiteSearchResultItem(
                            localResource.getId(),
                            localResource.getUrl(),
                            localResource.getDescription(),
                            rating,
                            weightedTfIdf.get(localResource.getId())
                    )
            );
            resultCount++;
        }

        //Sort results with TF-IDF weights
        results.sort();

        return results;
    }


    @SuppressWarnings("Duplicates")
    public void searchInternet(boolean isPdf, String query){
        for (String tutorialSite : urlFilter.getTutorialSites(query)) {

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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Get web search results
            while (searchEngine.hasMoreResults()) {
                WebSearchResult result = null;
                try {
                    result = searchEngine.nextResult();
                    if(result == null)
                        break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //If selected resource (URL) is not in database or updated date is older than 3 months
                //Add or update resource information
                List<Resource> resources = database.getTextResourcesByUrl(result.getUrl());
                if (resources.size() == 0 || date.isOlderThanMonths(resources.get(0).getLastModified(), 3)) {
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
                        tempPage = textFilter.replaceWithLemmas(tempPage);
                        wordCount = textAnalyzer.getWordCount(tempPage);
                        frequencies = textAnalyzer.getWordFrequency(tempPage);

                        //Skip web pages that not qualify with given conditions
                        if(!textFilter.isValidWebPage(tempPage, frequencies)){
                            continue;
                        }
                    } catch (HttpStatusException httpErr) {
                        //TODO: Replace with logger
                        System.out.println("Error status: " + httpErr.getStatusCode());
                        continue;
                    } catch (UnknownHostException | MalformedURLException unkHostErr) {
                        //TODO: Replace with logger
                        System.out.println("Unknown host at: " + unkHostErr.getMessage());
                        continue;
                    } catch (FileNotFoundException fileNotErr) {
                        //TODO: Replace with logger
                        System.out.println("File not found: " + fileNotErr.getMessage());
                        continue;
                    } catch (IOException unknownErr) {
                        //TODO: Replace with logger
                        System.out.println("Unknown Error: " + unknownErr.getMessage());
                    } catch (BoilerpipeProcessingException e) {
                        //TODO: Replace with logger
                        continue;
                    } catch (SAXException e) {
                        //TODO: Replace with logger
                        e.printStackTrace();
                    }

                    //Here the keyword frequencies are reduced with a limit
                    //Current limit is 10
                    for (int j = 0, k = 0; k < 20 && j < frequencies.size(); j++, k++) {
                        Map.Entry<String, Integer> frequency = frequencies.get(j);
                        if (stopWordHelper.isStopWord(frequency.getKey())) {
                            k--;
                            continue;
                        }
                        KeywordData keywordData = new KeywordData(frequency.getKey(),
                                frequency.getValue(),
                                ((double) frequency.getValue() / (double) wordCount));
                        keywordDataList.add(keywordData);
                    }
                    resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
                    //Store or update that information in database.
                    database.addResource(resource);
                }
            }

        }
    }

    @SuppressWarnings("Duplicates")
    public Map<String, Double> calculateWeightedTfIdf(List<String> webSearchKeywords, List<String> originalQueryTokens, List<Resource> matchingDocuments){
        //Experimenting code segment - Start////////////////////////////////////////////////////////////////////////////////////

//        //Get matching documents from database
//        List<Resource> matchingDocuments = database.getTextResourcesByKeywords(
//                webSearchKeywords.toArray(new String[webSearchKeywords.size()])
//        );



        //List Example Instance:
        //ObjectID               Keyword           TF_IDF               WeightedValue
        //sdfasdgws3423rfef      Java              0.02566261           5.0/2
        //sdfaefsdvcxvcxbbs      Java              0.05623123           5.0/2
        List<TfIdfData> tfIdfValues = new ArrayList<>();
        for (Resource document : matchingDocuments) {
            for (String keyword : webSearchKeywords) {
                if(originalQueryTokens.contains(keyword)){
                    tfIdfValues.add(new TfIdfData(document.getId(),
                            keyword,
                            textAnalyzer.getTFIDFWeight(database.countResources(),
                                    matchingDocuments.size(),
                                    document.getTfOf(keyword)
                            ),
                            ((double) 2.0 / (double) webSearchKeywords.size()))
                    );
                }else{
                    tfIdfValues.add(new TfIdfData(document.getId(),
                            keyword,
                            textAnalyzer.getTFIDFWeight(database.countResources(),
                                    matchingDocuments.size(),
                                    document.getTfOf(keyword)
                            ),
                            ((double) 1.0 / (double) webSearchKeywords.size()))
                    );
                }
            }
        }

        //Get averaging TF-IDF values for every identified document
        Map<String, Double> weightedTfIdf = tfIdfValues.stream()
                .collect(groupingBy(TfIdfData::getDocumentId, averagingDouble(TfIdfData::getWeightedTfIdfValue)));
        String test = "asdfadsfsdfg";
        return weightedTfIdf;
        //Experimenting code segment - End/////////////////////////////////////////////////////////////////////////
    }
}
