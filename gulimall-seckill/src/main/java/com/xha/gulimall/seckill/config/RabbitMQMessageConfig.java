package com.xha.gulimall.seckill.config;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * RabbitMQ的消息序列化和反序列化配置、消息回调配置
 *
 * @author Xu Huaiang
 * @date 2023/02/10
 */
@Configuration
public class RabbitMQMessageConfig {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 设置确认回调
     *
     * @PostConstruct注解的作用是在当前对象创建完成后执行此方法
     */
    @PostConstruct
    public void setConfirmCallback() {
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 确认
             *
             * @param correlationData 当前消息的唯一关联数据（这个消息的唯一ID）
             * @param ack             消息成功/失败
             * @param cause           原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("correlationData:" + correlationData + "，ack：" + ack + "，cause：" + cause);
            }
        });
    }

    /**
     * 设置回调
     */
    @PostConstruct
    public void setReturnCallback() {
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 消息未投递给指定队列,就触发当前的失败回调
             *
             * @param message    投递失败的消息
             * @param replyCode  回复状态码
             * @param replyText  回复文本
             * @param exchange   交换机
             * @param routingKey 路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("Message:" + message +
                        "，replyCode：" + replyCode +
                        "，replyText：" + replyText +
                        "，exchange：" + exchange +
                        "，routingKey：" + routingKey);
            }
        });
    }
}
