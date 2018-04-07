package nio.client;


public class TimeClient {



    public static void main(String[] args) {
       new Thread(new TimeClientHandle()).start();
    }


}
