package com.n256coding.Services;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.8
 * @author Nishan
 */
public class OntologyHandler {
    private OntModel ontoModel;
    private String ontologyBaseUrl;

    public OntologyHandler(String ontologyPath, String ontologyBaseUrl) {
        loadOntology(ontologyPath, ontologyBaseUrl);
    }

    public void loadOntology(String ontologyPath, String ontologyBaseUrl) {
        this.ontologyBaseUrl = ontologyBaseUrl;
        ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        InputStream in = FileManager.get().open(ontologyPath);
        ontoModel.read(in, null);
    }


    /**
     * @apiNote Get sub-words of given word, Ex. Object Oriented Concepts -> Polymorphism, Inheritance, Abstraction ..
     * @param word keyword that needs to find subkeywords of
     * @param resultLimit maximum number of results needs to return
     * @return sub-keywords
     */
    public List<String> getSubWordsOf(String word, int resultLimit) {
        word = word
                .toLowerCase()
                .replace(" ", "_");
        String queryString = "PREFIX progonto: <" + ontologyBaseUrl + "#>\n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "select (str(?subclass) as ?output) where {\n" +
                "  ?subclass rdfs:subClassOf* progonto:" + word + "\n" +
                "}";
        return executeRequest(queryString, resultLimit, word);
    }


    /**
     * @param word keyword that needs to find similar words of
     * @return equivalent words
     */
    public List<String> getEquivalentWords(String word) {
        word = word
                .toLowerCase()
                .replace(" ", "_");
        String queryString = "PREFIX progonto: <" + ontologyBaseUrl + "#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "select (str(?word) as ?output) where {\n" +
                "  ?word owl:equivalentClass progonto:" + word + " .\n" +
                "}";
        return executeRequest(queryString, 99999, word);
    }


    /**
     * @apiNote Executes the query to read ontology
     * @param queryString query to read ontology
     * @param originalWord original word or phrase that was input into the ontology
     * @return
     */
    private List<String> executeRequest(String queryString, int resultLimit, String originalWord){
        List<String> outputWords = new ArrayList<>();
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, ontoModel);
        ResultSet resultSet = queryExecution.execSelect();

        for (int i=0; resultSet.hasNext() && i < resultLimit; i++) {
            String result = resultSet
                    .nextSolution()
                    .getLiteral("output")
                    .getString()
                    .replace(ontologyBaseUrl.concat("#"), "");
            if(!result.equalsIgnoreCase(originalWord)){
                outputWords.add(result.replace("_", " "));
            }
        }
        return outputWords;
    }

}
