package com.n256coding.Dev;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.CoreMap;
import org.springframework.stereotype.Indexed;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class NLPTest {
    //https://stackoverflow.com/questions/4774185/how-to-get-relationship-between-words-with-nlp-stanford-parser
    public void testNLP(){
//        LexicalizedParser lp = new LexicalizedParser("englishPCFG.ser.gz", );
//        Tree parse = (Tree) lp.apply("The screen is very good");
//
//        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
//        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
//        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
//        Collection tdl = gs.typedDependenciesCollapsed();
//        System.out.println(tdl);
    }


//    https://stackoverflow.com/questions/31858305/relationship-extraction-using-stanford-corenlp?rq=1
public String content = "John Smith only eats an apple and a banana for lunch. He's on a diet and his mother told him that it would be very healthy to eat less for lunch. John doesn't like it at all but since he's very serious with his diet, he doesn't want to stop.";
    public void nlpTest2(){
        // set up properties
        Properties props = new Properties();
        props.setProperty("ssplit.eolonly","true");
        props.setProperty("annotators",
                "tokenize, ssplit, pos, depparse");
        // set up pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // get contents from file

        System.out.println(content);
        // read in a product review per line
        Annotation annotation = new Annotation(content);
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            System.out.println("---");
            System.out.println("sentence: "+sentence);
            SemanticGraph tree = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
            System.out.println(tree.toString(SemanticGraph.OutputFormat.READABLE));
        }
    }

    //https://stackoverflow.com/questions/8169827/using-dependency-parser-in-stanford-corenlp
    public void nlpTest3(){
        // set up properties
        Properties props = new Properties();
        props.setProperty("ssplit.eolonly","true");
        props.setProperty("annotators",
                "tokenize, ssplit, pos, depparse");
        // set up pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // get contents from file

        //System.out.println(content);
        // read in a product review per line
        Annotation annotation = new Annotation("Draw an angular web page with material design.");
        pipeline.annotate(annotation);

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            System.out.println("---");
            System.out.println("sentence: "+sentence);
            SemanticGraph tree = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            IndexedWord root = tree.getFirstRoot();
            recursiveRelation(tree, root);

        }
    }

    public void recursiveRelation(SemanticGraph tree, IndexedWord word){
        List<IndexedWord> words = tree.getChildList(word);
        System.out.println("Root: "+word);
        System.out.println(words);
        for (IndexedWord indexedWord : words) {
            recursiveRelation(tree, indexedWord);
        }
    }
}
