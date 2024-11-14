package com.bx.implatform.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.taobao.api.ApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class DingdingMessageService {


    public static List<String> userIdList = new ArrayList<>();

    public  void sendMessage(String msg) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken?appkey=dingsiboi4otksvedufq&appsecret=3_5w6tczEql4J0eGmtNttOhX4eqLp6KHwItATKOrM0HrgC2cLe91vp3S9gPUh3LI");
        try {
            OapiGettokenRequest req = new OapiGettokenRequest();
            req.setHttpMethod("GET");
            OapiGettokenResponse rsp = client.execute(req);
            JSONObject body = JSONUtil.parseObj(rsp.getBody());
            String accessToken = body.getStr("access_token");
            OapiMessageCorpconversationAsyncsendV2Request req2 = new OapiMessageCorpconversationAsyncsendV2Request();
            req2.setAgentId(3299813105L);
            req2.setUseridList(String.join(",", userIdList));
            OapiMessageCorpconversationAsyncsendV2Request.Msg obj1 = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
            obj1.setMsgtype("text");
            OapiMessageCorpconversationAsyncsendV2Request.Text obj2 = new OapiMessageCorpconversationAsyncsendV2Request.Text();
            obj2.setContent(msg);
            obj1.setText(obj2);
            req2.setMsg(obj1);
            OapiMessageCorpconversationAsyncsendV2Response rsp2 = client.execute(req2, accessToken);

        } catch (ApiException e) {
            e.printStackTrace();
        }

    }

    public String update(String newIds, boolean clear){
        if(clear){
            userIdList.clear();
        }
        String[] split = newIds.split(",");
        userIdList.addAll(Arrays.asList(split));
        userIdList = new ArrayList<>(userIdList.stream().distinct().collect(Collectors.toList()));
        return "1";
    }
}
