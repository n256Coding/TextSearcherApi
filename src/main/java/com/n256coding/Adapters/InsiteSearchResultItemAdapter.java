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
        _id = resource.getId();
        url = resource.getUrl();
        description = resource.getDescription();
        title = resource.getTitle();
        imageUrl = resource.getImageUrl() == null ? "" : resource.getImageUrl();
    }
}
