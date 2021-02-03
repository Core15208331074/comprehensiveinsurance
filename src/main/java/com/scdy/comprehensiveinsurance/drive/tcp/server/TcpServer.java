package com.scdy.comprehensiveinsurance.drive.tcp.server;


import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.drive.PacketDecoder;
import com.scdy.comprehensiveinsurance.entity.EthEntity;
import com.scdy.comprehensiveinsurance.model.SensorModel;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Data
public class TcpServer {
    private Integer localPort;//本地端口
    private EthEntity ethEntity;
    private TcpServerHandler tcpServerHandler;//用于接收tcp返回
    private SensorModel sensorModel;

    private EventLoopGroup boss;//用于关闭tcp的数据
    private EventLoopGroup worker;//用于关闭tcp的数据
    private ChannelFuture channelFuture;//用于给tcp发送消息


    public TcpServer(Integer localPort, EthEntity ethEntity, SensorModel sensorModel) throws Exception {
        this.localPort = localPort;
        this.ethEntity = ethEntity;
        this.sensorModel = sensorModel;
        connect();
        saveTcpServerToMap();
    }

    /**
     * tcp client 存储到map
     *
     * @throws InterruptedException
     */
    private synchronized Map<String, TcpServer> saveTcpServerToMap() throws InterruptedException {
        Map<String, TcpServer> tcpClientMap = GlobalConstants.getTcpServerMap();
        String key = localPort + "";
        TcpServer tcpServer = tcpClientMap.get(key);
        if (StringUtils.isEmpty(tcpServer)) {
            tcpClientMap.put(key, this);
        } else {
            tcpServer.getBoss().shutdownGracefully().sync();
            tcpServer.getWorker().shutdownGracefully().sync();
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
            ServerBootstrap bootstrap = new ServerBootstrap();
            boss = new NioEventLoopGroup();//用于关闭
            worker = new NioEventLoopGroup();//用于关闭
            bootstrap.group(boss, worker);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024); // 连接数
            bootstrap.option(ChannelOption.TCP_NODELAY, true); // 不延迟，消息立即发送
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); // 长连接
            tcpServerHandler = new TcpServerHandler(ethEntity, sensorModel);//用于接收消息
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline p = socketChannel.pipeline();
                    p.addLast(new PacketDecoder());//解码器，自己写的
                    p.addLast(tcpServerHandler);
                }
            });
            channelFuture = bootstrap.bind(localPort).sync();//用于发送消息
        } catch (InterruptedException e) {
            log.error("创建tcp server失败\r\n端口:{} \r\n错误信息:{}", localPort, e.getMessage());
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
            e.printStackTrace();
        }
        return channelFuture;
    }


}

