package ru.nms.diplom.ircrossfeature.old;


import java.util.Map;


public class KnnResponse {

    private Map<String, Float> neighbours;

    public KnnResponse(Map<String, Float> neighbours) {
        this.neighbours = neighbours;
    }

    public KnnResponse() {
    }

    public Map<String, Float> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(Map<String, Float> neighbours) {
        this.neighbours = neighbours;
    }
}
