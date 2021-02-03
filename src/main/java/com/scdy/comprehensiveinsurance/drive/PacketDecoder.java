package com.scdy.comprehensiveinsurance.drive;

import com.scdy.comprehensiveinsurance.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 保证每次ClientHandler每次接收到的数据都是一个完整的数据包。并将数据解析成16进制的字符串
 */
public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        byte[] read = new byte[in.readableBytes()];
        in.readBytes(read);
        String packet = ByteUtil.BinaryToHexString(read).toUpperCase();
        out.add(packet);
    }
}
