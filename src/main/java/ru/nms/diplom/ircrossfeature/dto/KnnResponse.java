package ru.nms.diplom.ircrossfeature.dto;


import java.util.Map;


public class KnnResponse {

    private Map<String, Double> neighbours;

    public KnnResponse(Map<String, Double> neighbours) {
        this.neighbours = neighbours;
    }

    public KnnResponse() {
    }

    public Map<String, Double> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(Map<String, Double> neighbours) {
        this.neighbours = neighbours;
    }
}
