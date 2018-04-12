package aio.client;

public class AioTimeClient
{

    public static void main(String[] args)
    {
        new Thread(new AioTimeClientHandle()).start();
    }

}
