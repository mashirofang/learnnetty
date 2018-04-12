package aio.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class AioTimeClientHandle implements Runnable, CompletionHandler<Void, AioTimeClientHandle>
{
    private AsynchronousSocketChannel client;

    private final static String HOST = "127.0.0.1";

    private final static int PORT = 8080;

    private CountDownLatch latch;

    public AioTimeClientHandle()
    {
        // 初始化资源
        try
        {
            client = AsynchronousSocketChannel.open();
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

        client.connect(new InetSocketAddress(HOST, PORT), this, this);

        try
        {
            // 这里应当也单纯只是为了让线程阻塞
            latch.await();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        try
        {
            // 线程唤醒时关闭资源
            client.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void completed(Void result, AioTimeClientHandle attachment)
    {
        // 请求信息
        byte[] req = "QUERY TIME ORDER".getBytes();

        ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

        writeBuffer.put(req);

        writeBuffer.flip();

        client.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>()
        {
            @Override
            public void completed(Integer result, ByteBuffer attachment)
            {
                // 如果没有发送完则继续发送
                if (attachment.hasRemaining())
                {
                    client.write(attachment, attachment, this);
                }
                else
                {
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    // 如果已经发送完成,则读取响应
                    client.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>()
                    {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment)
                        {
                            attachment.flip();

                            byte[] bytes = new byte[attachment.remaining()];

                            attachment.get(bytes);

                            try
                            {
                                String body = new String(bytes, "UTF-8");

                                System.out.println("NOW IS :" + body);

                                latch.countDown();
                            }
                            catch (UnsupportedEncodingException e)
                            {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment)
                        {
                            try
                            {
                                client.close();

                                latch.countDown();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                        }
                    });


                }

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment)
            {
                try
                {
                    client.close();

                    latch.countDown();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void failed(Throwable exc, AioTimeClientHandle attachment)
    {
        try
        {
            client.close();

            latch.countDown();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
