package ru.nms.diplom.ircrossfeature.dto;

import java.util.Map;

public class SimilarityScoreResponse {
    private Map<String, Double> idsToScore;

    public SimilarityScoreResponse() {
    }

    public SimilarityScoreResponse(Map<String, Double> idsToScore) {
        this.idsToScore = idsToScore;
    }

    public Map<String, Double> getIdsToScore() {
        return idsToScore;
    }

    public void setIdsToScore(Map<String, Double> idsToScore) {
        this.idsToScore = idsToScore;
    }
}
