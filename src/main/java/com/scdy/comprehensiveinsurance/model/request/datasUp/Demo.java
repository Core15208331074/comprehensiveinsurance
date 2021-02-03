package com.scdy.comprehensiveinsurance.model.request.datasUp;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class Demo {
    public static void main(String[] args) {
        String str="{\"devsStatus\":[{\"influence\":1,\"name\":\"存在探测\",\"id\":\"0201597d63620190b350be0b63eec33c\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"空调\",\"id\":\"029e0a201b7f1d6ee64d2380837c53f9\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"风机\",\"id\":\"247f0dd1e571ebf29657a13225e3b6ba\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"烟感\",\"id\":\"27315e04ac4b0d579109b43d3b0365be\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"蓄电池\",\"id\":\"7d9ef79d0d1912e208d81c31a489b1cc\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"照明\",\"id\":\"8f7eff5ed425759f8cbed5b742aec33a\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"存在探测\",\"id\":\"95aeff3554586adfe6e1d33ded51c631\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"温湿度\",\"id\":\"965886f23ccf14a84f9f4af10475260d\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"温湿度\",\"id\":\"a695ea6cd02353cadc92457d3cafac92\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"照明\",\"id\":\"a6d76dce2f4a7e47b239d193bdf78a85\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"空调\",\"id\":\"aba34996e1e0c834352dd0fdab8ffee4\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"蓄电池\",\"id\":\"b35980342207f7779b4a73fe8f643add\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"温湿度\",\"id\":\"dbf29815b3b369dac8d07d5e1a5bdec7\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"风机\",\"id\":\"eaea41351896f7158ea013b04a48f50c\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"温湿度\",\"id\":\"ee13fd15c8f3f8d3e600edbcfcccbc57\",\"status\":1,\"group\":0},{\"influence\":1,\"name\":\"烟感\",\"id\":\"f448f0d02144d49f6bab82ab6757c1ec\",\"status\":1,\"group\":0}],\"id\":\"1a6495c978724aa1aad331272dc12977\",\"time\":\"2021-01-15 18:13:00\"}";
        JSONObject jsonObject = JSONUtil.parseObj(str);

        String s = SecureUtil.md5(jsonObject.toString());
        System.out.println(s);
    }
}
