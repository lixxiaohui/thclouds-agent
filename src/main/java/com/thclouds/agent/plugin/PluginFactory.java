package com.thclouds.agent.plugin;

import com.thclouds.agent.plugin.sentinel.impl.feign.SentinelFeignPlugin;

import java.util.ArrayList;
import java.util.List;

public class PluginFactory {
    public static List<IPlugin> pluginGroup = new ArrayList<>();
    static {
        //熔断限流
        pluginGroup.add(new SentinelFeignPlugin());

    }

}
