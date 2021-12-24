package ru.bmstu;

import akka.actor.ActorRef;
import akka.actor.dsl.Creators;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;

public class ZWatcher implements Watcher {
    private final ActorRef cfg;
    private ZooKeeper zooKeeper;
    private static final String SERVER_PATH = "/servers";

    public  ZWatcher (ActorRef cfg, ZooKeeper zooKeeper) {
        this.cfg = cfg;
        this.zooKeeper = zooKeeper;
    }

    public ZWatcher (ActorRef cfg) {
        this.cfg = cfg;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }


    @Override
    public void process (WatchedEvent event) {
        if (event == null) {
            return;
        }
        Event.KeeperState keeperState = event.getState();
        Event.EventType eventType = event.getType();
        String path = event.getPath();
        if (Event.KeeperState.SyncConnected == keeperState) {
            try {
                List<String> list = zooKeeper.getChildren(SERVER_PATH, this);
                ArrayList<String> serverData = new ArrayList<>();
                for (String name : list) {
                    serverData.add(new String(zooKeeper.getData(SERVER_PATH + '/' + name, this, null)));
                }
                cfg.tell(new ServerList(serverData), ActorRef.noSender());
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
