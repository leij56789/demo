package com.company.demo.mq;

import com.company.demo.config.RabbitMQConfig;
import com.company.demo.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserMessageProducer {

    private static final Logger log = LoggerFactory.getLogger(UserMessageProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendUserCreatedMessage(User user) {
        // 使用 Map 而不是 User 对象，避免序列化问题
        Map<String, Object> message = new HashMap<>();
        message.put("event", "USER_CREATED");
        message.put("userId", user.getId());
        message.put("userName", user.getName());
        message.put("timestamp", System.currentTimeMillis());

        log.info("发送用户创建消息: {}", message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EXCHANGE, RabbitMQConfig.USER_ROUTING_KEY, message);
    }

    public void sendEmailMessage(String to, String subject, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("to", to);
        message.put("subject", subject);
        message.put("content", content);

        log.info("发送邮件消息: {}", to);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, message);
    }
}