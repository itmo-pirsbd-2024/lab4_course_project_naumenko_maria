package ru.nms.diplom.ircrossfeature.service;

import io.grpc.stub.StreamObserver;
import ru.nms.diplom.ircrossfeature.service.ir.IRService;
import ru.nms.diplom.ircrossfeature.service.ir.impl.FaissIRService;
import ru.nms.diplom.ircrossfeature.service.ir.impl.LuceneIRService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static ru.nms.diplom.ircrossfeature.service.DataFrameLoader.loadRelevantPassages;
import static ru.nms.diplom.ircrossfeature.service.MRRService.calculateMrr;
import static ru.nms.diplom.ircrossfeature.util.Utils.sortSByScoresAndToList;

public class CrossFeatureServiceImpl extends CrossFeatureServiceGrpc.CrossFeatureServiceImplBase {

    private final IRService luceneIRService = new LuceneIRService();
    private final IRService faissIRService = new FaissIRService();
    @Override
    public void calculateMRR(CrossFeatureRequest request, StreamObserver<CrossFeatureResponse> responseObserver) {
        Map<String, String> relevantPassages = loadRelevantPassages("D:\\diplom\\shared\\relevant_passages.csv");
        List<String> queries = relevantPassages.keySet().stream().limit(request.getQueriesAmount()).collect(Collectors.toList());

        CompletableFuture<Map<String, Map<String, Double>>> luceneFuture = CompletableFuture.supplyAsync(() -> luceneIRService.knn(queries, request.getK()));
        CompletableFuture<Map<String, Map<String, Double>>> faissFuture = CompletableFuture.supplyAsync(() -> faissIRService.knn(queries, request.getK()));

        CompletableFuture<Void> allDone = CompletableFuture.allOf(luceneFuture, faissFuture);

        allDone.join();
        Map<String, Map<String, Double>> luceneResults;
        Map<String, Map<String, Double>> faissResults;
        try {
            luceneResults = luceneFuture.get();
            System.out.println("Successfully got result from lucene");
            faissResults = faissFuture.get();
            System.out.println("Successfully got result from faiss");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("error occured while getting result from IR services", e);
        }

        luceneFuture = CompletableFuture.supplyAsync(() -> luceneIRService.enrichAndNormalizeResults(luceneResults, faissResults, queries));
        faissFuture = CompletableFuture.supplyAsync(() -> faissIRService.enrichAndNormalizeResults(faissResults, luceneResults, queries));

        allDone = CompletableFuture.allOf(luceneFuture, faissFuture);
        allDone.join();
        Map<String, Map<String, Double>> enrichedAndNormalizedLuceneResults;
        Map<String, Map<String, Double>> enrichedAndNormalizedFaissResults;

        try {
            enrichedAndNormalizedLuceneResults = luceneFuture.get();
            System.out.println("Successfully enriched result from lucene");
            enrichedAndNormalizedFaissResults = faissFuture.get();
            System.out.println("Successfully enriched result from faiss");
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("error occured while enriching results from IR services", e);
        }
        Map<String, List<String>> resultFromFaiss = sortSByScoresAndToList(enrichedAndNormalizedFaissResults, Map.Entry.<String, Double>comparingByValue().reversed());
        Map<String, List<String>> resultFromLucene = sortSByScoresAndToList(enrichedAndNormalizedLuceneResults, Map.Entry.<String, Double>comparingByValue().reversed() );

        double faissMrr = calculateMrr(relevantPassages, resultFromFaiss);
        double bm25MRR = calculateMrr(relevantPassages, resultFromLucene);

        double mrrWithRRF = calculateMrr(relevantPassages, combineViaRRF(resultFromFaiss, resultFromLucene, queries, request.getK()));
        double mrrWithRSF = calculateMrr(relevantPassages, combineViaRSF(enrichedAndNormalizedFaissResults, enrichedAndNormalizedLuceneResults, queries));

        CrossFeatureResponse response = CrossFeatureResponse.newBuilder()
                .setBm25MRR(bm25MRR)
                .setFaissMRR(faissMrr)
                .setMrrWithRRF(mrrWithRRF)
                .setMrrWithRSF(mrrWithRSF)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


//    void enrichWithScores(Map<String,  Map<String, Double>> faissResults, Map<String,  Map<String, Double>> luceneResults, List<String> queries) {
//        Map<String, List<String>> docsAbsentInFaissResult = new HashMap<>();
//        Map<String, List<String>> docsAbsentInLuceneResult = new HashMap<>();
//        for (String query: queries) {
//            if (!faissResults.containsKey(query) || !luceneResults.containsKey(query)) throw new RuntimeException("Result for query " + query + " is missing from results");
//
//        }
//    }
    void enrichWithScores(Map<String,  Map<String, Double>> faissResults, Map<String,  Map<String, Double>> luceneResults, List<String> queries) {
        Map<String, Set<String>> faissMissing = new HashMap<>();
        for (String query: queries) {
            if (!faissResults.containsKey(query) || !luceneResults.containsKey(query)) throw new RuntimeException("Result for query " + query + " is missing from results");
            Set<String> missingIds = new HashSet<>(luceneResults.get(query).keySet());
            missingIds.removeAll(faissResults.get(query).keySet());
            faissMissing.put(query, missingIds);
        }
    }



    //reciprocal rank fusion
    private Map<String, List<String>> combineViaRRF(Map<String,  List<String>> resultFromFaiss, Map<String,  List<String>> resultFromBM25, List<String> queries, int k) {
        Map<String, List<String>> combinedQueryToPassages = new HashMap<>();
        for(String query: queries) {
            List<String> faissList = resultFromFaiss.get(query);
            List<String> bm25List = resultFromBM25.get(query);
            var combinedNeighbours = reorderNeighboursViaRRF(faissList, bm25List, k, 60);
            combinedQueryToPassages.put(query, combinedNeighbours);
        }
        return combinedQueryToPassages;
    }

    //relative score fusion
    private Map<String, List<String>> combineViaRSF(Map<String,  Map<String, Double>> resultFromFaiss, Map<String,  Map<String, Double>> resultFromBM25, List<String> queries) {
        Map<String, List<String>> combinedQueryToPassages = new HashMap<>();
        for(String query: queries) {
            var faissList = resultFromFaiss.get(query);
            var bm25List = resultFromBM25.get(query);
            var combinedNeighbours = reorderNeighboursViaRSF(faissList, bm25List);
            combinedQueryToPassages.put(query, combinedNeighbours);
        }
        return combinedQueryToPassages;
    }

    private List<String> reorderNeighboursViaRSF(Map<String, Double> neighboursFromFaiss, Map<String, Double> neighboursFromBM25) {

        // Add all entries from the first map
        Map<String, Double> combinedMap = new HashMap<>(neighboursFromFaiss);

        // Add all entries from the second map, summing values if key already exists
        for (Map.Entry<String, Double> entry : neighboursFromBM25.entrySet()) {
            combinedMap.merge(entry.getKey(), entry.getValue(), Double::sum);
        }

        // Sort the combined map by values in descending order and return the keys as a list
        return combinedMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private List<String> reorderNeighboursViaRRF(List<String> neighboursFromFaiss, List<String> neighboursFromBM25, int k, int alfa) {
        Map<String, Double> passageToRank = new HashMap<>();
        for(int i = 0; i < neighboursFromFaiss.size(); i++) {
            int bm25Ind = neighboursFromBM25.indexOf(neighboursFromFaiss.get(i));
//            if (bm25Ind == -1) continue;
            bm25Ind = bm25Ind == -1 ? k - 1 : bm25Ind;
            double RRFScore = (double) (1.0 / (i + 1 + alfa) + 1.0 / (bm25Ind + 1 + alfa));
            passageToRank.put(neighboursFromFaiss.get(i), RRFScore);
        }
        for(int i = 0; i < neighboursFromBM25.size(); i++) {
            if(passageToRank.containsKey(neighboursFromBM25.get(i))) continue;
            int faissInd = neighboursFromFaiss.indexOf(neighboursFromBM25.get(i));
            faissInd = faissInd == -1 ? k - 1 : faissInd;
            double RRFScore = (double) (1.0 / (i + 1 + alfa) + (faissInd == -1 ? 0 : 1.0 / (faissInd + 1 + alfa)));
            passageToRank.put(neighboursFromBM25.get(i), RRFScore);
        }
        return passageToRank.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }
}
