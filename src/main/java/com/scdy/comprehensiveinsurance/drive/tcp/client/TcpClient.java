package com.scdy.comprehensiveinsurance.drive.tcp.client;


import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.drive.PacketDecoder;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Data
public  class  TcpClient {
    private String targetIp;//目标ip
    private Integer targetPort;//目标端口
    private EthEntity ethEntity;
    private SensorModel sensorModel;

    private NioEventLoopGroup group;//用于关闭tcp
    private TcpClientHandler tcpClientHandler;//用于接收tcp返回的数据
    private ChannelFuture channelFuture;//用于给tcp发送消息


    public  TcpClient(String targetIp, Integer targetPort, EthEntity ethEntity, SensorModel sensorModel) throws Exception {
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        this.ethEntity = ethEntity;
        this.sensorModel = sensorModel;
        connect();
        saveTcpClientToMap();
    }

    /**
     * tcp client 存储到map
     *
     * @throws InterruptedException
     */
    private synchronized Map<String, TcpClient> saveTcpClientToMap() throws InterruptedException {
        Map<String, TcpClient> tcpClientMap = GlobalConstants.getTcpClientMap();
        String key = targetIp + ":" + targetPort;
        TcpClient tcpClient = tcpClientMap.get(key);
        if (StringUtils.isEmpty(tcpClient)) {
            tcpClientMap.put(key, this);
        } else {
            tcpClient.getGroup().shutdownGracefully().sync();
            tcpClientMap.put(key, this);
        }
        return tcpClientMap;
    }

    /**
     * 连接tcp
     *
     * @return
     */
    private synchronized ChannelFuture connect() throws Exception {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            group = new NioEventLoopGroup();//用于关闭
            bootstrap.group(group);
            tcpClientHandler = new TcpClientHandler(ethEntity, sensorModel);//用于接收消息
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline p = socketChannel.pipeline();
                    p.addLast(new PacketDecoder());//解码器，自己写的
                    p.addLast(tcpClientHandler);
                }
            });
            channelFuture = bootstrap.connect(targetIp, targetPort).sync();//用于发送消息
        } catch (InterruptedException e) {
            log.error("连接tcp server失败，ip:" + targetIp + " 端口：" + targetPort + " 错误信息：{}", e.getMessage());
            group.shutdownGracefully().sync();
            e.printStackTrace();
        }
        return channelFuture;
    }


}

