package com.n256coding.Services;

import com.n256coding.Services.Filters.UrlFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

public class FileHandler {
    public static final String TEMP_DOWNLOAD_DIR = System.getProperty("user.dir") + "\\DownloadedFiles\\";
    public static final String TEMP_FILES_DIR = System.getProperty("user.dir")+"\\TemporaryFiles\\";
    public static final String TEXT_CORPUS_DIR = System.getProperty("user.dir") + "\\TextCorpusFiles\\";
    public static final String WORD2VEC_MODEL_DIR = TEXT_CORPUS_DIR.concat("Word2VecModel");
    public static final String WORD2VEC_MODEL_PATH = TEXT_CORPUS_DIR.concat("Word2VecModel\\Word2VecModel.txt");
    public static final String ONTOLOGY_FILE_PATH = System.getProperty("user.dir")+"\\Ontology\\My_Programming.owl";
    public static final String LOG_FILE_PATH = System.getProperty("user.dir") + "\\Logger\\Log.txt";

    public interface FileTypes {
        String PDF = ".pdf";
        String TXT = ".txt";
        String PPT = ".ppt";
        String PPTX = ".pptx";
    }

    private FileHandler() {
    }

    public static String createNewCorpusFile(String textCorpus) throws IOException {
        String tempFilePath = TEXT_CORPUS_DIR.concat(UUID.randomUUID().toString());
        File file = new File(tempFilePath);
        FileUtils.writeStringToFile(file, textCorpus, Charset.defaultCharset());
        return tempFilePath;
    }

    public static boolean removeFile(String filePath) throws IOException {
        File file = new File(filePath);
        return FileUtils.deleteQuietly(file);
    }

    public static boolean createCorpusModelDirIfNotExists() {
        File file = new File(WORD2VEC_MODEL_DIR);
        if(!file.exists()){
            return file.mkdirs();
        }
        return false;
    }

    /**
     * @param url url of the remote location
     * @param localPath local path of the downloaded file
     * @return generated file name
     * @throws IOException
     */
    public static String downloadFile(String url, String localPath, String fileExtension) throws IOException {
        String fileName = UUID.randomUUID().toString().concat(fileExtension);
        if(url.contains("programming-book.com/download")){
            url = UrlFilter.encodeUrl(url);
            URL urlConn=new URL(url);
            URLConnection conn = urlConn.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
            conn.setRequestProperty("Cookie", "_ga=GA1.2.1403070994.1529653524; _gid=GA1.2.821862884.1531139613");
            conn.connect();
            FileUtils.copyInputStreamToFile(conn.getInputStream(), new File(localPath+"\\"+fileName));
        }
        else{
            FileUtils.copyURLToFile(new URL(url), new File(localPath+"\\"+fileName), 100000, 100000);
        }

        return localPath+"\\"+fileName;
    }

    public static void writeStringToFile(String content, String directory, String fileName, String extension) throws IOException {
        File file = new File(directory+"\\"+fileName+"."+extension);
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    public static String createRecommendationFile(List<String> values) throws IOException {
        String fileName = UUID.randomUUID().toString().concat(".csv");
        File file = new File(TEMP_FILES_DIR+fileName);
        FileUtils.writeLines(file, values, true);

        return TEMP_FILES_DIR+fileName;
    }
}
