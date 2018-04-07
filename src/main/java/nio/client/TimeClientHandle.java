package nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandle implements Runnable {

    private final static String HOST = "127.0.0.1";

    private final static int PORT = 8080;

    private volatile boolean stop;

    private Selector selector;

    private SocketChannel socketChannel;

    public TimeClientHandle() {
        // 初始化资源
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    @Override
    public void run() {
        try {
            // 建立连接
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!stop) {
            SelectionKey key = null;
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    handleInput(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
                key.cancel();
                try {
                    key.channel().close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.exit(1);
                }
            }
        }

        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    // 处理流程
    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            // 判断连接是否成功
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {
                if (sc.finishConnect()) {
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                } else {
                    System.exit(1);
                }
            }
            // 读就绪
            if (key.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);

                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("Now is :" + body);
                } else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                }


            }


        }


    }

    private void doConnect() throws IOException {
        // 建立连接
        if (socketChannel.connect(new InetSocketAddress(HOST, PORT))) {
            // 注册读事件
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        } else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void doWrite(SocketChannel socketChannel) throws IOException {
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
        if (!writeBuffer.hasRemaining()) {
            System.out.println("SEND ORDER TO SERVER SUCCEED.");
        }

    }
}
