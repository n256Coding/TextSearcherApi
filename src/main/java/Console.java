import com.n256coding.Common.Environments;
import com.n256coding.Dev.ConsineSimilarityTester;
import com.n256coding.Services.FileHandler;
import com.n256coding.Services.Recommender;
import com.n256coding.Services.SafariDownloader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.util.Version;
import org.apache.mahout.cf.taste.common.TasteException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

public class Console {

    public Console() {
    }

    public static void main(String[] args) throws IOException, ParseException, ClassNotFoundException {
//        Trainer trainer = new Trainer();
//        DateEx date = new DateEx();
//        StopWordHelper stopWord = new StopWordHelper();
//        SearchEngineConnection searchEngine = new GoogleConnection();
//        DatabaseConnection database = new MongoDbConnection();
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
//            List<Resource> resources = database.getResourcesByUrl(searchEngine.getResultedUrls().get(i));
//            if (resources.size() == 0 || date.isOlderThanMonths(resources.get(0).getCreated_at(), 6)) {
//                //boolean testWord = resources.get(0).getCreated_at().getTime() > new Date().getTime();
//                //Extract text content from URL or the result.
//                //Look for term frequency of that result.
//                //Reason: Does not need to add as a new resources but need to update frequencies and modified date
//
//                List<KeywordData> keywordDataList = new ArrayList<>();
//                Resource resource = new Resource();
//                resource.setUrl(searchEngine.getResultedUrls().get(i));
//                resource.setPdf(isPdf);
//                resource.setCreated_at(new Date());
//
//                List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
//                try {
//                    frequencies = textAnalyzer.getWordFrequency(searchEngine.getResultPageAt(i));
//                } catch (HttpStatusException httpErr) {
//                    System.out.println("Error status: " + httpErr.getStatusCode());
//                    continue;
//                } catch (UnknownHostException unkHostErr) {
//                    System.out.println("Unknown host at: " + unkHostErr.getMessage());
//                    continue;
//                } catch (FileNotFoundException fileNotErr) {
//                    System.out.println("File not found: " + fileNotErr.getMessage());
//                    continue;
//                } catch (IOException unknownErr) {
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
//        List<Resource> localResources = database.getResourcesByKeywords(webSearchKeywords.toArray(new String[webSearchKeywords.size()]));
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
//                    ResourceRating.getRatingOfResource(localResource.getId()).getPreference()
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
//        System.out.println(database.getResourcesByUrl("https://www.javaworld.com/article/3033958/open-source-tools/open-source-career-maker-or-wipeout.html").size());




//        Algorithm4 algorithm = new Algorithm4();
//        algorithm.api("object oriented concepts");
//        Document pageResult = Jsoup.connect("https://www.completecsharptutorial.com/basic/c-events-tutorial-with-programming-example.php")
//                .userAgent("Mozilla")
//                .get();
////
////        return pageResult.text();
//        String testss = "";
//        String testsss = "";
//        try {
//            testss = ArticleExtractor.INSTANCE.getText(pageResult.html());
//            testsss = ArticleExtractor.INSTANCE.getText(new URL("https://www.completecsharptutorial.com/basic/c-events-tutorial-with-programming-example.php"));
//        } catch (BoilerpipeProcessingException e) {
//            e.printStackTrace();
//        }
//        String lemmaReplace = new TextFilter().replaceWithLemmas(testss);

        SafariDownloader safariDownloader = new SafariDownloader(Environments.SAFARI_USERNAME, Environments.SAFARI_PASSWORD);
        safariDownloader.setHeadersForSearching();
        safariDownloader.login();

        safariDownloader.searchBooks("sql database operations");
        safariDownloader.setHeaders();
        String url = safariDownloader.getResultedBookUrls().get(0);
        String content = safariDownloader.getContentOf(url);


        String test = "sdfsdf";
    }


}
