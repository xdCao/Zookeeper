
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;


public class NodeAcl implements Watcher {

    private static Logger logger=LoggerFactory.getLogger(NodeAcl.class);

    private static final String serverpath="127.0.0.1:2181";

    private static final int timeout=5000;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeeperException, InterruptedException {

        ZooKeeper zooKeeper=new ZooKeeper(serverpath,timeout,new NodeAcl());

        zooKeeper.create("/acl","123".getBytes(), OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);

        /*创建权限列表*/
        List<ACL> acls=new ArrayList<ACL>();
        Id caohao=new Id("digest",DigestAuthenticationProvider.generateDigest("caohao:123456"));
        Id ltx=new Id("digest",DigestAuthenticationProvider.generateDigest("ltx:123456"));
        acls.add(new ACL(ZooDefs.Perms.ALL,caohao));
        acls.add(new ACL(ZooDefs.Perms.READ,ltx));

        zooKeeper.create("/acl/testd","testDigest".getBytes(),acls,CreateMode.PERSISTENT);

        /*以用户caohao登录*/
        zooKeeper.addAuthInfo("digest","caohao:123456".getBytes());

//        zooKeeper.addAuthInfo("digest","ltx:123456".getBytes());

        zooKeeper.create("/acl/testd/child","child".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);

        zooKeeper.delete("/acl/testd/child",0);


        zooKeeper.delete("/acl/testd",0);

        zooKeeper.delete("/acl",0);

        /*以ip方式创建权限*/
        List<ACL> ipAcls=new ArrayList<ACL>();
        Id ip=new Id("ip","127.0.0.1");
        ipAcls.add(new ACL(ZooDefs.Perms.ALL,ip));

        String s = zooKeeper.create("/ip", "ip".getBytes(), ipAcls, CreateMode.PERSISTENT);

        logger.info(s);

        zooKeeper.delete("/ip",0);


    }


    public void process(WatchedEvent watchedEvent) {
        logger.info("触发watcher事件; "+watchedEvent);
    }
}
