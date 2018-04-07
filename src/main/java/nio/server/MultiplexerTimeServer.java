package nio.server;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * nio 服务端 demo 代码
 *
 * @author xufan
 * @version C10 2018年04月02日
 * @since SDP V300R003C10
 */
public class MultiplexerTimeServer implements Runnable {
    // nio 的多路复用器
    private Selector selector;

    // nio 的服务端套接字
    private ServerSocketChannel serverSocketChannel;

    private volatile boolean stop;

    final static int PORT = 8080;

    // 初始化服务端
    public MultiplexerTimeServer() {
        // 初始化资源
        try {
            // 初始化多路复用器
            selector = Selector.open();
            // 初始化serverSocket
            serverSocketChannel = ServerSocketChannel.open();
            // 设置非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 绑定8080 端口,设置最大连接数为1024个
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT), 1024);
            // 将 serverSocket 注册到多路复用器,监听 accept 操作位
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            // 打印一行日志
            System.out.println("Server Start ! Port 8080");
        } catch (IOException e) {
            e.printStackTrace();
            // 如果资源初始化失败,如端口被占用等,则退出(这里的status = 1代表非正常退出)
            System.exit(1);
        }
    }

    // 关闭服务器
    public void stop() {
        this.stop = true;
    }

    public void run() {
        SelectionKey key = null;
        while (!stop) {
            try {
                // 每隔1秒遍历一次selector(也可以使用无参,则不进行间隔)
                selector.select(1000);
                // 当存在就绪状态的channel时,返回selectorkey的集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                // 对集合进行迭代和处理
                Iterator<SelectionKey> it = selectionKeys.iterator();

                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    handleInput(key);
                }
            } catch (IOException e) {
                key.cancel();
                try {
                    key.channel().close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        // 如果服务已经关闭,则关闭多路复用器释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 具体的处理逻辑
    private void handleInput(SelectionKey key)
            throws IOException {
        // 根据key的操作位判断事件
        if (key.isValid()) {
            if (key.isAcceptable()) {

                // 获取到key的channel
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                // 接受连接(完成accept后相当于完成了tcp的三次握手,建立物理链路)
                SocketChannel sc = ssc.accept();
                // 设置模式为非阻塞模式
                sc.configureBlocking(false);
                // 将这个新的连接注册到selector上去
                sc.register(selector, SelectionKey.OP_READ);
            }
            // 用于读取客户端的请求
            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();

                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                // 读到的字节数
                int readBytes = sc.read(readBuffer);
                // 读到的字节数如果大于0 ,则进行处理
                if (readBytes > 0) {
                    // 将缓冲区当前的limit设置为position ,position设置为0,用于后续的读写操作
                    readBuffer.flip();
                    // 创建 byte数组(大小为缓冲区的limit)
                    byte[] bytes = new byte[readBuffer.remaining()];
                    // 使用 get操作进行复制
                    readBuffer.get(bytes);
                    // byte[]转string
                    String body = new String(bytes, "UTF-8");
                    // 打印
                    System.out.println("The Time Server receive order :" + body);

                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date().toString() : "BAD ORDER";
                    // 将信息写出
                    doWrite(sc, currentTime);

                }
                // 如果读到的字节数为 -1 则说明链路已经关闭
                else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                }

            }

        }

    }

    private void doWrite(SocketChannel sc, String response)
            throws IOException {
        if (response != null && response.trim().length() > 0) {

            byte[] bytes = response.getBytes();

            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);

            writeBuffer.put(bytes);

            writeBuffer.flip();
            // 此处有半写包的场景,需要进行轮询,后续补足
            sc.write(writeBuffer);
        }
    }
}
