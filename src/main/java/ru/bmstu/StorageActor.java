package ru.bmstu;

import akka.actor.AbstractActor;
import java.util.*;

public class StorageActor extends AbstractActor {
    private ArrayList<String> data = new ArrayList<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().create()
                .match(ServerRequest.class, this::redirect)
                .match(ServerList.class, this::saveServerList)
                .build();
    }

    private void redirect(ServerRequest request) {
        Random rand = new Random();
        int target = rand.nextInt(data.size());
        sender().tell(data.get(target), getSelf());
    }

    private void saveServerList (ServerList list) {
        data = new ArrayList<>(list.getData());
        System.out.println(data);
    }
}
