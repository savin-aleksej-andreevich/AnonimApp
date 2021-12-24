package ru.bmstu;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ZWatcher implements Watcher {
    private final ActorRef cfg;
    private ZooKeeper zooKeeper;
}
