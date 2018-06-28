import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class NodeOperation implements Watcher {

    private static Logger logger=LoggerFactory.getLogger(NodeOperation.class);

    public static final String serverPath="127.0.0.1:2181";

    public static final int timeout=5000;

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {

        ZooKeeper zooKeeper=new ZooKeeper(serverPath,timeout,new NodeOperation());


        /*同步创建节点*/
        String syncResult = zooKeeper.create("/testJavaSync", "testData".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        logger.info("返回结果: "+syncResult);

        String ctx="{create:node}";
        /*异步创建节点*/
        zooKeeper.create("/testNode","testAsync".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT,new CreateCallback(),ctx);

        Thread.sleep(2000);

        /*同步修改节点数据*/
        Stat stat = zooKeeper.setData("/testNode", "xyz".getBytes(), 0);
        logger.info("修改节点后的status: "+stat);

        /*异步修改节点数据*/
        zooKeeper.setData("/testNode","abc".getBytes(),1,new ModifyCallback(),null);

        Thread.sleep(2000);

        Stat stat1=new Stat();

        /*同步获取节点数据*/
        byte[] data = zooKeeper.getData("/testNode",  true, stat1);

        String s=new String(data);

        logger.info("获取节点数据: "+s);
        logger.info("此时的stat: "+stat1);

        /*同步删除节点*/
        zooKeeper.delete("/testNode",2);
        zooKeeper.delete("/testJavaSync",0);

        /*获取子节点列表*/
        List<String> children = zooKeeper.getChildren("/caohao", new NodeOperation());
        for (String child:children){
            logger.info(child);
        }

        /*判断节点是否存在*/
        Stat exists = zooKeeper.exists("/caohao1", new NodeOperation());

        if (exists!=null){
            logger.info(""+exists);
        }else {
            logger.info("该节点不存在");
        }

        Thread.sleep(2000);



    }


    public void process(WatchedEvent watchedEvent) {
        logger.info("收到watch事件: "+watchedEvent);

        if (watchedEvent.getType()==Event.EventType.NodeChildrenChanged){

            try {
                ZooKeeper zooKeeper=new ZooKeeper(serverPath,timeout,this);
                List<String> children = zooKeeper.getChildren("/caohao", this);
                for (String child:children){
                    logger.info(child);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (KeeperException e) {
                e.printStackTrace();
            }

        }else if (watchedEvent.getType()==Event.EventType.NodeCreated){
            logger.info("Node created");
        }else if (watchedEvent.getType()==Event.EventType.NodeDataChanged){
            logger.info("Data changed");
        }else if (watchedEvent.getType()== Event.EventType.NodeDeleted){
            logger.info("Node deleted");
        }

    }
}
