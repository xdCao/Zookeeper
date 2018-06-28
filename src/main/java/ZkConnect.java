
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ZkConnect implements Watcher {

    private static Logger logger= LoggerFactory.getLogger(ZkConnect.class);

    private static final String serverPath="127.0.0.1:2181";

    private static final String distirbuteServerPath="127.0.0.1:2182,127.0.0.1:2183,127.0.0.1:2184";


    public static void main(String[] args) throws IOException, InterruptedException {

        ZooKeeper zk=new ZooKeeper(serverPath,5000,new ZkConnect());

        long sessionId = zk.getSessionId();
        byte[] sessionPasswd = zk.getSessionPasswd();


        logger.info("开始连接...");
        logger.info("连接状态: "+zk.getState());

        Thread.sleep(2000);

        logger.info("连接状态: "+zk.getState());

        Thread.sleep(1000);

        logger.info("开始会话重连...");

        ZooKeeper zooKeeper=new ZooKeeper(serverPath,5000,new ZkConnect(),sessionId,sessionPasswd);

        logger.info("连接状态: "+zooKeeper.getState());

        Thread.sleep(2000);

        logger.info("连接状态: "+zooKeeper.getState());

    }

    public void process(WatchedEvent watchedEvent) {
        logger.info("收到watch事件: "+watchedEvent);
    }


}
