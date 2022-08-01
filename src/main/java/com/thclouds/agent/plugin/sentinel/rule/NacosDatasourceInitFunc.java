package com.thclouds.agent.plugin.sentinel.rule;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.thclouds.agent.conf.Config;


import java.util.List;
import java.util.Properties;

/**
 * @author lixh
 */
public class NacosDatasourceInitFunc implements InitFunc {

    @Override
    public void init() throws Exception {
        System.out.println("NacosDatasourceInitFun init");
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, Config.FeignRule.REMOVE_ADDRESS);
        properties.put(PropertyKeyConst.NAMESPACE, Config.FeignRule.NACOS_NAMESPACE_ID);

        //限流规则初始化
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(properties, Config.FeignRule.GROUP_ID, Config.FeignRule.FLOW_DATA_ID,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                }));
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        //限流规则初始化

        //系统保护规则初始化

        //访问控制规则

    }
}
