package ru.nms.diplom.ircrossfeature;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureServiceImpl;
import ru.nms.diplom.ircrossfeature.service.LuceneIRService;
import ru.nms.diplom.luceneir.service.SearchRequest;
import ru.nms.diplom.luceneir.service.SearchResponse;
import ru.nms.diplom.luceneir.service.SearchServiceGrpc;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.nms.diplom.ircrossfeature.service.DataFrameLoader.loadRelevantPassages;

public class Main {

    public static void main(String[] args) {
        Server server = ServerBuilder
                .forPort(8081)
                .addService(new CrossFeatureServiceImpl()).build();

        try {
            server.start();
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("did not manage to start server", e);
        }
    }
}
