package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


/**
 * netty 官网的一个小案例
*/
public class DiscardServer
{
    private int port;

    public DiscardServer(int port)
    {
        this.port = port;
    }

    public static void main(String[] args)
        throws Exception
    {
        int port = 8080;

        new DiscardServer(port).run();
    }

    public void run()
        throws Exception
    {
        // 创建处理i/0操作的事件循环,为不通的传输做了不同的实现,(甚至可以通过构造函数进行配置)
        // bossGroup 专门用来接收连接
        // workerGroup 专门用来处理业务
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try
        {
            // 服务构建帮助类,避免直接使用channel搭建服务
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                // 指定nioServerSocketChannel用于接受请求
                .channel(NioServerSocketChannel.class) // (3)
                // 这里指定的处理器每当新接受一个channel时就会执行
                .childHandler(new ChannelInitializer<SocketChannel>()
                { // (4)
                    @Override
                    public void initChannel(SocketChannel ch)
                        throws Exception
                    {
                        ch.pipeline().addLast(new DiscardServerHandler());
                    }
                })
                // 用于设置socket的属性 用于设置 NioServerSocketChannel
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                // 用于设置接受父channel 的子channel
                .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            // 绑定端口启动连接
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            // 优雅关闭应用
            f.channel().closeFuture().sync();
        }
        finally
        {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
