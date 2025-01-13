package ru.nms.diplom.ircrossfeature.service.ir;

import java.util.*;

import static ru.nms.diplom.ircrossfeature.util.Utils.normalizeScores;

public interface IRService {
    Map<String, Map<String, Double>> knn(List<String> queries, int k);

    Map<String, Double> getSimilarityScoresForDocIds(Set<String> ids, String query);

    default Map<String, Map<String, Double>> enrichWithScores(Map<String, Map<String, Double>> IRResults, Map<String, Map<String, Double>> resultsFromOtherIRApi, List<String> queries) {
        Map<String,  Map<String, Double>> enrichedIRResults = new HashMap<>();
        for (String query: queries) {

            if (!IRResults.containsKey(query) || !resultsFromOtherIRApi.containsKey(query)) throw new RuntimeException("Result for query " + query + " is missing from results");
            Set<String> missingIds = new HashSet<>(resultsFromOtherIRApi.get(query).keySet());
            enrichedIRResults.put(query, new HashMap<>(IRResults.get(query)));
            missingIds.removeAll(IRResults.get(query).keySet());

            enrichedIRResults.get(query).putAll(getSimilarityScoresForDocIds(missingIds, query));
        }

        return enrichedIRResults;
    }

    default Map<String, Map<String, Double>> enrichAndNormalizeResults(Map<String, Map<String, Double>> IRResults, Map<String, Map<String, Double>> resultsFromOtherIRApi, List<String> queries) {
        Map<String, Map<String, Double>> results = enrichWithScores(IRResults, resultsFromOtherIRApi, queries);
        normalizeScores(results, false);
        return results;
    }


}
