package com.thclouds.agent.plugin.sentinel.impl.feign;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.fastjson.JSON;
import com.thclouds.agent.context.EntryContext;
import com.thclouds.agent.context.EntryHolder;
import feign.Request;
import net.bytebuddy.asm.Advice;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FeignAdice {


    @Advice.OnMethodEnter()
    public static <ParamFlowException> void enter(@Advice.Origin("#t") String className, @Advice.Origin("#m") String methodName, @Advice.AllArguments Object[] allArguments) throws Exception {
//        initSystemRule();
//        registerStateChangeObserver();
        System.out.println(Thread.currentThread().getId()+"  className："+className + " methodName: "+methodName );

        Request request = (Request)allArguments[0];
        Request.Options options = (Request.Options)allArguments[1];
        URI asUri = URI.create(request.url());
//        String resourceName = asUri.getHost();
        String resourceName = methodName+":"+asUri.getPath();
        System.out.println(Thread.currentThread().getId()+"  参数：" + request.body()+  "   "+request.url()+"   "+ asUri.getHost() +"    "+asUri.getPath() );

        Entry entry = null;
        try {
            //TODO 根据资源的来源判断
            // 被保护的逻辑
            entry = SphU.entry(resourceName,EntryType.OUT);
            //系统规则只对 IN 生效
//            entry = SphU.entry("methodA", EntryType.IN);
            System.out.println(Thread.currentThread().getId()+"  enter");
        } catch (Exception ex) {
            System.out.println(Thread.currentThread().getId()+"  block : "+ ex.getClass());
            if (ex instanceof FlowException) {
                System.out.println(Thread.currentThread().getId()+"限流: "+ ex);
                throw new RuntimeException("BlockException 系统限流了，请稍后再试!");
            } else if (ex instanceof DegradeException) {

                System.out.println(Thread.currentThread().getId()+"降级"+  ((DegradeException) ex).getRule());
                throw new RuntimeException("BlockException 系统降级了，请稍后再试!");
            } else if (ex instanceof SystemBlockException) {
                System.out.println(Thread.currentThread().getId()+"系统规则(负载/...不满足要求)"+ex);
                throw new RuntimeException("BlockException 系统规则(负载/...不满足要求)");
            } else if (ex instanceof AuthorityException) {
                System.out.println(Thread.currentThread().getId()+"授权规则不通过" + ex);
                throw new RuntimeException("BlockException 授权规则不通过");
            }

            throw new RuntimeException("BlockException");
            // 处理被流控的逻
        }
        EntryContext.putEntryHolder(new EntryHolder(entry,null));
    }


    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(@Advice.Origin("#t") String className,
                            @Advice.Origin("#m") String methodName, @Advice.Thrown Throwable e) {

        if (e!=null && !BlockException.isBlockException(e)) {
            Tracer.trace(e);
        }
        EntryHolder entryHolder = EntryContext.getEntryHolder();
        System.out.println(Thread.currentThread().getId()+" entryHolder：" + entryHolder);
        if (null !=  entryHolder){
            entryHolder.getEntry().exit();
            System.out.println(Thread.currentThread().getId()+"  method exit");
        }

    }


    public static void registerStateChangeObserver() {
        EventObserverRegistry.getInstance().addStateChangeObserver("logging",
                (prevState, newState, rule, snapshotValue) -> {
                    if (newState == CircuitBreaker.State.OPEN) {
                        System.err.println(String.format("%s -> OPEN at %d, snapshotValue=%.2f", prevState.name(),
                                TimeUtil.currentTimeMillis(), snapshotValue));
                    } else {
                        System.err.println(String.format("%s -> %s at %d", prevState.name(), newState.name(),
                                TimeUtil.currentTimeMillis()));
                    }
                });
    }

    public static void initFlowRules(){
        if (FlowRuleManager.getRules().isEmpty()) {
            List<FlowRule> rules = new ArrayList<>();
            FlowRule rule = new FlowRule("thclouds-company-service");
            // set limit qps to 20
            rule.setCount(5);
            rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            rule.setLimitApp("default");
            rules.add(rule);
            FlowRuleManager.loadRules(rules);

        }
    }


    public  static void initDegradeRule() {
        if (DegradeRuleManager.getRules().isEmpty()) {
            List<DegradeRule> rules = new ArrayList<>();
            DegradeRule rule = new DegradeRule("thclouds-company-service")
                    .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
                    // Max allowed response time
                    .setCount(50)
                    // Retry timeout (in second)
                    .setTimeWindow(5)
                    // Circuit breaker opens when slow request ratio > 60%
                    .setSlowRatioThreshold(0.6)
                    .setMinRequestAmount(5)
                    .setStatIntervalMs(2000);
            rules.add(rule);
            DegradeRuleManager.loadRules(rules);
        }
    }

    public static void initSystemRule() {
        List<SystemRule> rules = new ArrayList<SystemRule>();
        SystemRule rule = new SystemRule();
        // max load is 3
        rule.setHighestSystemLoad(3.0);
        // max cpu usage is 60%
        rule.setHighestCpuUsage(0.6);
        // max avg rt of all request is 10 ms
        rule.setAvgRt(10);
        // max total qps is 20
        rule.setQps(20);
        // max parallel working thread is 10
        rule.setMaxThread(10);

        rules.add(rule);
        SystemRuleManager.loadRules(Collections.singletonList(rule));
    }

}
