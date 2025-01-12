package ru.nms.diplom.ircrossfeature.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.nms.diplom.luceneir.service.Document;
import ru.nms.diplom.luceneir.service.SearchRequest;
import ru.nms.diplom.luceneir.service.SearchResponse;
import ru.nms.diplom.luceneir.service.SearchServiceGrpc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LuceneIRService {

    private final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();
    private final SearchServiceGrpc.SearchServiceBlockingStub stub = SearchServiceGrpc.newBlockingStub(channel);
    public Map<String, Map<String, Float>> knnBM25(List<String> queries, int k) {

        Map<String, Map<String, Float>> result = new HashMap<>();
        for (String query: queries) {
            SearchResponse helloResponse = stub.knn(SearchRequest.newBuilder()
                    .setK(k)
                    .setQuery(query)
                    .build());

        helloResponse.getDocumentsList().forEach(
                doc -> System.out.println(doc.getScore() + " | " + doc.getContent())
        );
            result.put(query, helloResponse.getDocumentsList().stream().collect(Collectors.toMap(Document::getContent, Document::getScore, (v1, v2) -> v1)));
        }
        shutDown();
        return result;
    }

    private void shutDown() {
        channel.shutdown();
    }
}
