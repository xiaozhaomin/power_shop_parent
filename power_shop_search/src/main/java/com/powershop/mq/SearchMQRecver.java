package com.powershop.mq;

import com.powershop.pojo.LocalMessage;
import com.powershop.pojo.MsgDistinct;
import com.powershop.service.MsgDistinctService;
import com.powershop.service.SearchItemService;
import com.powershop.utils.JsonUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class SearchMQRecver {
    @Autowired
    private SearchItemService searchItemService ;
    @Autowired
    private MsgDistinctService msgDistinctService;

    @RabbitListener(bindings = {@QueueBinding(
            value = @Queue(value = "search_queue",declare = "true"),
            exchange = @Exchange(value = "item_exchange",type = ExchangeTypes.TOPIC),
            key = {"item.*"}
    )})
    //修改一致性
    public void listen(String msg, Channel channel, Message message){
        try {
            System.out.println("msg"+msg);
            LocalMessage localMessage = JsonUtils.jsonToPojo(msg, LocalMessage.class);
            //查询消息去重表
            MsgDistinct msgDistinct =msgDistinctService.getMsgDistinctByTxNo(localMessage.getTxNo());
            if (msgDistinct == null ){
                //查不到则同步索引库，并且插入去重表
                searchItemService.insertDoc(localMessage.getItemId());
                msgDistinct =new MsgDistinct();
                msgDistinct.setTxNo(localMessage.getTxNo());
                msgDistinct.getCreateTime(new Date());
                msgDistinctService.insertMsgDistinct(msgDistinct);
            }else{
                System.out.println("=======幂等生效：事务"+msgDistinct.getTxNo()
                        +" 已然成功执行过了===========");
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
