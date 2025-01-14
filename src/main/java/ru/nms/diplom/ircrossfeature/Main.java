package ru.nms.diplom.ircrossfeature;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ru.nms.diplom.ircrossfeature.service.CrossFeatureServiceImpl;

import java.io.IOException;

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
