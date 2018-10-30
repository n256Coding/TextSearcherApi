package com.n256coding.Services;

public class OrailyDownloader {
    public void download() {
//        for (FreeEbook ebook : ebooksResult) {
//            WebSearchResult searchResult = new WebSearchResult();
//            if (ebook.getPdf() == null)
//                continue;
//            searchResult.setUrl(ebook.getPdf());
//            searchResult.setDescription(ebook.getDescription());
//            searchResult.setPdf(true);
//
//            List<KeywordData> keywordDataList = new ArrayList<>();
//            Resource resource = new Resource();
//            resource.setUrl(ebook.getPdf());
//            resource.setPdf(true);
//            resource.setTitle(ebook.getTitle());
//            resource.setImageUrl(ebook.getThumbnail());
//            resource.setLastModified(new Date());
//            resource.setDescription(ebook.getDescription());
//
//            List<Map.Entry<String, Integer>> frequencies = new ArrayList<>();
//            int wordCount = 0;
//            try {
//                String tempPage = searchResult.getUrlContent();
//                tempPage = textFilter.replaceWithLemmas(tempPage);
//                wordCount = textAnalyzer.getWordCount(tempPage);
//                frequencies = textAnalyzer.getWordFrequency(tempPage);
//
//            } catch (BoilerpipeProcessingException e) {
//                e.printStackTrace();
//            } catch (SAXException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            //Here the keyword frequencies are reduced with a limit
//            //Current limit is 10
//            for (int j = 0, k = 0; k < 20 && j < frequencies.size(); j++, k++) {
//                Map.Entry<String, Integer> frequency = frequencies.get(j);
//                if (stopWordHelper.isStopWord(frequency.getKey())) {
//                    k--;
//                    continue;
//                }
//                KeywordData keywordData = new KeywordData(frequency.getKey(),
//                        frequency.getValue(),
//                        ((double) frequency.getValue() / (double) wordCount));
//                keywordDataList.add(keywordData);
//            }
//            resource.setKeywords(keywordDataList.toArray(new KeywordData[keywordDataList.size()]));
//            //Store or update that information in database.
//            database.addResource(resource);
    }
}
