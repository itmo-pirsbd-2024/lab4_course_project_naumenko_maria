package ru.nms.diplom.ircrossfeature.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {

    public static Map<String, List<String>> sortSByScoresAndToList(Map<String,  Map<String, Double>> results, Comparator<Map.Entry<String, Double>> comparator) {
        return results.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .sorted(comparator)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList())
                ));
    }

    public static void normalizeScores(Map<String, Map<String, Double>> queryToDocuments, boolean inverted) {
        for (Map.Entry<String, Map<String, Double>> entry : queryToDocuments.entrySet()) {
            Map<String, Double> docScores = entry.getValue();

            double maxScore = docScores.values().stream().max(Comparator.comparingDouble(v -> v)).get();

            docScores.replaceAll((docId, score) -> inverted ? 1 - (score / maxScore): (score / maxScore));
        }
    }
}
