import com.n256coding.Actors.FileHandler;
import com.n256coding.Actors.GoogleConnection;
import com.n256coding.Actors.PDFHandler;
import com.n256coding.Actors.TextAnalyzer;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.KeywordData;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Dev.Trainer;
import com.n256coding.Interfaces.DatabaseConnection;
import com.n256coding.Interfaces.SearchEngineConnection;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jsoup.HttpStatusException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Console {
    public static void main(String[] args) throws IOException, ParseException, ClassNotFoundException {
        Trainer trainer = new Trainer();
        SearchEngineConnection searchEngine = new GoogleConnection();
        DatabaseConnection database = new MongoDbConnection();
        FileHandler fileHandler = new FileHandler();
        PDFHandler pdfHandler = new PDFHandler();
        TextAnalyzer textAnalyzer = new TextAnalyzer();


        //Get user keywords
        String keywords = "Java Threading";
        boolean isPdf = true;

        //Filter and Identify spell mistakes
        List<String> tokens = textAnalyzer.getTokenizedList(keywords, " ");
        tokens = textAnalyzer.correctSpellings(tokens.toArray(new String[tokens.size()]));

        //Identify related keywords
        List<String> relatives = textAnalyzer.identifyRelatives(tokens.toArray(new String[tokens.size()]));

        //Search in the web
        List<String> webSearchKeywords = new ArrayList<>();
        webSearchKeywords.addAll(tokens);
        webSearchKeywords.addAll(relatives);
        searchEngine.searchOnline(isPdf, keywords);

        //Get web search results
        for (int i = 0; i < searchEngine.getResultCount(); i++) {
            //If search results is not in database or updated date is far than 6 months
            List<Resource> resources = database.getTextResourcesByUrl(searchEngine.getResultedUrls().get(i));
            if(resources.size() == 0 || resources.get(0).getLastModified().getTime() > new Date().getTime()){
                //Extract text content from URL or the result.
                //Look for term frequency of that result.
                List<KeywordData> keywordDataList = new ArrayList<>();
                Resource resource = new Resource();
                resource.setUrl(searchEngine.getResultedUrls().get(i));
                resource.setPdf(isPdf);
                resource.setLastModified(new Date());

                List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
                try{
                    frequencies = textAnalyzer.getWordFrequency(searchEngine.getResultPageAt(i));
                }catch (HttpStatusException httpErr){
                    //TODO: Replace with logger
                    System.out.println("Error status: "+httpErr.getStatusCode());
                    continue;
                }catch (UnknownHostException unkHostErr){
                    //TODO: Replace with logger
                    System.out.println("Unknown host at: "+unkHostErr.getMessage());
                    continue;
                }catch (FileNotFoundException fileNotErr){
                    //TODO: Replace with logger
                    System.out.println("File not found: "+fileNotErr.getMessage());
                    continue;
                }catch (IOException unknownErr){
                    //TODO: Replace with logger
                    System.out.println("Invalid File: "+unknownErr.getMessage());
                }

                //Here the keyword frequencies are reduced with a limit
                for (int j = 0; j < 20 && j < frequencies.size(); j++) {
                    Map.Entry<String, Integer> frequency = frequencies.get(j);
                    KeywordData keywordData = new KeywordData(frequency.getKey(), frequency.getValue(), 0);
                    keywordDataList.add(keywordData);
                }
                resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
                //Store or update that information in database.
                database.addResource(resource);
            }
        }

        //Search in the database

        //Collect all relevant information from database
        //Calculate TF-IDF values for each information to rank them.

        //If the selected documents have ranks more than 10, make recommendation

        //Send results to user
    }
}
