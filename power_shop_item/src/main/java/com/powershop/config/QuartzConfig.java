package com.powershop.config;

import com.powershop.quartz.ItemJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {
    @Bean
    //做什么事
    public MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean(ItemJob itemJob){
        MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        methodInvokingJobDetailFactoryBean.setTargetObject(itemJob);//调用哪个类
        methodInvokingJobDetailFactoryBean.setTargetMethod("scanLocalMessageAndSendMsg");//调用哪个目标类的方法
        return methodInvokingJobDetailFactoryBean;
    }
    //什么时候做
    @Bean
    public CronTriggerFactoryBean cronTriggerFactoryBean(MethodInvokingJobDetailFactoryBean job){
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setCronExpression("*/1 * * * * ?");//调用cron表达式
        cronTriggerFactoryBean.setJobDetail(job.getObject());//设置job 从工厂做什么事工厂拿在转到schedulerFactoryBean调用
        return cronTriggerFactoryBean;
    }
    //什么时候做什么事
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(CronTriggerFactoryBean trigger){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setTriggers(trigger.getObject());//调用上层工厂
        return schedulerFactoryBean;
    }
}
