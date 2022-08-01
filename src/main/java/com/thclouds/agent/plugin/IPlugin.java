package com.thclouds.agent.plugin;

public interface IPlugin {
    //名称
    String name();
    //监控点
    InterceptPoint[] buildInterceptPoint();
    //拦截器类
    Class adviceClass();
}
