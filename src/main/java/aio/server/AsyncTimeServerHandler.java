package aio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * aio 服务端 demo 代码
 */
public class AsyncTimeServerHandler implements Runnable {

    // 端口号
    private static final int PORT = 8080;

    // 共享锁
    CountDownLatch latch;

    // aio-socket 服务端
    AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AsyncTimeServerHandler()
    {
        try
        {
            // 初始化资源
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            // 绑定端口
            asynchronousServerSocketChannel.bind(new InetSocketAddress(PORT));
            // 启动成功
            System.out.println("THE AIO TIME SERVER IS STARTED IN PROT" + PORT);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void run()
    {
        latch = new CountDownLatch(1);

        doAccept();

        try
        {
            // 这里是让线程阻塞,实际中不需要这样
            latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }

    // 接受连接
    private void doAccept()
    {
        asynchronousServerSocketChannel.accept(this,new AcceptComletionHandler());


    }
}
