package com.n256coding.Helpers;

import com.n256coding.Models.InsiteSearchResultItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortHelper {
    public List<InsiteSearchResultItem> sortSearchResultsWithTfIDF(List<InsiteSearchResultItem> results) {
        Collections.sort(results, new Comparator<InsiteSearchResultItem>() {
            @Override
            public int compare(InsiteSearchResultItem o1, InsiteSearchResultItem o2) {
                return Double.compare(o2.getTf_idf(), o1.getTf_idf());
            }
        });
        return results;
    }
}
