package com.thclouds.agent;


import com.thclouds.agent.conf.ConfigInitializer;
import com.thclouds.agent.plugin.IPlugin;
import com.thclouds.agent.plugin.InterceptPoint;
import com.thclouds.agent.plugin.PluginFactory;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.logging.LogManager;

import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class SentinelAgent {
//    private static ILog LOGGER = LogManager.getLogger(SentinelAgent.class);
    //JVM 首先尝试在代理类上调用以下方法
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("============================agnent 开启========================== == ==\r\n");
        //TODO 1、添加配置文件和参数解析
        ConfigInitializer.initializeCoreConfig(agentArgs);
        //2、加载插件  TODO 根据配置加载插件
        List<IPlugin> pluginGroup = PluginFactory.pluginGroup;
        //3、字节码插装  TODO看看是不是可以通过环绕加强
        AgentBuilder agentBuilder = new AgentBuilder.Default().ignore(
                nameStartsWith("net.bytebuddy.")
                        .or(nameStartsWith("org.slf4j."))
                        .or(nameStartsWith("org.groovy."))
                        .or(nameContains("javassist"))
                        .or(nameContains(".asm."))
                        .or(nameContains(".reflectasm."))
                        .or(nameStartsWith("sun.reflect"))
                        .or(ElementMatchers.<TypeDescription>isSynthetic()));

        for (IPlugin plugin : pluginGroup) {
            InterceptPoint[] interceptPoints = plugin.buildInterceptPoint();
            for (InterceptPoint point : interceptPoints) {
                AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
                    builder = builder.visit(Advice.to(plugin.adviceClass()).on(point.buildMethodsMatcher()));
                    return builder;
                };
                agentBuilder = agentBuilder.type(point.buildTypesMatcher()).transform(transformer);
            }
        }

        //and(ElementMatchers."org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient")) // 拦截任意方法

            //监听
        AgentBuilder.Listener listener = new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {

                System.out.println("onTransformation：" + typeDescription);
            }

            @Override
            public void onIgnored(TypeDescription typeDescription,
                                  ClassLoader classLoader, JavaModule javaModule, boolean b) {
            }

            @Override
            public void onError(String s, ClassLoader classLoader,
                                JavaModule javaModule, boolean b, Throwable throwable) {
                throwable.printStackTrace();
                System.err.println("onerror：" + s+"      "+throwable.getMessage());
            }

            @Override
            public void onComplete(String s, ClassLoader classLoader,
                                   JavaModule javaModule, boolean b) {
            }
        };

        agentBuilder.with(listener).installOn(inst);

        //启动监控服务
        //停机回调
    }






}

