package com.n256coding.Dev;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.FileManager;

import java.io.InputStream;

public class OntologyTester {
    public static OntModel getOntologyModel(String ontoFile)
    {
        OntModel ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        try
        {
            InputStream in = FileManager.get().open(ontoFile);
            try
            {
                ontoModel.read(in, null);
            }
            catch (Exception e)
            {
                //TODO: Replace with logger
                e.printStackTrace();
            }
            //TODO: Replace with logger
            System.out.println("Ontology " + ontoFile + " loaded.");
        }
        catch (JenaException je)
        {
            //TODO: Replace with logger
            System.err.println("ERROR" + je.getMessage());
            je.printStackTrace();
            System.exit(0);
        }
        return ontoModel;
    }

    public static void queryOntology(){
        String queryString = "select (str(?z) as ?output) {?x ?y ?z}";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, getOntologyModel("D:\\SLIIT\\Year 4 Sem 1\\CDAP\\Research Project\\Resources\\Ontologies\\My_Programming.owl"));
        ResultSet resultSet = queryExecution.execSelect();

        while (resultSet.hasNext()){
            QuerySolution querySolution = resultSet.nextSolution();
            System.out.println(querySolution.getLiteral("output").getString());
        }
    }

    public static void getSubClassesOf(String word){

    }
}
