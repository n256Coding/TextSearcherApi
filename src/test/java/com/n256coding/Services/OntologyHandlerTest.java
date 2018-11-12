package com.n256coding.Services;

import com.n256coding.Common.Environments;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class OntologyHandlerTest {
    OntologyHandler ontologyHandler;

    @Before
    public void setUp() throws Exception {
        ontologyHandler = new OntologyHandler(FileHandler.ONTOLOGY_FILE_PATH, Environments.ONTOLOGY_BASE_URL);
    }

    @After
    public void tearDown() throws Exception {
        ontologyHandler = null;
    }

    @Test
    public void getSubWordsOf() throws Exception {
        List<String> subWords = ontologyHandler.getSubWordsOf("object oriented concept", 8);
        assertNotNull(subWords);
        assertTrue(subWords.contains("inheritance"));
    }

    @Test
    public void getEquivalentWords() throws Exception {
        List<String> equivalentWords = ontologyHandler.getEquivalentWords("threading");
        assertTrue(equivalentWords.contains("thread"));
    }

}