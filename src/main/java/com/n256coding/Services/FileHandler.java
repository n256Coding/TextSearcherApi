package com.n256coding.Services;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

public class FileHandler {
    public static final String TEXT_CORPUS_DIR = System.getProperty("user.dir") + "\\TextCorpusFiles\\";
    public static final String WORD2VEC_MODEL_PATH = TEXT_CORPUS_DIR.concat("Word2VecModel\\Word2VecModel.txt");
    public static final String WORD2VEC_MODEL_DIR = TEXT_CORPUS_DIR.concat("Word2VecModel");
    public static final String TEMP_DOWNLOAD_DIR = System.getProperty("user.dir") + "\\DownloadedFiles\\";
    public interface FileTypes {
        String PDF = ".pdf";
        String TXT = ".txt";
        String PPT = ".ppt";
        String PPTX = ".pptx";
    }

    public FileHandler() {
    }

    public String createNewCorpusFile(String textCorpus) throws IOException {
        String tempFilePath = TEXT_CORPUS_DIR.concat(UUID.randomUUID().toString());
        File file = new File(tempFilePath);
        FileUtils.writeStringToFile(file, textCorpus, Charset.defaultCharset());
        return tempFilePath;
    }

    public boolean removeFile(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    public boolean createCorpusModelDirIfNotExists() {
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
    public String downloadFile(String url, String localPath, String fileExtension) throws IOException {
        String fileName = UUID.randomUUID().toString().concat(fileExtension);
        FileUtils.copyURLToFile(new URL(url), new File(localPath+"\\"+fileName), 10000, 10000);
        return localPath+"\\"+fileName;
    }

    public void writeStringToFile(String content, String directory, String fileName, String extension) throws IOException {
        File file = new File(directory+"\\"+fileName+"."+extension);
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    public String getSystemDir(){
        return TEXT_CORPUS_DIR;
    }
}
