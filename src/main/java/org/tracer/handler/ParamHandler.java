package org.tracer.handler;

import org.tracer.logger.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 参数处理
 */
public class ParamHandler {

    private static Map<String, String> agentArgMap = new HashMap<>();

    static {
        agentArgMap.put("outputToConsole", "false");
        agentArgMap.put("serverPort", "18988");
        agentArgMap.put("serverEnable", "false");
    }

    /**
     * 解析参数（格式：key1=value1,key2=value2）
     */
    public static void parseArgs(String agentArgs) {
        if (agentArgs != null && !agentArgs.isEmpty()) {
            String[] pairs = agentArgs.split(",");
            for (String pair : pairs) {
                String[] entry = pair.split("=", 2); // 最多分割成两部分
                if (entry.length == 2) {
                    String key = entry[0].trim();
                    String value = entry[1].trim();
                    agentArgMap.put(key, value);
                }
            }
        }
    }

    /**
     * 加载各种组件
     */
    public static void loadAgent() {
        // 处理日志
        LoggerUtil.builder(agentArgMap);
    }

    /**
     * 获取打印参数
     *
     * @return
     */
    public static Boolean getOutputToConsole() {
        return agentArgMap.get("outputToConsole") != null && Boolean.parseBoolean(agentArgMap.get("outputToConsole"));
    }

    public static Integer getServerPort() {
        return agentArgMap.get("serverPort") != null ? Integer.parseInt(agentArgMap.get("serverPort")) : 18988;
    }

    /**
     * 获取服务端口号
     *
     * @return
     */
    public static Boolean isServerEnable() {
        return agentArgMap.get("serverEnable") != null && Boolean.parseBoolean(agentArgMap.get("serverEnable"));
    }


    public static void setOutputToConsole(String outputToConsole) {
        agentArgMap.put("outputToConsole", outputToConsole);
    }
}
