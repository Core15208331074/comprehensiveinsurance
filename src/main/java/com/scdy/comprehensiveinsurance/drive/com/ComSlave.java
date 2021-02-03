package com.scdy.comprehensiveinsurance.drive.com;

import com.scdy.comprehensiveinsurance.constant.GlobalConstants;
import com.scdy.comprehensiveinsurance.drive.PacketDecoder;
import com.scdy.comprehensiveinsurance.service.SensorDataService;
import com.scdy.comprehensiveinsurance.service.impl.SensorDataServiceImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Data
public class ComSlave {
    private SensorDataService sensorDataService = new SensorDataServiceImpl();

    private String portName;//端口号
    private Integer baudrate;//波特率
    private RxtxChannelConfig.Databits databits;//数据位
    private String databitsStr;//数据位字符串
    private RxtxChannelConfig.Stopbits stopbits;//停止位
    private String stopbitsStr;//停止位字符串
    private RxtxChannelConfig.Paritybit paritybit;//校验位
    private String paritybitStr;//校验位字符串


    private OioEventLoopGroup group;//用于关闭
    private RxtxHandler rxtxHandler;//用于接收返回的数据
    private RxtxChannel rxtxChannel;//用于发送信息到串口


    public ComSlave(String portName, Integer baudrate, String paritybit, String databits, String stopbits) throws Exception {
        this.portName = portName;
        this.baudrate = baudrate;
        this.databitsStr = databits;
        this.stopbitsStr = stopbits;
        this.paritybitStr = paritybit;

        //校验位
        switch (paritybit) {
            case "NONE":
                this.paritybit = RxtxChannelConfig.Paritybit.NONE;
                break;
            case "ODD":
                this.paritybit = RxtxChannelConfig.Paritybit.ODD;
                break;
            case "EVEN":
                this.paritybit = RxtxChannelConfig.Paritybit.EVEN;
                break;
            case "MARK":
                this.paritybit = RxtxChannelConfig.Paritybit.MARK;
                break;
            case "SPACE":
                this.paritybit = RxtxChannelConfig.Paritybit.SPACE;
                break;
        }
        //数据位
        switch (databits) {
            case "5 bit":
                this.databits = RxtxChannelConfig.Databits.DATABITS_5;
                break;
            case "6 bit":
                this.databits = RxtxChannelConfig.Databits.DATABITS_6;
                break;
            case "7 bit":
                this.databits = RxtxChannelConfig.Databits.DATABITS_7;
                break;
            case "8 bit":
                this.databits = RxtxChannelConfig.Databits.DATABITS_8;
                break;
        }
        //停止位
        switch (stopbits) {
            case "1 bit":
                this.stopbits = RxtxChannelConfig.Stopbits.STOPBITS_1;
                break;
            case "1.5 bit":
                this.stopbits = RxtxChannelConfig.Stopbits.STOPBITS_1_5;
                break;
            case "2 bit":
                this.stopbits = RxtxChannelConfig.Stopbits.STOPBITS_2;
                break;
        }
        connect();
        saveComSlaveToMap();
    }

    /**
     * com slave 存储到map
     *
     * @throws InterruptedException
     */
    private Map<String, ComSlave> saveComSlaveToMap() throws InterruptedException {
        Map<String, ComSlave> slaveMap = GlobalConstants.getComSlaveMap();
        String key = portName + ":" + baudrate + ":" + paritybitStr + ":" + databitsStr + ":" + stopbitsStr;
        ComSlave comSlave = slaveMap.get(key);
        if (StringUtils.isEmpty(comSlave)) {
            slaveMap.put(key, this);
        } else {
            comSlave.getGroup().shutdownGracefully().sync();
            slaveMap.put(key, this);
        }
        return slaveMap;
    }

    /**
     * 根据串口名启动串口.
     *
     * @return
     */
    public RxtxChannel connect() throws Exception {
        //就是该类用于串口通讯
        rxtxChannel = new RxtxChannel();
        rxtxChannel.config().setBaudrate(baudrate);//波特率
        rxtxChannel.config().setParitybit(paritybit); //校验位
        rxtxChannel.config().setDatabits(databits);//数据位
        rxtxChannel.config().setStopbits(stopbits); //停止位

        try {
            Bootstrap bootstrap = new Bootstrap();
            group = new OioEventLoopGroup();
            bootstrap.group(group)
                    .channelFactory(new ChannelFactory<RxtxChannel>() {
                        @Override
                        public RxtxChannel newChannel() {
                            return rxtxChannel;
                        }
                    })
                    .handler(new ChannelInitializer<RxtxChannel>() {
                        @Override
                        public void initChannel(RxtxChannel ch) throws Exception {
                            rxtxHandler = new RxtxHandler();
                            ch.pipeline().addLast(
                                    //解码器，自己写的
                                    new PacketDecoder(),
                                    //handler类，处理逻辑
                                    rxtxHandler
                            );
                        }
                    });


            //绑定COM口
            bootstrap.connect(new RxtxDeviceAddress(portName)).sync();
        } catch (InterruptedException e) {
            log.error("连接串口"+portName+"失败：{}", e.getMessage());
            group.shutdownGracefully().sync();
            e.printStackTrace();
        }
        return rxtxChannel;
    }


}

