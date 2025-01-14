package ru.nms.diplom.ircrossfeature.service.ir.impl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.nms.diplom.ircrossfeature.service.ir.IRService;
import ru.nms.diplom.luceneir.service.*;

import java.util.*;
import java.util.stream.Collectors;

public class LuceneIRService implements IRService {

    private final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();
    private final SearchServiceGrpc.SearchServiceBlockingStub stub = SearchServiceGrpc.newBlockingStub(channel);

    @Override
    public Map<String, Map<String, Double>> knn(List<String> queries, int k) {
        Map<String, Map<String, Double>> result = new HashMap<>();
        for (String query: queries) {
            DocumentsResponse documentsResponse = stub.knn(SearchRequest.newBuilder()
                    .setK(k)
                    .setQuery(query)
                    .build());

            documentsResponse.getDocumentsList().forEach(
                    doc -> System.out.println(doc.getScore() + " | " + doc.getId())
            );
            result.put(query, documentsResponse.getDocumentsList().stream().collect(Collectors.toMap(Document::getId, Document::getScore, (v1, v2) -> v1)));
        }
        return result;
    }

    @Override
    public Map<String, Double> getSimilarityScoresForDocIds(Set<String> ids, String query) {
        DocumentsResponse documentsResponse = stub.getSimilarityScores(SimilarityScoreRequest.newBuilder().addAllDocId(ids).setQuery(query).build());

        return documentsResponse.getDocumentsList().stream().collect(Collectors.toMap(Document::getId, Document::getScore));
    }

}
