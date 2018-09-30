package com.n256coding.Services;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFHandler {
    private PDFHandler() {
    }

    public static String parseText(File bookFile) throws IOException {
        PDFTextStripper pdfTextStripper;
        PDDocument pdDocument;
        COSDocument cosDocument;

        RandomAccessRead randomAccessRead = new RandomAccessFile(bookFile, "r");
        PDFParser pdfParser = new PDFParser(randomAccessRead);
        pdfParser.parse();
        cosDocument = pdfParser.getDocument();
        pdfTextStripper = new PDFTextStripper();
        pdDocument = new PDDocument(cosDocument);
        pdfTextStripper.setStartPage(1);
        String parsedText = pdfTextStripper.getText(pdDocument);
        pdDocument.close();
        cosDocument.close();
        return parsedText;
    }

    //TODO: Dev method
    public static void convertAllPdfsToTxt(String dir) throws IOException {
        File dirs = new File(dir);
        for (File file : dirs.listFiles()) {
            FileHandler.writeStringToFile(parseText(file), FileHandler.TEMP_DOWNLOAD_DIR, file.getName(), FileHandler.FileTypes.TXT);
        }
    }

}
