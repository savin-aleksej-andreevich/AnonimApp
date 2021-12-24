package ru.bmstu;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;

public class ZAnonimApp {
    private static List<ServerNode> serverNodes;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        serverNodes = new ArrayList<>();
    }
}
