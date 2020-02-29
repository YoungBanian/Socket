import connector.BIOConnector;
import connector.NIOConnector;

public class Bootstrap {

    public static void main(String[] args) {
//        BIOConnector connector = new BIOConnector();
//        connector.start();

        NIOConnector connector = new NIOConnector();
        connector.start();
    }

}
