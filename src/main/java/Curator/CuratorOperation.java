package Curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryForever;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .namespace("myWork¢")
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

        curatorOperation.getClient().
                create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath("/curator/me","caohao".getBytes());

        curatorOperation.close();

        Thread.sleep(3000);

        logger.info("当前客户端状态: "+curatorOperation.getClient().isStarted());


    }


}
