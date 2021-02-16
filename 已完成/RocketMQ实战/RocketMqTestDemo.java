/**
 * cmft.com Inc.
 * Copyright (c) 1872-2019 All Rights Reserved.
 */
package com.cmft.marathon.rocketmqDemo.presentation.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmft.marathon.entity.ResponseData;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author
 * @version $Id: RocketMqProducer.java, v 0.1 2019-10-16 10:56 Exp $$
 */
@RestController
@RequestMapping("/rocketMq")
public class RocketMqTestDemo {



    @PostMapping("/send")
    public Object producerOfSendMsg (@RequestParam(value="data") String data)throws Exception{
//        //通过ProducerGroupName初始化消息生产者
//        DefaultMQProducer producer = new DefaultMQProducer("RocketMqProducerGroupTest");
//        //设置MQ的NameSrv服务地址
//        producer.setNamesrvAddr("100.75.186.165:4308");
//        //关闭VIP通道，防止消息发送错误
//        producer.setVipChannelEnabled(false);
//        //启动生产者实例
//        producer.start();
//        //创建消息对象，包括Topic、Tag、Key和传递的数据
//        for(int i=0;i<20;i++){
//            Message msg = new Message("TopicTest", "TagTest", "KeyTest",(data+i).getBytes(RemotingHelper.DEFAULT_CHARSET));
//            //Reliable Synchronous方式发送消息到NameSrv下的某个broker实例并得到返回结果
//            SendResult sendResult = producer.send(msg);
//        }
//        //关闭生产者实例
//        producer.shutdown();
//        //返回消息的响应
//        return "success";


//        DefaultMQProducer producer = new DefaultMQProducer("RocketMqProducerGroupTest");
//        producer.setNamesrvAddr("100.75.186.165:4308");
//        producer.setVipChannelEnabled(false);
//        producer.setRetryTimesWhenSendAsyncFailed(1);
//        producer.setCreateTopicKey("AUTO_CREATE_TOPIC_KEY");
//        producer.start();
//        //Create a message instance, specifying topic, tag and message body.
//        Message msg = new Message("TopicTest", "TagTest", "KeyTest", data.getBytes(RemotingHelper.DEFAULT_CHARSET));
//        producer.send(msg, new SendCallback() {
//            @Override
//            public void onSuccess(SendResult sendResult) {
//                System.out.printf(sendResult.getMsgId());
//            }
//            @Override
//            public void onException(Throwable e) {
//                e.printStackTrace();
//            }
//        });
//        producer.shutdown();
//        return null;

//        //通过ProducerGroupName初始化消息生产者
//        DefaultMQProducer producer = new DefaultMQProducer("RocketMqProducerGroupTest");
//        //设置MQ的NameSrv服务地址
//        producer.setNamesrvAddr("100.75.186.165:4308");
//        //关闭VIP通道，防止消息发送错误
//        producer.setVipChannelEnabled(false);
//        //启动生产者实例
//        producer.start();
//        //创建消息对象，包括Topic、Tag、Key和传递的数据
//        Message msg = new Message("TopicTest", "TagTest", "KeyTest",data.getBytes(RemotingHelper.DEFAULT_CHARSET));
//        //Reliable Synchronous方式发送消息到NameSrv下的某个broker实例并得到返回结果
//        producer.sendOneway(msg);
//        //关闭生产者实例
//        producer.shutdown();
//        //返回消息的响应
//        return null;

        //通过ProducerGroupName初始化消息生产者
        DefaultMQProducer producer = new DefaultMQProducer("RocketMqProducerGroupTest");
        //设置MQ的NameSrv服务地址
        producer.setNamesrvAddr("100.75.186.165:4308");
        //关闭VIP通道，防止消息发送错误
        producer.setVipChannelEnabled(false);
        //启动生产者实例
        producer.start();
        //创建消息对象，包括Topic、Tag、Key和传递的数据
        for(int i=0;i<20;i++){
            Message msg = new Message("TopicTest", "TagTest", "KeyTest",(data+i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    Integer id = (Integer) arg;
                    int index = id % mqs.size();
                    return mqs.get(index);
                }
            }, 1);
        }
//        //关闭生产者实例
//        producer.shutdown();
        //返回消息的响应
        return "success";



    }

    @PostConstruct
    public void consumer() throws InterruptedException, MQClientException {

        // Instantiate with specified consumer group name.
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("RocketMqConsumerGroupTest");

        // Specify name server addresses.
        consumer.setNamesrvAddr("100.75.186.165:4308");
        consumer.setVipChannelEnabled(false);
        //消费消息方式 consumerGroup第一次启动 CONSUME_FROM_FIRST_OFFSET从消息队列头部消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        //广播模式消费消息
//        consumer.setMessageModel(MessageModel.BROADCASTING);



        // Subscribe one more more topics to consume.
        consumer.subscribe("TopicTest", "*");


        //设置线程数 保证同一个Topic下的同一个MessageQueue里的消息不被并发消费
        consumer.setConsumeThreadMin(1);
        consumer.setConsumeThreadMax(1);

        //并发消费
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        //顺序消费
        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                msgs.stream().forEach(E->{
                    System.out.println("Fetch Msg Topic : "+context.getMessageQueue().getTopic());
                    System.out.println("Fetch Msg brokerName : "+context.getMessageQueue().getBrokerName());
                    System.out.println("Fetch Msg queueId : "+context.getMessageQueue().getQueueId());
                    System.out.println("Fetch Msg Body : "+new String(E.getBody(), Charset.forName(RemotingHelper.DEFAULT_CHARSET)));
                    System.out.println("Fetch Msg Tags : "+E.getTags());
                });
                return ConsumeOrderlyStatus.SUCCESS;
            }
        });

        //Launch the consumer instance.
        consumer.start();

        System.out.printf("Consumer Started.%n");
    }











}
