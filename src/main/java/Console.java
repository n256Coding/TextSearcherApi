import com.n256coding.Models.FreeEbook;
import com.n256coding.Services.Filters.TextFilter;
import com.n256coding.Services.OntologyHandler;
import com.n256coding.Services.Recommender;
import com.n256coding.Services.TextAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

public class Console {

    public Console() {
    }

    public static void main(String[] args) throws IOException, ParseException, ClassNotFoundException {
//        Trainer trainer = new Trainer();
//        DateEx date = new DateEx();
//        StopWordHelper stopWord = new StopWordHelper();
//        SearchEngineConnection searchEngine = new GoogleConnection();
//        DatabaseConnection database = new MongoDbConnection();
//        FileHandler fileHandler = new FileHandler();
//        PDFHandler pdfHandler = new PDFHandler();
//        TextAnalyzer textAnalyzer = new TextAnalyzer();
//
//
////        NLPTest nlpTest = new NLPTest();
////        nlpTest.nlpTest3();
//
//
//        //Get user keywords
//        String keywords = "Java Threading";
//        boolean isPdf = false;
//
//        //Filter and Identify spell mistakes
//        List<String> tokens = textAnalyzer.getTokenizedList(keywords, " ");
//        tokens = textAnalyzer.correctSpellings(tokens.toArray(new String[tokens.size()]));
//        String correctedKeywords = textAnalyzer.correctSpellingsV2(keywords);
//
//        //Identify related keywords
//        List<String> relatives = textAnalyzer.identifyRelatives(tokens.toArray(new String[tokens.size()]));
//
//        //Search in the web
//        List<String> webSearchKeywords = new ArrayList<>();
//        webSearchKeywords.addAll(tokens);
//        webSearchKeywords.addAll(relatives);
//        searchEngine.searchOnline(isPdf, keywords);
//
//        //Get web search results
//        for (int i = 0; i < searchEngine.getResultCount(); i++) {
//            //If search results is not in database or updated date is far than 6 months
//            List<Resource> resources = database.getTextResourcesByUrl(searchEngine.getResultedUrls().get(i));
//            if (resources.size() == 0 || date.isOlderThanMonths(resources.get(0).getLastModified(), 6)) {
//                //boolean testWord = resources.get(0).getLastModified().getTime() > new Date().getTime();
//                //Extract text content from URL or the result.
//                //Look for term frequency of that result.
//                //TODO: Needs to handle resources that older more than 6 months in another way. SEE:Reason
//                //Reason: Does not need to add as a new resources but need to update frequencies and modified date
//
//                List<KeywordData> keywordDataList = new ArrayList<>();
//                Resource resource = new Resource();
//                resource.setUrl(searchEngine.getResultedUrls().get(i));
//                resource.setPdf(isPdf);
//                resource.setLastModified(new Date());
//
//                List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
//                try {
//                    frequencies = textAnalyzer.getWordFrequency(searchEngine.getResultPageAt(i));
//                } catch (HttpStatusException httpErr) {
//                    //TODO: Replace with logger
//                    System.out.println("Error status: " + httpErr.getStatusCode());
//                    continue;
//                } catch (UnknownHostException unkHostErr) {
//                    //TODO: Replace with logger
//                    System.out.println("Unknown host at: " + unkHostErr.getMessage());
//                    continue;
//                } catch (FileNotFoundException fileNotErr) {
//                    //TODO: Replace with logger
//                    System.out.println("File not found: " + fileNotErr.getMessage());
//                    continue;
//                } catch (IOException unknownErr) {
//                    //TODO: Replace with logger
//                    System.out.println("Invalid File: " + unknownErr.getMessage());
//                }
//
//                //Here the keyword frequencies are reduced with a limit
//                for (int j = 0, k = 0; k < 20 && j < frequencies.size(); j++, k++) {
//                    Map.Entry<String, Integer> frequency = frequencies.get(j);
//                    if (stopWord.isStopWord(frequency.getKey())) {
//                        k--;
//                        continue;
//                    }
//                    KeywordData keywordData = new KeywordData(frequency.getKey(), frequency.getValue(), 0);
//                    keywordDataList.add(keywordData);
//                }
//                resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
//                //Store or update that information in database.
//                database.addResource(resource);
//            }
//        }
//
//        //Search in the database
//        List<Resource> localResources = database.getTextResourcesByKeywords(webSearchKeywords.toArray(new String[webSearchKeywords.size()]));
//
//        //Collect all relevant information from database
//        String test = "asdfasdf";
//
//        //Calculate TF-IDF values for each information to rank them.
//
//        //If the selected documents have ranks more than 10, make recommendation
//
//        //Send results to user
//        InsiteSearchResult results = new InsiteSearchResult();
//        results.setOriginalQuery(keywords);
//        results.setSpellCorrectedQuery(correctedKeywords);
//        for (Resource localResource : localResources) {
//            results.addResultItem(new InsiteSearchResultItem(
//                    localResource.getUrl(),
//                    localResource.getDescription(),
//                    ResourceRating.getRatingOfResource(localResource.getId()).getRating()
//            ));
//        }

//        JSoupSessionTester.viewSite();
//        Tester.testNLP("Java threading and object oriented concepts.");
//        Tester.googleTester();

//        OntologyHandler ontology = new OntologyHandler("D:\\SLIIT\\Year 4 Sem 1\\CDAP\\Research Project\\Resources\\Ontologies\\My_Programming.owl",
//                "http://www.semanticweb.org/nishan/ontologies/2018/5/Programming#");
//        NLPTest nlpTest = new NLPTest();
//        nlpTest.getSpecificNodeExample();


//        GoogleCustomSearchTester tester = new GoogleCustomSearchTester();
//        tester.getContent("angular 6 basics");

//        DatabaseConnection database = new MongoDbConnection(Environments.MONGO_DB_HOSTNAME, Environments.MONGO_DB_PORT);
//        System.out.println(database.getTextResourcesByUrl("https://www.javaworld.com/article/3033958/open-source-tools/open-source-career-maker-or-wipeout.html").size());




//        Algorithm4 algorithm = new Algorithm4();
//        algorithm.api("object oriented concepts");
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        TextFilter textFilter = new TextFilter();
        OntologyHandler ontologyHandler = new OntologyHandler(
                "D:\\SLIIT\\Year 4 Sem 1\\CDAP\\Research Project\\Resources\\Ontologies\\My_Programming.owl",
                "http://www.semanticweb.org/nishan/ontologies/2018/5/Programming");
        List<String> nGrams = textFilter.replaceString(textAnalyzer.getIterativeNGram("object oriented concept of programming concept", 1, 3), " ", "_");

        for (String nGram : nGrams) {
            List<String> subWordsOf = ontologyHandler.getSubWordsOf(nGram, 5);
            String test = "Hello World";
        }

        String test = "sdfsdf";
    }


}
