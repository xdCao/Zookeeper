import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.data.Stat;

public class ModifyCallback implements AsyncCallback.StatCallback {


    public void processResult(int i, String s, Object o, Stat stat) {

        System.out.println(i+"  ,  "+s+"  ,  "+o+"  ,  "+stat);

    }


}
