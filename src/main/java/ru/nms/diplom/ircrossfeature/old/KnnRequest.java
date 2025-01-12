package ru.nms.diplom.ircrossfeature.old;


public class KnnRequest {

    private String query;
    private int k;
    private boolean normalized;

    public KnnRequest() {
    }

    public KnnRequest(String query, int k, boolean normalized) {
        this.query = query;
        this.k = k;
        this.normalized = normalized;
    }


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public boolean isNormalized() {
        return normalized;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    @Override
    public String toString() {
        return "KnnRequest{" +
                "query='" + query + '\'' +
                ", k=" + k +
                ", normalized=" + normalized +
                '}';
    }
}
