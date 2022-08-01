package com.thclouds.agent.conf;


/**
 * @description 统一配置信息
 */
public class Config {

    public static class Agent{

    }

    public static class Logging{
        /**
         * 日志文件名称
         */
        public static String FILE_NAME = "thclouds-api.log";

        /**
         * 存放目录，默认{gAgentJarDir}/logs
         */
        public static String DIR ="";

    }

    public static class FeignRule{

        public static  String REMOVE_ADDRESS = "nacos.test.qy566.com:80";
        // nacos group
        public static  String GROUP_ID = "DEFAULT_GROUP";

        public static  String NACOS_NAMESPACE_ID = "dev";

        public static final String FLOW_DATA_ID = "flow.rule";

        public static final String DEGRADE_DATA_ID = "degrade.rule";

        public static final String SYSTEM_DATA_ID = "system.rule";


    }
}
