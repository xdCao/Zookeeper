import org.apache.zookeeper.AsyncCallback;

public class CreateCallback implements AsyncCallback.StringCallback {
    public void processResult(int rc, String path, Object ctx, String name) {
        System.out.println(rc+"   ,   "+path+"  ,  "+ctx+"   ,   "+name);
    }
}
