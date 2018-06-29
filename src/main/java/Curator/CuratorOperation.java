package Curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CuratorOperation {

    private static Logger logger=LoggerFactory.getLogger(CuratorOperation.class);

    private CuratorFramework client=null;

    private static final String serverPath="127.0.0.1:2181";

    public CuratorOperation() {


        /*
        * baseSleepTimeMs:初始sleep的时间
        * maxRetries:最大重试次数
        * maxSleepMs:最大重试时间
        * */
        RetryPolicy retryPolicy=new ExponentialBackoffRetry(1000,3,1000);

        /*n:重试次数
        * sleepMsBetweenRetries:重试间隔时间
        * */
//        RetryPolicy retryPolicy=new RetryNTimes(3,1000);

        /*重试一次,不推荐*/
//        RetryPolicy retryPolicy=new RetryOneTime(1000);

        /*永远重试,不推荐*/
//        RetryPolicy retryPolicy=new RetryForever(1000);

        client=CuratorFrameworkFactory.builder()
                .connectString(serverPath)
                .sessionTimeoutMs(20000)
                .retryPolicy(retryPolicy)
                .namespace("myWork")
                .build();

        client.start();



    }

    public void close(){
        if (client!=null){
            client.close();
        }
    }

    public  CuratorFramework getClient() {
        return client;
    }

    public static void main(String[] args) throws Exception {

        CuratorOperation curatorOperation=new CuratorOperation();

        logger.info("当前客户端状态: "+curatorOperation.getClient().isStarted());

        /*创建节点*/
        curatorOperation.getClient().
                create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath("/curator/me","caohao".getBytes());

        curatorOperation.getClient().
                create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath("/curator/he","caohao".getBytes());

        /*设置watcher,使用usingWatcher时只会触发监听一次,监听完毕后会销毁*/
        curatorOperation.getClient().getData().usingWatcher(new MyWatcher()).forPath("/curator/me");

        /*NodeCache:监听Node节点及其内部的数据的变更,会触发事件*/
        final NodeCache nodeCache=new NodeCache(curatorOperation.getClient(),"/curator/me");
        /*buildInitial:初始化时获取node的值并且缓存*/
        nodeCache.start(true);

        if (nodeCache.getCurrentData().getData()!=null){
            logger.info("节点初始化数据为: "+new String(nodeCache.getCurrentData().getData()));
        }else {
            logger.info("节点初始化数据为空");
        }

        /*设置nodeCache监听器*/
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                logger.info("节点路径: "+nodeCache.getCurrentData().getPath()+" , "+"节点数据: "+new String(nodeCache.getCurrentData().getData()));
            }
        });

        /*监听节点及其子节点的数据变更,cacheData:设置缓存节点的数据状态*/
        PathChildrenCache childrenCache=new PathChildrenCache(curatorOperation.getClient(),"/curator",true);
        /*startMode:
        * POST_INITIALIZED_EVENT:异步初始化,初始化之后会触发事件
        * NORMAL:异步初始化,不推荐
        * BUILD_INITIAL_CACHE:同步初始化
        * */
        childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        List<ChildData> childDataList = childrenCache.getCurrentData();
        logger.info("当前节点的子节点数据列表: ");
        for (ChildData childData:childDataList){
            logger.info(childData.getPath()+" , "+new String(childData.getData()));
        }

        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                logger.info("pathChildrenEvent: "+pathChildrenCacheEvent);
            }
        });


        /*修改节点数据*/
        curatorOperation.getClient().setData().withVersion(0).forPath("/curator/me","lalala".getBytes());

        /*获取节点数据*/
        byte[] bytes = curatorOperation.getClient()
                .getData()
                .forPath("/curator/me");

        logger.info("获取到数据: "+new String(bytes));

        /*获取子节点列表*/
        List<String> strings = curatorOperation.getClient().getChildren().forPath("/curator");
        for (String s:strings){
            logger.info("子节点: "+s);
        }



        /*删除节点*/
        curatorOperation.getClient()
                .delete()
                .guaranteed()//如果删除失败,后台会继续执行删除,直到成功
                .deletingChildrenIfNeeded()//如果有子节点的话,继续删除
                .withVersion(0)
                .forPath("/curator");

        /*判断节点是否存在*/
        Stat stat = curatorOperation.getClient().checkExists().forPath("/curator");
        if (stat==null){
            logger.info("该节点不存在");
        }else {
            logger.info(""+stat);
        }



        curatorOperation.close();

        Thread.sleep(3000);

        logger.info("当前客户端状态: "+curatorOperation.getClient().isStarted());


    }


}
