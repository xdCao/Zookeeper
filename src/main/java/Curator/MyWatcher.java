package Curator;

import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWatcher implements CuratorWatcher {

    private static Logger logger=LoggerFactory.getLogger(MyWatcher.class);

    public void process(WatchedEvent watchedEvent) {

        logger.info("监听到watcher事件: "+watchedEvent);

    }


}
