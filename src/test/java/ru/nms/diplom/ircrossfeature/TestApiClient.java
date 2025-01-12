package ru.nms.diplom.ircrossfeature;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureRequest;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureResponse;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureServiceGrpc;

public class TestApiClient {

    private static final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8081)
            .usePlaintext()
            .build();
    private static final CrossFeatureServiceGrpc.CrossFeatureServiceBlockingStub stub = CrossFeatureServiceGrpc.newBlockingStub(channel);
    public static void main(String[] args) {

        CrossFeatureRequest request = CrossFeatureRequest.newBuilder()
                .setK(100)
                .setQueriesAmount(1000)
                .build();

        CrossFeatureResponse response = stub.calculateMRR(request);
        System.out.println("faiss MRR: " + response.getFaissMRR());
        System.out.println("bm25 MRR: " + response.getMrrWithRRF());
        System.out.println("lucene MRR: " + response.getBm25MRR());
    }
}
