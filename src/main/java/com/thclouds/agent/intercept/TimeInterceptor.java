package com.thclouds.agent.intercept;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import feign.Request;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class TimeInterceptor  {

    @RuntimeType
    public static Object intercept(@This Object obj, @AllArguments Object[] allArguments, @Origin Method method,


                                   @SuperCall Callable<?> callable) throws Exception {
        initFlowRules();
        Request request = (Request)allArguments[0];
        Request.Options options = (Request.Options)allArguments[1];
        URI asUri = URI.create(request.url());
        String resourceName = asUri.getHost();
        Entry entry = null;
        Object call = null;
        try {
            // 被保护的逻辑
            entry = SphU.entry(resourceName);
             call = callable.call();

        } catch (BlockException ex) {
            // 处理被流控的逻辑
            System.out.println("blocked!");
        }finally {
            entry.exit();
            return call;
        }

    }

    public static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule("thclouds-company-service");
        // set limit qps to 20
        rule.setCount(1);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setLimitApp("default");
        rules.add(rule);
        FlowRuleManager.loadRules(rules);

    }
}
