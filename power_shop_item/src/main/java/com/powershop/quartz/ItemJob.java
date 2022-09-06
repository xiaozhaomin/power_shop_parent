package com.powershop.quartz;

import com.powershop.mapper.LocalMessageMapper;
import com.powershop.mq.MQSender;
import com.powershop.pojo.LocalMessage;
import com.powershop.pojo.LocalMessageExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

//扫描消息记录表返回消息
@Component//加入容器
public class ItemJob {
    @Autowired
    private LocalMessageMapper localMessageMapper;
    @Autowired
    private MQSender mqSender;
    public void scanLocalMessageAndSendMsg(){
        LocalMessageExample localMessageExample = new LocalMessageExample();//查询状态为0的
        LocalMessageExample.Criteria criteria = localMessageExample.createCriteria();//加入条件
        criteria.andStateEqualTo(0);//条件的状态
        List<LocalMessage> localMessages = localMessageMapper.selectByExample(localMessageExample);//查询出多个消息 去遍历
        for (LocalMessage localMessage : localMessages) {
            //写个mq实现接口去调用
            //2、发送消息
            mqSender.sendMsg(localMessage);
            System.out.println("ItemJob定时任务执行："+localMessage.toString());
        }
    }
}
