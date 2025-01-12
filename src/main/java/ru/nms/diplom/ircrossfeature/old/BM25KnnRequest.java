package ru.nms.diplom.ircrossfeature.old;

public class BM25KnnRequest extends KnnRequest{
    private BM25Type type;

    public BM25KnnRequest(String query, int k, boolean normalized, BM25Type type) {
        super(query, k, normalized);
        this.type = type;
    }

    public BM25KnnRequest() {
    }

    public BM25Type getType() {
        return type;
    }

    public void setType(BM25Type type) {
        this.type = type;
    }
}
