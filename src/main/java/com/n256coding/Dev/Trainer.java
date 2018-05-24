package com.n256coding.Dev;

import com.n256coding.Actors.TextAnalyzer;

import java.io.File;
import java.io.IOException;

public class Trainer {
    public void trainWord2Vec(String dirPath) throws IOException {
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        File documentDir = new File(dirPath);
        File[] documents = documentDir.listFiles();

        for (int i = 0; i < documents.length; i++) {
            File document = documents[i];
            System.out.println("Reading: "+document.getName());
            try{
                textAnalyzer.trainWord2VecModel(document.getAbsolutePath());
            }catch (OutOfMemoryError err){
                System.out.println("Out of Memory -----");
            }
            System.out.println(document.getName() + " train completed");
        }
    }
}
