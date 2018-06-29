package Curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CuratorAcl {

    private static Logger logger=LoggerFactory.getLogger(CuratorAcl.class);

    private static String serverPath="127.0.0.1:2181";

    public static void main(String[] args) throws Exception {

        /*创建权限列表*/
        List<ACL> acls=new ArrayList<ACL>();
        Id caohao=new Id("digest",DigestAuthenticationProvider.generateDigest("caohao:123456"));
        Id ltx=new Id("digest",DigestAuthenticationProvider.generateDigest("ltx:123456"));
        acls.add(new ACL(ZooDefs.Perms.ALL,caohao));
        acls.add(new ACL(ZooDefs.Perms.READ,ltx));

        RetryPolicy retryPolicy=new RetryNTimes(3,1000);

        CuratorFramework client = CuratorFrameworkFactory.builder()
                .authorization("digest","caohao:123456".getBytes())//权限登录
                .connectString(serverPath)
                .retryPolicy(retryPolicy)
                .namespace("myWork")
                .build();

        client.start();

        String acl = client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(acls)
                .forPath("/acl/a", "acl".getBytes());//这种方式在递归创建子节点时,父节点不会赋予权限,默认为anyone

        logger.info(acl);

        List<ACL> aclList = client.getACL().forPath("/acl");
        for (ACL acl1:aclList){
            logger.info("父节点权限: "+acl1);
        }

        List<ACL> aclListSub = client.getACL().forPath("/acl/a");
        for (ACL acl2:aclListSub){
            logger.info("子节点权限: "+acl2);
        }

        client.setACL().withACL(acls).forPath("/acl");

        List<ACL> aclListPar = client.getACL().forPath("/acl");
        for (ACL acl1:aclListPar){
            logger.info("父节点权限: "+acl1);
        }

        client.delete()
                .guaranteed()
                .deletingChildrenIfNeeded()
                .forPath("/acl");


    }

}
