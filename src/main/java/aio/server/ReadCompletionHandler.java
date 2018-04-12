package aio.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 *
 *
 * @author xufan
 * @version C10 2018年04月10日
 * @since SDP V300R003C10
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer>
{
    private AsynchronousSocketChannel channel;

    public ReadCompletionHandler(AsynchronousSocketChannel channel)
    {
        if (this.channel == null)
        {
            this.channel = channel;
        }
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment)
    {
        attachment.flip();

        byte[] body = new byte[attachment.remaining()];

        attachment.get(body);

        try
        {

            String req = new String(body, "UTF-8");

            System.out.println("The Server Receive Order :" + req);

            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(req) ? new Date().toString() : "BAD ORDER";

            doWrite(currentTime);
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
            channel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void doWrite(String currentTime)
    {
        byte[] bytes = currentTime.getBytes();

        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);

        writeBuffer.put(bytes);

        writeBuffer.flip();

        channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>()
        {

            // 没有完成继续发送的过程
            @Override
            public void completed(Integer result, ByteBuffer attachment)
            {
                if (attachment.hasRemaining())
                {
                    channel.write(attachment, attachment, this);
                }

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment)
            {
                try
                {
                    channel.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

    }

}
