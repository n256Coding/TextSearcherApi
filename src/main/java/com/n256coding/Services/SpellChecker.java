//package com.n256coding.Services;
//
//import com.aliasi.lm.NGramProcessLM;
//import com.aliasi.spell.CompiledSpellChecker;
//import com.aliasi.spell.FixedWeightEditDistance;
//import com.aliasi.spell.TrainSpellChecker;
//import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
//import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
//import com.aliasi.tokenizer.TokenizerFactory;
//import com.aliasi.util.Files;
//import com.aliasi.util.Streams;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field;
//import org.apache.lucene.document.FieldType;
//import org.apache.lucene.index.DirectoryReader;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.queryparser.classic.ParseException;
//import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.Query;
//import org.apache.lucene.search.TopDocs;
//import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.store.NativeFSLockFactory;
//import org.apache.lucene.store.SimpleFSDirectory;
//
//import java.io.*;
//
//public class SpellChecker {
//    private static final File MODEL_FILE = new File("SpellCheck.model");
//    private static final int NGRAM_LENGTH = 5;
//    static final File DATA = new File("0643");
//    static final File LUCENE_INDEX_DIR = new File("lucene");
//    static final StandardAnalyzer ANALYZER = new StandardAnalyzer();
//    static final double MATCH_WEIGHT = -0.0;
//    static final double DELETE_WEIGHT = -4.0;
//    static final double INSERT_WEIGHT = -1.0;
//    static final double SUBSTITUTE_WEIGHT = -2.0;
//    static final double TRANSPOSE_WEIGHT = -2.0;
//    static final int MAX_HITS = 100;
//    static final String TEXT_FIELD = "text";
//
//    public boolean trainDataset() throws IOException, ParseException, ClassNotFoundException {
//        FixedWeightEditDistance fixedEdit = new FixedWeightEditDistance(MATCH_WEIGHT,
//                DELETE_WEIGHT,
//                INSERT_WEIGHT,
//                SUBSTITUTE_WEIGHT,
//                TRANSPOSE_WEIGHT);
//
//        NGramProcessLM lm = new NGramProcessLM(NGRAM_LENGTH);
//        TokenizerFactory tokenizerFactory = new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE);
//        TrainSpellChecker sc = new TrainSpellChecker(lm, fixedEdit, tokenizerFactory);
//
//        FSDirectory fsDir = new SimpleFSDirectory(LUCENE_INDEX_DIR.toPath(), NativeFSLockFactory.INSTANCE);
//        IndexWriter luceneIndexWriter = new IndexWriter(fsDir,
//                new IndexWriterConfig(ANALYZER));
//
//        if(!DATA.isDirectory()){
//            System.out.println("Could not find training directory="+DATA);
//            System.out.println("Have you unpacked the data?");
//            return false;
//        }
//
//        String[] filesToIndex = DATA.list();
//        for (int i = 0; i < filesToIndex.length; ++i) {
//            System.out.println("    File=" + DATA + "/" + filesToIndex[i]);
//            String charSequence = Files.readFromFile(new File(DATA, filesToIndex[i]), "ISO-8859-1");
//            sc.handle(charSequence);
//            Document luceneDocument = new Document();
//            FieldType fieldType = new FieldType();
//            fieldType.setStored(true);
//            fieldType.setTokenized(true);
//            Field textField = new Field(TEXT_FIELD, charSequence, fieldType);
//            luceneDocument.add(textField);
//            luceneIndexWriter.addDocument(luceneDocument);
//        }
//
//        System.out.println("Writing model to file=" + MODEL_FILE);
//        writeModel(sc, MODEL_FILE);
//
//        System.out.println("Writing lucene index to ="+LUCENE_INDEX_DIR);
//        luceneIndexWriter.close();
//        return true;
//    }
//
//    public String getCorrection(String queryString)
//            throws IOException, ParseException, ClassNotFoundException {
//        String correction = null;
//        FSDirectory fsDir = new SimpleFSDirectory(LUCENE_INDEX_DIR.toPath(), NativeFSLockFactory.INSTANCE);
//        IndexReader indexReader = DirectoryReader.open(fsDir);
//        IndexSearcher searcher = new IndexSearcher(indexReader);
//        CompiledSpellChecker sc = readModel(MODEL_FILE);
//        TokenizerFactory tokenizerFactory = new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE);
//        sc.setTokenizerFactory(tokenizerFactory);
//
//        QueryParser queryParser = new QueryParser(TEXT_FIELD, ANALYZER);
//
//        Query query = queryParser.parse(queryString);
//        TopDocs results = searcher.search(query,MAX_HITS);
//
//        System.out.println("Found " + results.totalHits
//                + " document(s) that matched query '"
//                + queryString + "':");
//
//        // compute alternative spelling
//        String bestAlternative = sc.didYouMean(queryString);
//
//        if (bestAlternative.equals(queryString)) {
//            System.out.println(" No spelling correction found.");
//            correction = queryString;
//        } else {
//            try {
//                Query alternativeQuery
//                        = queryParser.parse(bestAlternative);
//                TopDocs results2 = searcher.search(alternativeQuery,MAX_HITS);
//                System.out.println("Found " + results2.totalHits
//                        + " document(s) matching best alt='"
//                        + bestAlternative + "':");
//                correction = bestAlternative;
//            } catch (ParseException e) {
//                System.out.println("Best alternative not valid query.");
//                System.out.println("Alternative=" + bestAlternative);
//                //TODO: Check this
//                correction = "#"+bestAlternative;
//            }
//        }
//        return correction;
//    }
//
//    private CompiledSpellChecker readModel(File file) throws IOException, ClassNotFoundException {
//        // create object input stream from file
//        FileInputStream fileIn = new FileInputStream(file);
//        BufferedInputStream bufIn = new BufferedInputStream(fileIn);
//        ObjectInputStream objIn = new ObjectInputStream(bufIn);
//
//        // read the spell checker
//        CompiledSpellChecker sc = (CompiledSpellChecker) objIn.readObject();
//
//        // close the resources and return result
//        Streams.closeQuietly(objIn);
//        Streams.closeQuietly(bufIn);
//        Streams.closeQuietly(fileIn);
//        return sc;
//    }
//
//    private void writeModel(TrainSpellChecker sc, File MODEL_FILE)
//            throws IOException {
//
//        // create object output stream from file
//        FileOutputStream fileOut = new FileOutputStream(MODEL_FILE);
//        BufferedOutputStream bufOut = new BufferedOutputStream(fileOut);
//        ObjectOutputStream objOut = new ObjectOutputStream(bufOut);
//
//        // write the spell checker to the file
//        sc.compileTo(objOut);
//
//        // close the resources
//        Streams.closeQuietly(objOut);
//        Streams.closeQuietly(bufOut);
//        Streams.closeQuietly(fileOut);
//    }
//
//}
