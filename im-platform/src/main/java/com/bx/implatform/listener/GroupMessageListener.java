package com.bx.implatform.listener;

import com.bx.imclient.annotation.IMListener;
import com.bx.imclient.listener.MessageListener;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.enums.IMSendCode;
import com.bx.imcommon.model.GroupMessageInfo;
import com.bx.imcommon.model.SendResult;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;


@Slf4j
@IMListener(type = IMListenerType.GROUP_MESSAGE)
public class GroupMessageListener implements MessageListener {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void process(SendResult result){
        GroupMessageInfo messageInfo = (GroupMessageInfo) result.getData();
        // 保存该用户已拉取的最大消息id
        if(result.getCode().equals(IMSendCode.SUCCESS.code())) {
            String key = String.join(":",RedisKey.IM_GROUP_READED_POSITION,messageInfo.getGroupId().toString(),result.getReceiver().getId().toString());
            redisTemplate.opsForValue().set(key, messageInfo.getId());
        }
    }

}
