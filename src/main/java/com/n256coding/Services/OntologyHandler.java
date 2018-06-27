package com.n256coding.Services;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OntologyHandler {
    private OntModel ontoModel;
    private String ontologyBaseUrl;
    private TextAnalyzer textAnalyzer;

    public OntologyHandler(String ontologyPath, String ontologyBaseUrl) {
        loadOntology(ontologyPath, ontologyBaseUrl);
    }

    public void loadOntology(String ontologyPath, String ontologyBaseUrl) {
        this.ontologyBaseUrl = ontologyBaseUrl;
        ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        InputStream in = FileManager.get().open(ontologyPath);
        ontoModel.read(in, null);
    }

    public List<String> getSubWordsOf(String word, int resultLimit) {
        word = word.toLowerCase();
        List<String> subWordList = new ArrayList<>();
        String queryString = "PREFIX progonto: <" + ontologyBaseUrl + "#>\n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "\n" +
                "select (str(?subclass) as ?output) where {\n" +
                "  ?subclass rdfs:subClassOf* progonto:" + word + "\n" +
//                "   ?subclass2 rdfs:subClassOf* ?subclass" +
                "}";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, ontoModel);
        ResultSet resultSet = queryExecution.execSelect();

        for (int i = 0; resultSet.hasNext(); i++) {
            if (i == 0)
                continue;
            String result = resultSet
                    .nextSolution()
                    .getLiteral("output")
                    .getString()
                    .replace(ontologyBaseUrl.concat("#"), "");
            subWordList.add(result);
        }
        return subWordList;
    }
}
