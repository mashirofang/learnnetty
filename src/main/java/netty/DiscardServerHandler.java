//package netty;
//
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.util.ReferenceCountUtil;
//
//// 通道入站处理适配器ChannelInboundHandlerAdapter
//// 这个类提供了可以覆盖各种事件的处理程序方法
//public class DiscardServerHandler extends ChannelInboundHandlerAdapter
//{
//    //正常情况的业务逻辑,直接丢弃收到的信息
////    @Override
////    public void channelRead(ChannelHandlerContext ctx, Object msg)
////    { // (2)
////        // Discard the received data silently.
////        ((ByteBuf)msg).release(); // (3)
////    }
//
//    // 这是一段比较正常的代码,会用拿到的msg处理一些业务
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg)
//    {
//        ByteBuf in = (ByteBuf) msg;
//        try
//        {
//            //用msg做些什么
//            while (in.isReadable()) { // (1)
//                System.out.print((char) in.readByte());
//                System.out.flush();
//            }
//        }
//        finally
//        {
//            ReferenceCountUtil.release(msg);
//        }
//    }
//
//    // 异常的业务逻辑,打印出错信息并关闭通道
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
//    { // (4)
//        // Close the connection when an exception is raised.
//        cause.printStackTrace();
//        ctx.close();
//    }
//
//}
