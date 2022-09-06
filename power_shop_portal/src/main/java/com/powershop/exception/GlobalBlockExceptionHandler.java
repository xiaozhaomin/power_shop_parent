package com.powershop.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.fastjson.JSON;
import com.powershop.utils.Result;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class GlobalBlockExceptionHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        Result data = null;
        if (e instanceof FlowException) {
            data = Result.build(-1, "限流异常...");
        } else if (e instanceof DegradeException) {
            data = Result.build(-2, "降级异常...");
        }else if (e instanceof ParamFlowException) {
            data = Result.build(-3, "参数限流异常...");
        }else if (e instanceof AuthorityException) {
            data = Result.build(-4, "授权异常...");
        }else if (e instanceof SystemBlockException) {
            data = Result.build(-5, "系统负载异常...");
        }
        response.getWriter().write(JSON.toJSONString(data));
    }
}
