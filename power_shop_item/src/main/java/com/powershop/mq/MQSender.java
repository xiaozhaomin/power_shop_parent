package com.powershop.mq;

import com.powershop.mapper.LocalMessageMapper;
import com.powershop.pojo.LocalMessage;
import com.powershop.utils.JsonUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ConfirmCallback：成功回调（消息发送成功调用confirm）
 * ReturnCallback：失败回调（消息发送失败并退回是调用returnedMessage）
 */
@Component
public class MQSender implements ConfirmCallback,ReturnCallback {
    @Autowired
    private LocalMessageMapper localMessageMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    //成功回调的业务逻辑（代表消息回调成功发送给RabbitMQ）
    public void sendMsg(LocalMessage localMessage){
        rabbitTemplate.setConfirmCallback(this);//回调成功
        rabbitTemplate.setReturnCallback(this);//回调失败
        CorrelationData correlationData = new CorrelationData(localMessage.getTxNo());
        rabbitTemplate.convertAndSend("item_exchange","item.add", JsonUtils.objectToJson(localMessage),correlationData);
                                        //item_exchange 交换器  //routing  //消息   //回调对象
    }
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        //修改localmessage的status
        if(ack){
            String txDo = correlationData.getId();//可以取到消息主键的id
            LocalMessage localMessage = new LocalMessage();
            localMessage.setTxNo(txDo);//主键
            localMessage.setState(1);//状态
            localMessageMapper.updateByPrimaryKeySelective(localMessage);//修改状态
        }
    }
    //失败回调（消息退回此方法）
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("return--message:" + new String(message.getBody())
                + ",exchange:" + exchange + ",routingKey:" + routingKey);
        //message 返回信息  exchange 交换器
    }
}
