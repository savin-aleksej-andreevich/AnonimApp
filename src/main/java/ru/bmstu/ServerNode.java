package ru.bmstu;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

public class ServerNode extends AllDirectives {
    private static ActorRef cfg;
    private static Integer port;
    private static ActorSystem system;
    private static ZWatcher watcher;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        port = Integer.parseInt(args[0]);
        system = ActorSystem.create("routes");
        //url = args[0]; system = ActorSystem.create("routes");
        cfg = system.actorOf(Props.create(StorageActor.class));
        watcher = new ZWatcher(cfg);
        ZooKeeper zooKeeper = new ZooKeeper("127.0.0.1:2181", 3000, watcher);
        watcher.setZooKeeper(zooKeeper);
        zooKeeper.create("/servers/s",
                port.toString().getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        ServerNode instance = new ServerNode();
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow;
        routeFlow = instance.createRoute(system).flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", port),
                materializer
        );
        System.out.println(String.format("Server online at http://localhost:%d/\nPress RETURN to stop...", port));
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    private Route createRoute (ActorSystem system) {
        return route(
                get()
        );
    }

    private Route get() {
        return parametr("url", url ->
                parameter()
        )
    }
}
