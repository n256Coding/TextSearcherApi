package com.n256coding.Models;

import com.n256coding.Common.Enviorenments;
import com.n256coding.Database.MongoDbConnection;
import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Interfaces.DatabaseConnection;

public class TfIdfData {
    private String documentId;
    private String keyword;
    private double tfIdfValue;
    private double weightedValue;
    private DatabaseConnection database;

    public TfIdfData(String documentId, String keyword, double tfIdfValue, double weightedValue) {
        this.documentId = documentId;
        this.keyword = keyword;
        this.tfIdfValue = tfIdfValue;
        this.weightedValue = weightedValue;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public double getTfIdfValue() {
        return tfIdfValue;
    }

    public void setTfIdfValue(double tfIdfValue) {
        this.tfIdfValue = tfIdfValue;
    }

    public double getWeightedTfIdfValue(){
        return this.tfIdfValue * this.weightedValue;
    }

    public Resource getDbResource(){
        database = new MongoDbConnection(Enviorenments.MONGO_DB_HOSTNAME);
        return database.getResourceById(this.documentId);
    }
}
