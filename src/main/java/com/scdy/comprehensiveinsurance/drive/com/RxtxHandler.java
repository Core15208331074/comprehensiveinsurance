package com.scdy.comprehensiveinsurance.drive.com;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RxtxHandler extends ChannelInboundHandlerAdapter {

    private StringBuffer stringBuffer = new StringBuffer();//用于接收存储串口发过来的数据 必须用static才能接收到到数据 但是可能出现其他问题，多线程字段共享


    /**
     * 客户端接收到数据的回调
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String str = msg.toString();
        stringBuffer.append(str);

    }

    /**
     * 获取串口发过来的完整数据，并且清空stringBuffer内容
     *
     * @return
     */
    public String receiveMsg() throws InterruptedException {
        Thread.sleep(3000);//延迟1秒接收获取到的指令
        String str = stringBuffer.toString();
        stringBuffer = new StringBuffer();
        return str;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
