package nio.server;

/**
 * nio 原生服务器
 *
 * @author xufan
 * @version C10 2018年04月02日
 * @since SDP V300R003C10
 */
public class TimeServer {
    public static void main(String[] args) {
        // 开启线程
        new Thread(new MultiplexerTimeServer(), "NIO-MultiplexerTimeServer-001").start();
    }

}
