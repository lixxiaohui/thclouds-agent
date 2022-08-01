package com.thclouds.agent.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 初始化所有配置
 */
public class ConfigInitializer {

    private static final String SPECIFIED_CONFIG_PATH = "thclouds_config";
    private static final String DEFAULT_CONFIG_FILE_NAME = "/config/agent.properties";
    private static Properties AGENT_SETTINGS;


    /**
     * 1、读取配置文件中的配置
     * 2、读取系统配置覆盖配置
     * 3、解析参数中的配置，覆盖配置
     * 4、灌入配置类中
     *
     * @param agentOptions
     */
    public static void initializeCoreConfig(String agentOptions) {
        AGENT_SETTINGS = new Properties();
//        try (final InputStreamReader configFileStream = loadConfig()) {
//            AGENT_SETTINGS.load(configFileStream);
//            for (String key : AGENT_SETTINGS.stringPropertyNames()) {
//                String value = (String) AGENT_SETTINGS.get(key);
//                AGENT_SETTINGS.put(key, PropertyPlaceholderHelper.INSTANCE.replacePlaceholders(value, AGENT_SETTINGS));
//            }
//
//        } catch (Exception e) {
//            LOGGER.error(e, "Failed to read the config file, skywalking is going to run in default config.");
//        }
    }


//    /**
//     * 从配置文件中加载配置
//     */
//    private static InputStreamReader loadConfig() {
//        String specifiedConfigPath = System.getProperty(SPECIFIED_CONFIG_PATH);
//        File configFile = StringUtil.isEmpty(specifiedConfigPath) ? new File(
//                AgentPackagePath.getPath(), DEFAULT_CONFIG_FILE_NAME) : new File(specifiedConfigPath);
//
//        if (configFile.exists() && configFile.isFile()) {
//            try {
//                LOGGER.info("Config file found in {}.", configFile);
//
//                return new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8);
//            } catch (FileNotFoundException e) {
//                throw new ConfigNotFoundException("Failed to load agent.config", e);
//            }
//        }
//        throw new ConfigNotFoundException("Failed to load agent.config.");
//    }
}
