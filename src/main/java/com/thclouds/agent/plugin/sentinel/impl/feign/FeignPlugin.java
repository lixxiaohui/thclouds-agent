package com.thclouds.agent.plugin.sentinel.impl.feign;

import com.thclouds.agent.plugin.IPlugin;
import com.thclouds.agent.plugin.InterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class FeignPlugin implements IPlugin {
    @Override
    public String name() {
        return "SentinelFeign";
    }

    @Override
    public InterceptPoint[] buildInterceptPoint() {
        return new InterceptPoint[]{
                new InterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        return ElementMatchers.nameStartsWith("org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient")
//                                .or(ElementMatchers.nameStartsWith("com.thclouds.commons.base.request.intercepter.FeignRequestInterceptor"))
                                ;
                    }
                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod()
                                  .and(ElementMatchers.any())
                                  .and(ElementMatchers.nameStartsWith("execute"))
//                                .or(ElementMatchers.nameStartsWith("apply"))
                                ;
                    }
                }
        };
    }
    @Override
    public Class adviceClass() {
        return FeignAdice.class;
    }

}
