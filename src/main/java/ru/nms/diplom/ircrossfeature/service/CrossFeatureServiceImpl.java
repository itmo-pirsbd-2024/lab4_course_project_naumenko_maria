package ru.nms.diplom.ircrossfeature.service;

import io.grpc.stub.StreamObserver;
import ru.nms.diplom.ircrossfeature.old.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.nms.diplom.ircrossfeature.old.BM25Type.BM25;
import static ru.nms.diplom.ircrossfeature.service.DataFrameLoader.loadRelevantPassages;

public class CrossFeatureServiceImpl extends CrossFeatureServiceGrpc.CrossFeatureServiceImplBase {

    private final  LuceneIRService luceneIRService = new LuceneIRService();
    private final PythonServerClient pythonServerClient = new PythonServerClient();

    @Override
    public void calculateMRR(CrossFeatureRequest request, StreamObserver<CrossFeatureResponse> responseObserver) {
        Map<String, String> relevantPassages = loadRelevantPassages("D:\\diplom\\shared\\relevant_passages.csv");
        List<String> queries = relevantPassages.keySet().stream().limit(request.getQueriesAmount()).collect(Collectors.toList());


        float startTime = System.currentTimeMillis();
        Map<String, Map<String, Float>> topKBM25Documents = luceneIRService.knnBM25(queries, request.getK());
        System.out.println("it took " + (System.currentTimeMillis() - startTime) + " milliseconds to find neighbourc in lucene");
        //old
        startTime = System.currentTimeMillis();

//        Map<String, Map<String, Float>> resultFromFaissWithScores = getResultFromFaiss(request.getK(), queries, true);
        System.out.println("it took " + (System.currentTimeMillis() - startTime) + " milliseconds to find neighbourc in faiss");

        startTime = System.currentTimeMillis();

//        Map<String, Map<String, Float>> resultFromBM25WithScores = getResultFromBm25(request.getK(), queries, true, BM25);
        System.out.println("it took " + (System.currentTimeMillis() - startTime) + " milliseconds to find neighbourc in bm25 lib");

//            Comparator<Double> comparator = Comparator.reverseOrder();
//        Map<String, List<String>> resultFromFaiss = sortSByScoresAndToList(resultFromFaissWithScores);
//        Map<String, List<String>> resultFromBM25 = sortSByScoresAndToList(resultFromBM25WithScores);
//        float faissMrr = calculateMrr(relevantPassages, resultFromFaiss);
//        float bm25Mrr = calculateMrr(relevantPassages, resultFromBM25);
        //
        Map<String, List<String>> resultFromLucene = sortSByScoresAndToList(topKBM25Documents);

        float bm25MRR = calculateMrr(relevantPassages, resultFromLucene);

        CrossFeatureResponse response = CrossFeatureResponse.newBuilder()
                .setBm25MRR(bm25MRR)
//                .setFaissMRR(faissMrr)
//                .setMrrWithRRF(bm25Mrr)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Map<String, List<String>> sortSByScoresAndToList(Map<String,  Map<String, Float>> results) {
        return results.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList())
                ));
    }

    private float calculateMrr(Map<String, String> relevantPassages, Map<String, List<String>> queryToResultPassages) {
        double mrr = 0;
        for (String query : queryToResultPassages.keySet()) {
            List<String> neighbours = queryToResultPassages.get(query);
            String relevantPassage = relevantPassages.get(query);

            for (int i = 0; i < neighbours.size(); i++) {
                if (neighbours.get(i).replaceAll("\"", "").equals(relevantPassage.replace("\"", ""))) {
                    mrr +=  (1.0 / (i + 1));
                    break;
                }
            }
        }
        return (float) (mrr / queryToResultPassages.size());
    }

    private Map<String, Map<String, Float>> getResultFromBm25(int k, List<String> queries, boolean normalized, BM25Type type) {
        Map<String, Map<String, Float>> queryToResultPassages = new HashMap<>();

        for (String query  :queries) {
            BM25KnnRequest knnRequest = new BM25KnnRequest(query, k, normalized, type);
            KnnResponse knnResponse = pythonServerClient.searchBM25(knnRequest);
            if (knnResponse == null) continue;
            Map<String, Float> neighbours =  knnResponse.getNeighbours();
            queryToResultPassages.put(query, neighbours);
        }
        return queryToResultPassages;
    }

    private Map<String, Map<String, Float>> getResultFromFaiss(int k, List<String> queries, boolean normalized) {
        Map<String, Map<String, Float>> queryToResultPassages = new HashMap<>();

        for (String query  :queries) {
            KnnRequest knnRequest = new KnnRequest(query, k, normalized);
            KnnResponse knnResponse = pythonServerClient.searchVector(knnRequest);
            if (knnResponse == null) continue;
            Map<String, Float> neighbours =  knnResponse.getNeighbours();
            queryToResultPassages.put(query, neighbours);
        }
        return queryToResultPassages;
    }
}
