package ru.nms.diplom.ircrossfeature.service.ir.impl;

import ru.nms.diplom.ircrossfeature.dto.KnnRequest;
import ru.nms.diplom.ircrossfeature.dto.KnnResponse;
import ru.nms.diplom.ircrossfeature.client.PythonServerClient;
import ru.nms.diplom.ircrossfeature.dto.SimilarityScoreRequest;
import ru.nms.diplom.ircrossfeature.service.DataFrameLoader;
import ru.nms.diplom.ircrossfeature.service.ir.IRService;

import java.util.*;

import static ru.nms.diplom.ircrossfeature.util.Utils.normalizeScores;

public class FaissIRService implements IRService {

    private final Map<String, String> idsToPassages;
    private final PythonServerClient pythonServerClient = new PythonServerClient();

    public FaissIRService() {
        System.out.println("started loading passages");
        this.idsToPassages = DataFrameLoader.loadAllPassages("D:\\diplom\\shared\\all_passages.csv");
        System.out.println("finished loading passages, loaded total of " + idsToPassages.size());
    }

    @Override
    public Map<String, Map<String, Double>> knn(List<String> queries, int k) {
        Map<String, Map<String, Double>> queryToResultPassages = new HashMap<>();

        for (String query : queries) {
            KnnRequest knnRequest = new KnnRequest(query, k, false);
            KnnResponse knnResponse = pythonServerClient.searchVector(knnRequest);
            if (knnResponse == null) continue;
            Map<String, Double> neighbours = knnResponse.getNeighbours();
            queryToResultPassages.put(query, neighbours);
        }
        return queryToResultPassages;
    }

    @Override
    public Map<String, Double> getSimilarityScoresForDocIds(Set<String> ids, String query) {
        Map<String, String> idsToTexts = new HashMap<>();
        for (String id: ids) {
            idsToTexts.put(id, idsToPassages.get(id)
            );
        }
        return pythonServerClient.getSimilarityScoresFromFaiss(new SimilarityScoreRequest(query, idsToTexts))
                .getIdsToScore();
    }

    @Override
    public Map<String, Map<String, Double>> enrichAndNormalizeResults(Map<String, Map<String, Double>> IRResults, Map<String, Map<String, Double>> resultsFromOtherIRApi, List<String> queries) {
        Map<String, Map<String, Double>> results = enrichWithScores(IRResults, resultsFromOtherIRApi, queries);
        normalizeScores(results, true);
        return results;
    }
}
