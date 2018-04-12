package aio.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * CompletionHandler 完成连接的处理过程,里面有两个方法,一个是成功一个是失败
 *
 * @author xufan
 * @version C10 2018年04月10日 
 * @since SDP V300R003C10
 */
public class AcceptComletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServerHandler>
{
    @Override
    public void completed(AsynchronousSocketChannel result, AsyncTimeServerHandler attachment)
    {
        // 接受一次请求成功后,去处理第二个请求,成功以后依然会回到这里
        attachment.asynchronousServerSocketChannel.accept(attachment,this);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        result.read(buffer,buffer,new ReadCompletionHandler(result));




    }

    @Override
    public void failed(Throwable exc, AsyncTimeServerHandler attachment)
    {
        attachment.latch.countDown();
    }
}
