package com.scdy.comprehensiveinsurance.utils;

/**
 * 返回数据处理工具.
 */
public class ResponsedDataUtil {
//    public static OriginalAnalysisModel getOriginalAnalysisModel(String responseInstruction) {
//        //01 03 1A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
//        //00 00 10 03 0D 55 E3 AD
//
//        ArrayList<String> responseInstructionList = ByteUtil.strsToList(responseInstruction.replace(" ", ""));
//        OriginalAnalysisModel originalAnalysisModel = new OriginalAnalysisModel();
//        originalAnalysisModel.setSlaveAddressHex(responseInstructionList.get(0));//从机地址
//        originalAnalysisModel.setFunctionCodeHex(responseInstructionList.get(1));//功能码
//        String totalDataBytesHex = responseInstructionList.get(2);
//        originalAnalysisModel.setTotalDataBytesHex(totalDataBytesHex);//数据字节总数
//        Integer totalDataBytes = ByteUtil.hexStringToNum(totalDataBytesHex);
//        int evenNumbers = totalDataBytes / 2;
//
//        ArrayList<String> dataListHex = new ArrayList<>();
//
//        int index = 3;
//        for (int i = 0; i < evenNumbers; i++) {
//            String dataHex = "";
//            for (int j = 0; j < 2; j++) {
//                dataHex += responseInstructionList.get(index);
//                index++;
//            }
//            dataListHex.add(dataHex);
//        }
//
//        originalAnalysisModel.setDataListHex(dataListHex);//数据List
//        //CRC校验值
//        originalAnalysisModel.setCRCHex(responseInstructionList.get(responseInstructionList.size() - 2) + responseInstructionList.get(responseInstructionList.size() - 1));
//
//        return originalAnalysisModel;
//    }
}
