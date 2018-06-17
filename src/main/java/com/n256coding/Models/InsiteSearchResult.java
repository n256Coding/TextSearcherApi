package com.n256coding.Models;

import java.util.ArrayList;
import java.util.List;

public class InsiteSearchResult {
    protected List<InsiteSearchResultItem> resultItems;
    protected int count;
    protected String spellCorrectedQuery;
    protected String originalQuery;

    public InsiteSearchResult() {
        resultItems = new ArrayList<>();
    }

    public List<InsiteSearchResultItem> getResultItems() {
        return resultItems;
    }

    public void addResultItem(InsiteSearchResultItem resultItem) {
        this.resultItems.add(resultItem);
    }

    public int getCount() {
        return resultItems.size();
    }

    public String getSpellCorrectedQuery() {
        return spellCorrectedQuery;
    }

    public void setSpellCorrectedQuery(String spellCorrectedQuery) {
        this.spellCorrectedQuery = spellCorrectedQuery;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }
}
