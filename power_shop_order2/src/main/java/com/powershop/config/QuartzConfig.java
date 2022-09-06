package com.powershop.config;

import com.powershop.job.OrderJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
@Configuration
public class QuartzConfig {
    //1、job：做什么事
    @Bean//job
    public MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean(
            OrderJob orderJob){
        MethodInvokingJobDetailFactoryBean JobDetailFactoryBean = new
                MethodInvokingJobDetailFactoryBean();
        JobDetailFactoryBean.setTargetObject(orderJob);
        JobDetailFactoryBean.setTargetMethod("closeTimeoutOrder");
        return JobDetailFactoryBean;
    }

    //2、trigger:什么时候做
    @Bean//trigger（job）
    public CronTriggerFactoryBean cronTriggerFactoryBean(
            MethodInvokingJobDetailFactoryBean JobDetailFactoryBean){
        CronTriggerFactoryBean triggerFactoryBean = new CronTriggerFactoryBean();
        triggerFactoryBean.setCronExpression("*/5 * * * * ?");
        triggerFactoryBean.setJobDetail(JobDetailFactoryBean.getObject());
        return triggerFactoryBean;
    }

    //3、scheduled：什么时候做什么事
    @Bean//scheduler
    public SchedulerFactoryBean schedulerFactoryBean(
            CronTriggerFactoryBean triggerFactoryBean){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setTriggers(triggerFactoryBean.getObject());
        return schedulerFactoryBean;
    }
}
