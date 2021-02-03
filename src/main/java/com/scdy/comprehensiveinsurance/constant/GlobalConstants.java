package com.scdy.comprehensiveinsurance.constant;

import com.scdy.comprehensiveinsurance.drive.com.ComSlave;
import com.scdy.comprehensiveinsurance.drive.tcp.client.TcpClient;
import com.scdy.comprehensiveinsurance.drive.tcp.server.TcpServer;
import com.scdy.comprehensiveinsurance.model.ComStatusModel;
import com.scdy.comprehensiveinsurance.model.EthStatusModel;
import io.netty.channel.ChannelHandlerContext;

import java.util.*;

/**
 * 全局常量
 */
public class GlobalConstants {
    public static final String TCP_CLIENT = "TCP Client";
    public static final String TCP_SERVER = "TCP Server";
    public static final String UDP_CLIENT = "UDP Client";
    public static final String UDP_SERVER = "UDP Server";
    public static final String ETH = "eth";
    public static final String COM = "com";
    public static final String FIRE_MONITORING = "火灾监控";
    public static final String HOST_INFORMATION = "主机信息";

    public static final String BATTERY_MOUNT_E_GROUP_PRESSURE= "电池组总电压";//峨山蓄电池电池组总电压
    public static final String BATTERY_MOUNT_E_BATTERY_TEMPERATURE= "电池温度";//峨山蓄电池电池温度
    public static final String BATTERY_MOUNT_E_MONOMER_RESISTANCE= "单体内阻";//峨山蓄电池单体内阻

    public static final String BATTERY_MOUNT_E_TOTAL_VOLTAGE = "电池组总电压";

    public static final String BATTERY_MOUNT_E_BATTERY_TEMPERATURE_201_300 = "电池温度201-300";
    public static final String BATTERY_MOUNT_E_BATTERY_TEMPERATURE_101_200 = "电池温度101-200";
    public static final String BATTERY_MOUNT_E_BATTERY_TEMPERATURE_1_100 = "电池温度1-100";

    public static final String BATTERY_MOUNT_E_MONOMER_RESISTANCE_201_300 = "单体内阻201-300";
    public static final String BATTERY_MOUNT_E_MONOMER_RESISTANCE_101_200 = "单体内阻101-200";
    public static final String BATTERY_MOUNT_E_MONOMER_RESISTANCE_1_100 = "单体内阻1-100";

    public static final String BATTERY_MOUNT_E_SINGLE_VOLTAGE_201_300 = "单体电压201-300";
    public static final String BATTERY_MOUNT_E_SINGLE_VOLTAGE_101_200 = "单体电压101-200";
    public static final String BATTERY_MOUNT_E_SINGLE_VOLTAGE_1_100 = "单体电压1-100";
    public static final String BATTERY_LITONG_TELEMETERING_STARTADDRESS_7= "0488";
    public static final String BATTERY_LITONG_TELEMETERING_STARTADDRESS_6= "0460";
    public static final String BATTERY_LITONG_TELEMETERING_STARTADDRESS_5= "0438";
    public static final String BATTERY_LITONG_TELEMETERING_STARTADDRESS_4= "0410";
    public static final String BATTERY_LITONG_TELEMETERING_STARTADDRESS_3= "03E8";
    public static final String BATTERY_LITONG_TELEMETERING_STARTADDRESS_2= "0028";
    public static final String BATTERY_LITONG_TELEMETERING_STARTADDRESS_1= "0000";
    public static final String BATTERY_LITONG_TELECOMMUNICATION= "遥信";
    public static final String BATTERY_LITONG_TELEMETERING_144= "遥信144";
    public static final String BATTERY_LITONG_TELEMETERING_1160_1207= "遥测1160_1207";
    public static final String BATTERY_LITONG_TELEMETERING_1120_1159= "遥测1120_1159";
    public static final String BATTERY_LITONG_TELEMETERING_1080_1119= "遥测1080_1119";
    public static final String BATTERY_LITONG_TELEMETERING_1040_1079= "遥测1040_1079";
    public static final String BATTERY_LITONG_TELEMETERING_1000_1039= "遥测1000_1039";
    public static final String BATTERY_LITONG_TELEMETERING_40_79= "遥测40_79";
    public static final String BATTERY_LITONG_TELEMETERING_0_39= "遥测0_39";
    public static final String BATTERY_LITONG= "蓄电池";
    public static final String ENVIRONMENTAL_CONTROL_LITONG_AIRCONDITIONER_SLAVE_ADDRESS_2= "09";//空调地址2
    public static final String ENVIRONMENTAL_CONTROL_LITONG_AIRCONDITIONER_SLAVE_ADDRESS_1= "08";//空调地址1
    public static final String ENVIRONMENTAL_CONTROL_LITONG_LIGHTING_CONTROL_SLAVE_ADDRESS= "06";//照明反控地址
    public static final String ENVIRONMENTAL_CONTROL_LITONG_AIR_CONDITIONER = "空调";
    public static final String ENVIRONMENTAL_CONTROL_LITONG_AIR_CONDITIONER_FUNCTION_CODE = "06";//空调
    public static final String ENVIRONMENTAL_CONTROL_LITONG_LIGHTCONTROL = "灯控";//名称，不是反控
    public static final String ENVIRONMENTAL_CONTROL_LITONG_LIGHT_CONTROL_FUNCTION_CODE = "03";//灯控功能码
    public static final String ENVIRONMENTAL_CONTROL_LITONG_FAN_SMOKE_SENSOR_DETECTOR = "风机、烟感、探测器";
    public static final String ENVIRONMENTAL_CONTROL_LITONG_FAN_SMOKE_SENSOR_DETECTOR_FUNCTION_CODE = "02";//风机、烟感、探测器功能码
    public static final String ENVIRONMENTAL_CONTROL_LITONG_TEMPERATURE_HUMIDITY = "温湿度";
    public static final String ENVIRONMENTAL_CONTROL_LITONG_TEMPERATURE_HUMIDITY_FUNCTION_CODE = "04";//温湿度功能码
    public static final String ENVIRONMENTAL_CONTROL_LITONG_LIGHTING_CONTROL = "照明控制";
    public static final String ENVIRONMENTAL_CONTROL_LITONG = "照明(利通环控)";
    public static final String DATA_ACQUISITION_CONTROLLER_SWITCH = "开关量";
    public static final String DATA_ACQUISITION_CONTROLLER_RELAY = "继电器";
    public static final String DATA_ACQUISITION_CONTROLLER = "数采控制器";
    public static final String ELECTRONIC_FENCE_CLOTH_REMOVAL_CONTROL = "布撤防控制";
    public static final String ELECTRONIC_FENCE_STATUS_QUERY = "状态查询";
    public static final String ELECTRONIC_FENCE = "电子围栏";
    public static final String CORE_CLAMP_ALARM_SIGN_CLAMP_CURRENT_ALARM_SIGN = "夹件电流报警标志";
    public static final String CORE_CLAMP_ALARM_SIGN_CORE_CURRENT_ALARM_MARK = "铁芯电流报警标志";
    public static final String CORE_CLAMP_ALARM_SIGN = "铁芯夹件-报警标志";
    public static final String CORE_CLAMP_READ_TIME_CURRENT_DATA_CLAMP = "夹件电流";
    public static final String CORE_CLAMP_READ_TIME_CURRENT_DATA_CURRENT = "铁芯电流";
    public static final String CORE_CLAMP_READ_TIME_CURRENT_DATA_TIME = "时间";
    public static final String CORE_CLAMP_READ_TIME_CURRENT_DATA = "铁芯夹件-时间和电流";
    public static final String COMPLEX_PROTECTION_TELEMETRY = "XZL-801D微机综合保护测控装置-遥测";
    public static final String MEASURING_PHASE_A_CURRENT = "测量A相电流";
    public static final String MEASURING_PHASE_B_CURRENT = "测量B相电流";
    public static final String MEASURING_C_PHASE_CURRENT = "测量C相电流";
    public static final String PHASE_A_VOLTAGE = "A相电压";
    public static final String B_PHASE_VOLTAGE = "B相电压";
    public static final String C_PHASE_VOLTAGE = "C相电压";
    public static final String AB_CAMERA_LINE_VOLTAGE = "AB相线电压";
    public static final String BC_CAMERA_LINE_VOLTAGE = "BC相线电压";
    public static final String CA_CAMERA_LINE_VOLTAGE = "CA相线电压";
    public static final String ACTIVE_POWER_P = "有功功率P";
    public static final String REACTIVE_POWER_Q = "无功功率Q";
    public static final String POWER_FACTOR = "功率因数";
    public static final String FREQUENCY = "频率";
    public static final String COMPLEX_PROTECTION_ALERT = "XZL-801D微机综合保护测控装置-告警";
    public static final String COMPLEX_PROTECTION_ALERT_TIME = "时间";
    public static final String COMPLEX_PROTECTION_ALERT_REPORT_PROPERTIES = "报告属性";
    public static final String COMPLEX_PROTECTION_ALERT_RECORD_INDEX_NUMBER = "记录的索引号";
    public static final String COMPLEX_PROTECTION_ALERT_HIGH_ACTION_VALUE = "动作值高";
    public static final String COMPLEX_PROTECTION_ALERT_ACTION_VALUE_ATTRIBUTE = "动作值属性";

    private static volatile LinkedHashMap<String, LinkedHashMap<String, EthStatusModel>> ethStatusMap = new LinkedHashMap<>();//eth状态
    private static LinkedHashMap<String, LinkedHashMap<String, ComStatusModel>> comStatusMap = new LinkedHashMap<>();//串口状态
    private static volatile Map<String, TcpClient> tcpClientMap = new HashMap<String, TcpClient>();//存放tcp client
    private static volatile Map<String, TcpServer> tcpServerMap = new HashMap<String, TcpServer>();//存放tcp server
    private static volatile Map<String,ChannelHandlerContext> tcpClientChannelMap =new HashMap<>();//tcp server客户端通道，用于主动给客户端发送消息
    private static Map<String, ComSlave> comSlaveMap = new HashMap<String, ComSlave>();//存放com slave

    public static Map<String, ChannelHandlerContext> getTcpClientChannelMap() {
        return tcpClientChannelMap;
    }

    public static Map<String, TcpServer> getTcpServerMap() {
        return tcpServerMap;
    }

    public static Map<String, ComSlave> getComSlaveMap() {
        return comSlaveMap;
    }

    public static  Map<String, TcpClient> getTcpClientMap() {
        return tcpClientMap;
    }

    public static LinkedHashMap<String, LinkedHashMap<String, EthStatusModel>> getEthStatusMap() {
        return ethStatusMap;
    }

    public static LinkedHashMap<String, LinkedHashMap<String, ComStatusModel>> getComStatusMap() {
        return comStatusMap;
    }

    private static Map<String, Boolean> ComStartStatusMap = new HashMap<>();//串口启动状态

    public static Map<String, Boolean> getComStartStatusMap() {
        return ComStartStatusMap;
    }

}
