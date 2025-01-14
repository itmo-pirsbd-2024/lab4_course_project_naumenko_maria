package ru.nms.diplom.ircrossfeature.dto;

import java.util.Map;

public class SimilarityScoreRequest {
    private String query;
    private Map<String, String> idsToText;

    public SimilarityScoreRequest() {
    }

    public SimilarityScoreRequest(String query, Map<String, String> idsToText) {
        this.query = query;
        this.idsToText = idsToText;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, String> getIdsToText() {
        return idsToText;
    }

    public void setIdsToText(Map<String, String> idsToText) {
        this.idsToText = idsToText;
    }

    @Override
    public String toString() {
        return "SimilarityScoreRequest{" +
                "query='" + query + '\'' +
                ", idsToText=" + idsToText +
                '}';
    }
}
