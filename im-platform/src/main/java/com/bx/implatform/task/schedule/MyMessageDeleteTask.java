package com.bx.implatform.task.schedule;

import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.mapper.GroupMessageMapper;
import com.bx.implatform.mapper.PrivateMessageMapper;
import com.bx.implatform.util.SensitiveFilterUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author: Blue
 * @date: 2024-09-01
 * @version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MyMessageDeleteTask {
    @Resource
    PrivateMessageMapper privateMessageMapper;
    @Resource
    GroupMessageMapper groupMessageMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    public void run() {
        ChainWrappers.lambdaUpdateChain(privateMessageMapper).remove();
        ChainWrappers.lambdaUpdateChain(groupMessageMapper).remove();

    }
}
