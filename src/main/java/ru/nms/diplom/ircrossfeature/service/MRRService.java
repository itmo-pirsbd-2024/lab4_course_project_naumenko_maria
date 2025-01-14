package ru.nms.diplom.ircrossfeature.service;

import java.util.List;
import java.util.Map;

public class MRRService {

    public static double calculateMrr(Map<String, String> relevantPassages, Map<String, List<String>> queryToResultPassages) {
        double mrr = 0;
        for (String query : queryToResultPassages.keySet()) {
            List<String> neighbours = queryToResultPassages.get(query);
            String relevantPassage = relevantPassages.get(query);

            for (int i = 0; i < neighbours.size(); i++) {
                if (neighbours.get(i).replaceAll("\"", "").equals(relevantPassage.replace("\"", ""))) {
                    mrr += (1.0 / (i + 1));
                    break;
                }
            }
        }
        return (mrr / queryToResultPassages.size());
    }
}
