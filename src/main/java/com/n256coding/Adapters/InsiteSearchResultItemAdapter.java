package com.n256coding.Adapters;

import com.n256coding.DatabaseModels.Resource;
import com.n256coding.Models.InsiteSearchResult;
import com.n256coding.Models.InsiteSearchResultItem;
import com.n256coding.Models.WebSearchResult;


public class InsiteSearchResultItemAdapter extends InsiteSearchResultItem {

    public InsiteSearchResultItemAdapter(WebSearchResult webSearchResult){
        url = webSearchResult.getUrl();
        description = webSearchResult.getDescription();
    }

    public InsiteSearchResultItemAdapter(Resource resource){
        url = resource.url;
        description = resource.url;
    }
}
