package aio.server;

/**
 * aio(nio2.0) 原生服务器
 */
public class AioTimeServer
{
    public static void main(String[] args) {
        // 开启线程
        new Thread(new AsyncTimeServerHandler(), "AIO-AsyncTimeServer-001").start();
    }

}
