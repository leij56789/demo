package com.company.demo.mq;

import com.company.demo.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserMessageConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.USER_QUEUE)
    public void handleUserCreated(Map<String, Object> message) {
        log.info("收到用户创建消息: {}", message);

        String event = (String) message.get("event");
        Object userId = message.get("userId");
        String userName = (String) message.get("userName");

        log.info("处理用户创建事件: userId={}, userName={}, event={}", userId, userName, event);

        // 发送欢迎邮件等业务处理
        sendWelcomeEmail(userName);
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmail(Map<String, String> message) {
        log.info("发送邮件: to={}, subject={}", message.get("to"), message.get("subject"));
    }

    private void sendWelcomeEmail(String userName) {
        log.info("发送欢迎邮件给: {}", userName);
    }
}